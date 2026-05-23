# MyChat App 实施进度报告

## 2026-05-23 本次完善

- 对齐 web 后端真实协议：新闻列表/评论支持 `data + pagination`，新闻字段兼容 `coverImage`、`author`、`publishedAt`，评论字段兼容 `username`、`userId`。
- Android 端补齐 web 端主要功能入口：发布新闻、新闻评论、管理后台、个人设置、关于 Q信、服务器状态。
- Socket 会话连接改为复用 web 端 token 登录流程，登录成功后读取在线用户、好友分组和群聊历史。
- 群聊发送会带上当前用户信息，能写入 web 端群聊历史；在线好友私聊会使用 web Socket 返回的 `roomId` 定向发送。
- 服务器地址变更后会重建 Retrofit 与 Socket 连接，便于连接宿主机 web 后端。
- 扫码功能补充 `CAMERA` 权限声明；取消按钮可点击返回。
- 移除主要页面中的表情占位，改为更贴近 web 海岛风格的文字标识、绿色按钮、浅色卡片和圆角界面。
- 修复扫码配置服务器闪退风险：补充 CameraX `camera-camera2` 运行依赖，并在相机初始化失败时显示错误而不是崩溃。
- 修复移动端静态资源路径：后端返回 `/static/...`、`/assets/...`、`/upload/...` 时会自动拼接当前服务器地址；头像、新闻封面、聊天图片消息使用 Coil 加载。
- 修复登录页顶部大面积空白：`SkyDecoration` 改为背景层，不再参与登录页纵向排版。
- 将登录页、聊天页、我的页、AI 页中的“小岛/海面/木屋/草地/AI”等文字拼景替换为 Compose Canvas 绘制的小岛插画卡片。

当前验证状态：

- `.\gradlew.bat :app:compileDebugKotlin --offline` 仍被本机环境阻塞：`JAVA_HOME is not set and no 'java' command could be found in your PATH`。
- 已在 `D:\sdk`、`C:\Program Files`、`C:\Users\Tenz\AppData\Local` 常见位置查找 `java.exe`，未找到可临时指定的 JDK/JBR。

## 架构

```
手机 App (Kotlin/Compose) ──HTTP/WebSocket──▶ 宿主机 Node.js 后端 (Express + Socket.IO)
                                                      │
                                                      └── MySQL (虚拟机 IP)
```

后端直接复用 web 项目 (`mychat/server`)，无需修改。

---

## 已完成：全部 8 个阶段

### Phase 1 — 项目重构 + 海岛主题

**状态：已完成**

