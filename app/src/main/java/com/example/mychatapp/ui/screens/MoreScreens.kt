package com.example.mychatapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.data.model.NewsDto
import com.example.mychatapp.data.model.SettingsDto
import com.example.mychatapp.ui.AppUiState
import com.example.mychatapp.ui.components.SettingRow
import com.example.mychatapp.ui.components.StatItem
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandGreen700
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

// ─── Publish News ────────────────────────────────────────────

@Composable
fun PublishNewsScreen(
    categories: List<String>,
    loading: Boolean,
    onPublish: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "推荐") }

    FormScaffold(title = "发布新闻", onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            categories.take(4).forEach { item ->
                Button(
                    onClick = { category = item },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == item) IslandGreen500 else Color.White,
                        contentColor = if (category == item) Color.White else IslandGreen700
                    ),
                    border = BorderStroke(1.dp, IslandLine),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(item, fontSize = 13.sp) }
            }
        }
        IslandOutlinedField(title, { title = it }, "新闻标题")
        IslandOutlinedField(summary, { summary = it }, "摘要")
        IslandOutlinedField(content, { content = it }, "正文内容", minLines = 8)
        Button(
            onClick = { onPublish(title, summary, content, category) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !loading,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
        ) { Text(if (loading) "发布中..." else "发布到新闻中心", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

// ─── Settings ────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    state: AppUiState,
    onSave: (SettingsDto) -> Unit,
    onBack: () -> Unit,
    onChangePassword: ((String, String) -> Unit)? = null,
    onClearCache: (() -> Unit)? = null
) {
    var notifications by remember(state.settings) { mutableStateOf(state.settings?.notifications ?: true) }
    var voice by remember(state.settings) { mutableStateOf(state.settings?.isVoice ?: true) }
    var fontSize by remember(state.settings) { mutableStateOf(state.settings?.fontSize ?: "standard") }
    var showPwdDialog by remember { mutableStateOf(false) }
    var showCacheDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    FormScaffold(title = "个人设置", onBack = onBack) {
        // Notifications
        ToggleRow("消息通知", "接收聊天、好友和新闻提醒", notifications) { notifications = it }
        ToggleRow("语音提示", "保留 web 端语音开关配置", voice) { voice = it }

        // Font size
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
            border = BorderStroke(1.dp, IslandLine)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("字体大小", fontWeight = FontWeight.Black, color = IslandText, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("compact" to "紧凑", "standard" to "标准", "large" to "舒展").forEach { (value, label) ->
                        Button(
                            onClick = { fontSize = value },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize == value) IslandGreen500 else Color.White,
                                contentColor = if (fontSize == value) Color.White else IslandGreen700
                            ),
                            border = BorderStroke(1.dp, IslandLine),
                            shape = RoundedCornerShape(18.dp)
                        ) { Text(label) }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("更多操作", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IslandGreen700)
        Spacer(Modifier.height(4.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
            border = BorderStroke(1.dp, IslandLine)
        ) {
            Column {
                SettingRow("密", "修改密码") { showPwdDialog = true }
                SettingRow("清", "清理缓存") { showCacheDialog = true }
            }
        }

        // Save button
        Button(
            onClick = { onSave(SettingsDto(theme = "island", fontSize = fontSize, isVoice = voice, notifications = notifications)) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
        ) { Text("保存设置", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }

    // Password change dialog
    if (showPwdDialog) {
        var oldPwd by remember { mutableStateOf("") }
        var newPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPwdDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White.copy(alpha = 0.95f),
            title = { Text("修改密码", fontWeight = FontWeight.Black, color = IslandText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = oldPwd, onValueChange = { oldPwd = it }, label = { Text("旧密码") },
                        visualTransformation = PasswordVisualTransformation(), singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslandGreen500, unfocusedBorderColor = IslandLine))
                    OutlinedTextField(value = newPwd, onValueChange = { newPwd = it }, label = { Text("新密码") },
                        visualTransformation = PasswordVisualTransformation(), singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslandGreen500, unfocusedBorderColor = IslandLine))
                    OutlinedTextField(value = confirmPwd, onValueChange = { confirmPwd = it }, label = { Text("确认密码") },
                        visualTransformation = PasswordVisualTransformation(), singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslandGreen500, unfocusedBorderColor = IslandLine))
                }
            },
            confirmButton = {
                Button(onClick = {
                    when {
                        oldPwd.isBlank() || newPwd.isBlank() -> Toast.makeText(context, "请填写完整", Toast.LENGTH_SHORT).show()
                        newPwd != confirmPwd -> Toast.makeText(context, "两次密码不一致", Toast.LENGTH_SHORT).show()
                        newPwd.length < 3 -> Toast.makeText(context, "密码至少3位", Toast.LENGTH_SHORT).show()
                        else -> {
                            onChangePassword?.invoke(oldPwd, newPwd)
                            showPwdDialog = false
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500), shape = RoundedCornerShape(16.dp)) {
                    Text("确认修改", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showPwdDialog = false }) { Text("取消", color = IslandGreen500) } }
        )
    }

    // Cache clear dialog
    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCacheDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White.copy(alpha = 0.95f),
            title = { Text("清理缓存", fontWeight = FontWeight.Black, color = IslandText) },
            text = { Text("将清除本地聊天图片、表情和头像缓存，释放存储空间。", color = IslandMuted) },
            confirmButton = {
                Button(onClick = {
                    onClearCache?.invoke()
                    showCacheDialog = false
                    Toast.makeText(context, "缓存已清理", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500), shape = RoundedCornerShape(16.dp)) {
                    Text("立即清理", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showCacheDialog = false }) { Text("取消", color = IslandGreen500) } }
        )
    }
}

// ─── Admin ───────────────────────────────────────────────────

@Composable
fun AdminScreen(
    state: AppUiState,
    onRefresh: () -> Unit,
    onDeleteNews: (Int) -> Unit,
    onBack: () -> Unit
) {
    FormScaffold(title = "管理后台", onBack = onBack) {
        if (state.userRole != "admin") {
            Text("当前账号没有管理员权限", color = IslandMuted)
            return@FormScaffold
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, IslandLine)
        ) {
            Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem((state.adminStats?.perCategory?.size ?: 0).toString(), "分类")
                StatItem(state.news.size.toString(), "文章")
                StatItem(state.auditLogs.size.toString(), "日志")
            }
        }
        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = IslandGreen700),
            border = BorderStroke(1.dp, IslandLine)
        ) { Text("刷新后台数据", fontWeight = FontWeight.Bold) }
        Text("新闻管理", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IslandText)
        state.news.forEach { item ->
            AdminNewsRow(item = item, onDelete = { item.id?.let(onDeleteNews) })
        }
        Text("最近操作", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IslandText)
        state.auditLogs.take(8).forEach { log ->
            Text("${log.admin ?: "系统"} · ${log.action ?: "操作"} · ${log.createdAt ?: ""}", color = IslandMuted, fontSize = 14.sp)
        }
    }
}

