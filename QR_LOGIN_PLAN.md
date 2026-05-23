# 扫码登录 & 手机端连接后端 — 实施方案

## 1. 总体流程

```
桌面端 (Electron)                      后端 (Node.js)                    手机端 (Android)
─────────────────                      ──────────────                    ────────────────
                                                                         
1. 点击"扫码登录"Tab                                                            
2. 请求后端生成 QR 会话 ──────────▶  3. 生成 sessionId                    
                                   (Redis TTL 120s)                    
4. 显示 QR 码 ◀──────────────────  返回 sessionId + host                
  (sessionId + host)                                                       
                                                                         
                                                                   5. 打开扫码页面
                                                                   6. 扫描桌面端 QR 码
                                                                   7. 解析: sessionId + host
                                                                         │
                                                                   8. 配置 host 为服务器地址
                                                                         │
9. 桌面端轮询 sessionId 状态 ──────▶ 10. 手机调用 /auth/qr/confirm  ──────┘
                                    POST { sessionId, token(已登录) }
                                    → 标记 session 已确认
                                    → 返回桌面端用户信息
                                                                    
11. 桌面端收到确认 ◀──────────────── 返回 { status: "confirmed", user }    
12. 桌面端自动登录完成                                                      
```

---

## 2. 后端新增：QR 扫码登录 API（需在 web 项目 server 中新增）

### 2.1 新增路由 `server/routes/qrRouter.js`

```
GET    /api/auth/qr/generate    → 桌面端请求生成 QR 会话
POST   /api/auth/qr/confirm     → 手机端确认扫码
GET    /api/auth/qr/status/:id  → 桌面端轮询状态
```

### 2.2 接口详情

**2.2.1 GET /api/auth/qr/generate**
- 调用者：桌面端 Electron（已登录用户）
- Header：`Authorization: Bearer <desktop_user_token>`
- 逻辑：
  1. 验证 token 有效
  2. 生成 `sessionId = uuid()`
  3. Redis 写入：`qr:<sessionId>` → `{ userId, username, status: "pending", createdAt }`，TTL 120s
  4. 返回 `{ sessionId, host: "http://<lan_ip>:3000" }`
- 响应：`{ sessionId: "abc123", host: "http://172.30.34.106:3000" }`

**2.2.2 POST /api/auth/qr/confirm**
- 调用者：手机端（已登录用户，或者未登录也可以）
- Body：`{ sessionId: "abc123" }`
- Header：`Authorization: Bearer <mobile_user_token>`（如果手机已登录）
- 逻辑：
  1. 查 Redis `qr:<sessionId>`
  2. 如果不存在或已过期 → 返回 404 "二维码已过期"
  3. 将 session 状态改为 `confirmed`
  4. 如果手机端已登录（有 token），记录关联关系
  5. 返回桌面端用户信息
- 响应：`{ status: "confirmed", user: { id, username }, token: "<mobile_token>" }`

**2.2.3 GET /api/auth/qr/status/:id**
- 调用者：桌面端轮询
- 逻辑：
  1. 查 Redis `qr:<sessionId>`
  2. 返回当前状态
- 响应：`{ status: "pending" }` 或 `{ status: "confirmed", confirmedBy: { username }, token: "<mobile_token>" }`

### 2.3 Redis Key 设计
```
qr:<sessionId> → {
  "desktopUserId": 1,
  "desktopUsername": "admin",
  "status": "pending|confirmed|expired",
  "createdAt": 1716451200000,
  "confirmedBy": null
}
TTL: 120 seconds
```

---

## 3. 桌面端 Electron 改造

### 3.1 在 web Vue 项目中实现扫码登录 UI

目前 `IslandApp.vue` 中第 124-127 行有扫码登录的占位 UI：

```html
<div v-else-if="loginMode === 'login' && loginTab === 'scan'" class="island-login__qr">
  <div class="island-qr-grid" aria-label="模拟二维码"></div>
  <p>打开 Q信移动端扫码登录。</p>
</div>
```

改造步骤：
1. 引入 QR 码生成库（如 `qrcode` npm 包）
2. 点击"扫码登录"Tab 时，调用 `GET /api/auth/qr/generate`
3. 用返回的 `{ sessionId, host }` 拼成 JSON 字符串
4. 用 Canvas 渲染 QR 码图片
5. 启动定时器（每 2 秒轮询 `GET /api/auth/qr/status/:sessionId`）
6. 收到 `status: "confirmed"` 后，自动完成登录（页面跳转到主界面）
7. 如果 120 秒未确认，显示"二维码已过期，请刷新"

### 3.2 需要安装的 npm 包
```bash
npm install qrcode  # QR 码生成
```