- 将原 590 行 `MainActivity.kt` 拆分为 12 个文件
- 创建海岛色板 `IslandColors.kt`：`IslandGreen500` (#6FB34F)、`IslandText` (#2E332D)、`IslandCream` (#FFFAF0) 等
- `Theme.kt` 用 `lightColorScheme` 替代 Material3 默认紫色
- `Type.kt` 定义全字体排版体系
- 添加 `navigation-compose`、`CameraX`、`MLKit Barcode` 依赖
- 配置阿里云 Maven 镜像 + HTTP 代理 7890

### Phase 2 — API 层完善

**状态：已完成**

`MyChatApi.kt`：25+ REST 端点，覆盖 web 端所有接口：

| 模块 | 端点 |
|------|------|
| 认证 | `login`, `register`, `getMe` |
| 好友 | `getFriends`, `requestFriend`, `acceptFriend`, `deleteFriend` |
| 设置 | `getSettings`, `saveSettings` |
| AI | `askAi`, `getAiConfig`, `saveAiConfig` |
| 新闻 | `getNews`, `getNewsDetail`, `getHotNews`, `getRecentNews`, `getCategories`, `incrementView`, `getComments`, `addComment`, `deleteComment`, `createNews` |
| 系统 | `getAbout`, `healthCheck` |
| 上传 | `uploadFile` (Multipart) |

数据模型：`AuthModels` / `FriendModels` / `NewsModels` / `SettingsModels` / `AiModels` / `SystemModels`

Repository 层：`AuthRepository` / `FriendRepository` / `NewsRepository` / `AssistantRepository` / `SettingsRepository` / `ChatRepository`

`ApiClient.kt`：支持动态 baseUrl、Token 自动注入、Multipart 上传

### Phase 3 — 认证系统

**状态：已完成**

- 登录/注册在同一页面，Tab 切换
- 注册：账号 + 密码 + 确认密码，调用 `POST /api/auth/register`
- 自动登录：`TokenStore` 持久化 token，启动时 `getMe()` 验证
- QR 扫码：`QrScanScreen` — CameraX 预览 + MLKit 条码识别 + 扫描框叠加
- QR 结果解析：JSON `{"host":"http://..."}` 自动配置服务器地址

### Phase 4 — 实时聊天 + 好友系统

**状态：已完成**

`ChatSocketManager.kt`：完整的 Socket.IO 封装
- `StateFlow<Boolean>` 连接状态
- `SharedFlow<SocketEvent>` 事件流：`LoginSuccess` / `Message` / `SystemEvent` / `FriendRequest` / `FriendAccepted` / `FriendDeleted` / `HistoryMessages`
- 发送：`sendMessage` / `sendGroupMessage` / `sendFriendRequest` / `acceptFriend` / `deleteFriend`

UI：
- `ChatListScreen`：会话列表（群聊 + 在线用户 + 好友），搜索栏
- `ConversationScreen`：消息气泡（我方绿色 / 对方白色，圆角不同方向），文本输入 + 发送按钮
- `MessageBubble`：支持作者名、时间戳、文字内容

好友系统：
- `AppViewModel` 管理好友状态，支持 REST + Socket 双通道
- 好友列表融入会话列表

### Phase 5 — 新闻中心

**状态：已完成**

- `NewsListScreen`：Hero Card + 分类 FilterTabs + 新闻列表 + 搜索
- `NewsDetailScreen`：标题/作者/日期/阅读量 + 正文 + 评论区 + 评论输入
- API 接入：列表/详情/评论/阅读量递增/分类
- 空数据时显示预设内容 + "刷新后端新闻"按钮

### Phase 6 — AI 助手

**状态：已完成**

- `AiScreen`：AI 回答卡片 + 聊天历史（用户蓝色气泡 / AI 白色气泡）
- 快捷问题 chips：4 个预设问题
- 文本输入 + 发送按钮
- Loading 状态 `CircularProgressIndicator`

### Phase 7 — 个人设置

**状态：已完成**

- `MineScreen`：头像 + 用户名 + 在线状态指示
- 统计卡片：好友/群聊/收藏/积分
- 设置项：个人资料/消息通知/隐私/通用/主题/服务器地址/帮助
- 服务器地址配置弹窗（持久化到 SharedPreferences + 实时生效）

### Phase 8 — UI 打磨 + 连接配置

**状态：已完成**

- 全局海岛渐变背景 (`#FDF8ED` → `#FFFBF3` → `#F8EFD9`)
- 卡片风格：20-28dp 圆角、cream 半透明背景、绿色边框
- 底部导航栏：30dp 上圆角、绿色高亮指示
- `SkyDecoration`：浮动云朵装饰
- `IslandLandscapeStrip`：海岛风景条
- 服务器地址可配置（设置页 + QR 扫码自动配置）
- 中文错误提示

---

## 项目文件清单

```
mychatapp/
├── build.gradle.kts                     (插件: android + kotlin-compose)
├── settings.gradle.kts                  (阿里云镜像 + HTTP 代理配置)
├── gradle.properties                    (HTTP Proxy 127.0.0.1:7890)
├── gradle/
│   └── libs.versions.toml               (版本目录: Kotlin 2.1.20, AGP 9.1.1, Compose BOM 2025.03)
└── app/src/main/java/com/example/mychatapp/
    ├── MainActivity.kt                  (入口: ApiClient.init + Compose setContent)
    ├── data/
    │   ├── model/
    │   │   ├── AuthModels.kt            (Login/Register Request/Response, UserDto)
    │   │   ├── FriendModels.kt          (FriendDto, FriendListResponse, FriendRequest)
    │   │   ├── NewsModels.kt            (NewsDto, CommentDto, CategoryDto, CreateNewsRequest)
    │   │   ├── AiModels.kt              (AiChatRequest/Response)
    │   │   ├── SettingsModels.kt        (SettingsDto, AiConfigRequest/Response)
    │   │   └── SystemModels.kt          (AboutResponse, FileUploadResponse, HealthResponse)
    │   ├── remote/
    │   │   ├── MyChatApi.kt             (25+ Retrofit 接口定义)
    │   │   ├── ApiClient.kt             (OkHttp + Retrofit 单例, Token 拦截器)
    │   │   ├── ApiConfig.kt             (可持久化服务器地址, 默认 172.30.34.106:3000)
    │   │   ├── TokenStore.kt            (SharedPreferences Token 存储)
    │   │   └── RuntimeSession.kt        (内存中 auth 状态)
    │   ├── socket/
    │   │   └── ChatSocketManager.kt     (Socket.IO + StateFlow/SharedFlow 事件)
    │   └── repository/
    │       ├── AuthRepository.kt
    │       ├── FriendRepository.kt
    │       ├── NewsRepository.kt
    │       ├── AssistantRepository.kt
    │       ├── SettingsRepository.kt
    │       └── ChatRepository.kt        (文件上传 Uri → Multipart)
    └── ui/
        ├── AppViewModel.kt              (AndroidViewModel, 全局状态 + 所有业务逻辑)
        ├── AppNavigation.kt             (NavHost: login → main → conversation/detail/qr)
        ├── MainScreen.kt                (四 Tab 主页 + IslandBottomBar)
        ├── theme/
        │   ├── IslandColors.kt          (海岛色板 token)
        │   ├── Theme.kt                 (lightColorScheme 应用)
        │   └── Type.kt                  (IslandTypography)
        ├── screens/
        │   ├── LoginScreen.kt           (登录/注册 + IslandHeroCard)
        │   ├── ChatScreen.kt            (会话列表 + 聊天详情 + 消息气泡)
        │   ├── NewsScreen.kt            (新闻列表 + 详情 + 评论)
        │   ├── AiScreen.kt              (AI 对话 + 快捷问题)
        │   ├── MineScreen.kt            (个人资料 + 设置项 + 服务器配置入口)
        │   └── QrScanScreen.kt          (CameraX + MLKit 扫码)
        └── components/
            ├── SharedComponents.kt      (Avatar/SearchBar/FilterTabs/LandscapeStrip/等)
            └── ServerConfigDialog.kt    (服务器 IP 配置弹窗)
```

## 待编译验证

- 上一次构建有 3 个编译错误已全部修复（`TabItem` 可见性、`background` import、`PointF` → `Offset`）
- 已配置国内镜像 + HTTP 代理加速依赖下载
- 默认服务器地址：`http://172.30.34.106:3000`