// ─── About ───────────────────────────────────────────────────

@Composable
fun AboutScreen(state: AppUiState, onBack: () -> Unit) {
    val about = state.about
    FormScaffold(title = "关于 Q信", onBack = onBack) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF7FF).copy(alpha = 0.72f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Q信 MyChat", fontSize = 30.sp, fontWeight = FontWeight.Black, color = IslandText)
                Text("聊天、新闻中心、AI 助手和管理后台共用同一个 web 后端。", color = IslandMuted, fontSize = 16.sp)
                Text("版本：${about?.version ?: "--"} · 构建：${about?.build ?: "--"}", color = IslandGreen700, fontWeight = FontWeight.Bold)
            }
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
            border = BorderStroke(1.dp, IslandLine)
        ) {
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem((about?.counts?.users ?: 0).toString(), "用户")
                StatItem((about?.counts?.news ?: 0).toString(), "新闻")
                StatItem((about?.counts?.comments ?: 0).toString(), "评论")
                StatItem((about?.counts?.friendships ?: 0).toString(), "好友")
            }
        }
        StatusLine("MySQL", state.health?.mysql?.ready ?: about?.mysql?.ready, state.health?.mysql?.error ?: about?.mysql?.error)
        StatusLine("Redis", state.health?.redis?.ready ?: about?.redis?.ready, state.health?.redis?.error ?: about?.redis?.error)
    }
}

// ─── Shared Utilities ────────────────────────────────────────

@Composable
private fun FormScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹", modifier = Modifier.clickable { onBack() }.padding(end = 16.dp), fontSize = 34.sp, color = IslandGreen700)
            Text(title, fontSize = 32.sp, fontWeight = FontWeight.Black, color = IslandText)
        }
        content()
    }
}

@Composable
private fun IslandOutlinedField(value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = IslandMuted) }, minLines = minLines,
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IslandGreen500, unfocusedBorderColor = IslandLine,
            focusedContainerColor = Color.White.copy(alpha = 0.82f), unfocusedContainerColor = Color.White.copy(alpha = 0.82f)
        )
    )
}

@Composable
private fun ToggleRow(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
        border = BorderStroke(1.dp, IslandLine)
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, color = IslandText, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(desc, color = IslandMuted, fontSize = 14.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun AdminNewsRow(item: NewsDto, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.84f)),
        border = BorderStroke(1.dp, IslandLine)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title ?: "未命名新闻", fontWeight = FontWeight.Black, color = IslandText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.categoryName ?: "未分类"} · ${item.authorName ?: "未知作者"}", color = IslandMuted, fontSize = 13.sp)
            }
            Text("删除", modifier = Modifier.clickable(onClick = onDelete).padding(10.dp), color = Color(0xFFE15A4F), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusLine(name: String, ready: Boolean?, error: String?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.82f)),
        border = BorderStroke(1.dp, IslandLine)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = IslandText)
            Text(if (ready == false) (error ?: "异常") else "正常", color = if (ready == false) Color(0xFFE15A4F) else IslandGreen700)
        }
    }
}