### 3.3 QR 码内容格式
```json
{
  "action": "qr_login",
  "sessionId": "abc123-def456",
  "host": ""//根据实际
}
```

---

## 4. 手机端 Android 改造

### 4.1 现有状态
- `QrScanScreen.kt`：已完成 CameraX + MLKit 扫码
- `ApiConfig.kt`：可动态配置服务器地址
- 扫码回调已在 `AppNavigation.kt` 中处理

### 4.2 需要新增的 QR 登录逻辑


**Step 1：扫码后解析 JSON**

当前 `AppNavigation.kt` 已处理 JSON 解析 + host 配置，只需增加 `sessionId` 处理：

```kotlin
val json = JSONObject(result)
val host = json.optString("host", "")
val sessionId = json.optString("sessionId", "")
val action = json.optString("action", "")

if (action == "qr_login" && sessionId.isNotBlank() && host.isNotBlank()) {
    // 1. 配置 host
    ApiConfig.setBaseUrl(context, host)
    ApiClient.reinit(context, host)
    
    // 2. 如果已有 token（已登录），直接调用 confirm
    val token = viewModel.uiState.value.token
    if (!token.isNullOrBlank()) {
        viewModel.confirmQrLogin(sessionId)
    } else {
        // 未登录：先调用 register 或 demo 登录，再 confirm
        // 或者：新增一个手机端快速注册接口
    }
}
```


**Step 2：新增 ViewModel 方法**

在 `AppViewModel.kt` 中新增：

```kotlin
fun confirmQrLogin(sessionId: String) {
    viewModelScope.launch {
        runCatching {
            ApiClient.api.confirmQr(QrConfirmRequest(sessionId))
        }.onSuccess { response ->
            // QR 确认成功，桌面端应该已自动登录
            // 手机端收到新的 token
            val token = response.token ?: return@onSuccess
            tokenStore.saveToken(token)
            RuntimeSession.set(token, response.user?.username)
            _uiState.update { it.copy(token = token, signedIn = true) }
            socketManager.connect(token)
        }.onFailure {
            Toast.makeText(getApplication(), "扫码确认失败：${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

**Step 3：新增 API 端点和模型**

`MyChatApi.kt` 增加：
```kotlin
@POST("api/auth/qr/confirm")
suspend fun confirmQr(@Body body: QrConfirmRequest): LoginResponse
```

`AuthModels.kt` 增加：
```kotlin
data class QrConfirmRequest(val sessionId: String)
```

---

## 5. 手机端连接后端的三种场景

| 场景 | 方法 | 说明 |
|------|------|------|
| **扫码自动配置** | 扫桌面 QR 码 | QR 码含 `host`，自动设置服务器地址，最适合新用户 |
| **手动输入** | "我的" → "服务器地址" | 弹窗输入 `http://192.168.x.x:3000`，即输即生效 |
| **默认地址** | `ApiConfig.DEFAULT_BASE_URL` | 目前是 `172.30.34.106:3000`，同网段可直接连 |

---

## 6. 实施步骤总结

| 步骤 | 位置 | 内容 | 工作量 |
|------|------|------|--------|
| 1 | `mychat/server` | 新增 `routes/qrRouter.js` | ~60 行 |
| 2 | `mychat/server/index.js` | 注册 `/api/auth/qr` 路由 | 1 行 |
| 3 | `mychat/src/components/IslandApp.vue` | 扫码 Tab 改造（QR 生成 + 轮询） | ~80 行 |
| 4 | `mychat` | `npm install qrcode` | 1 个命令 |
| 5 | `mychatapp/.../MyChatApi.kt` | 新增 `confirmQr` 端点 | 3 行 |
| 6 | `mychatapp/.../AuthModels.kt` | 新增 `QrConfirmRequest` | 1 行 |
| 7 | `mychatapp/.../AppViewModel.kt` | 新增 `confirmQrLogin` 方法 | ~15 行 |
| 8 | `mychatapp/.../AppNavigation.kt` | 扫码回调增加 sessionId 处理 | ~15 行 |

---

## 7. 备选简化方案（如果不想改后端）

不改后端的情况下：

1. **QR 码只传 host**：桌面端不生成 session，扫码只是为了拿到 LAN IP
2. **手机扫码后手动输入账号密码登录**：拿到 host → 自动填入 → 用户手动在手机上登录
3. 优点：不需要改后端和桌面端代码，只需手机端支持扫码读取 host
4. 缺点：不是真正的"扫码登录"，仍需手动输入密码

当前手机端已支持此简化方案（扫码 JSON 中带 `host` 字段即自动配置）。
