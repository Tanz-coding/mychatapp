package com.example.mychatapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.ui.components.Avatar
import com.example.mychatapp.ui.components.FilterTabs
import com.example.mychatapp.ui.components.InputLike
import com.example.mychatapp.ui.components.IslandLandscapeStrip
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandGreen700
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

data class ConversationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val badge: String,
    val icon: String
)

@Composable
fun ChatListScreen(
    sessions: List<ConversationItem> = defaultSessions,
    onSessionClick: (ConversationItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        TopTitle("会话")
        Spacer(Modifier.height(22.dp))
        InputLike(leading = "⌕", text = "搜索会话或联系人")
        Spacer(Modifier.height(20.dp))
        IslandLandscapeStrip()
        Spacer(Modifier.height(18.dp))
        FilterTabs(listOf("全部", "未读", "群聊", "联系人"))
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            sessions.forEach { session ->
                ConversationRow(
                    title = session.title,
                    message = session.message,
                    time = session.time,
                    badge = session.badge,
                    icon = session.icon,
                    onClick = { onSessionClick(session) }
                )
            }
        }
    }
}

@Composable
fun ConversationScreen(
    title: String,
    messages: List<ChatMessage> = emptyList(),
    onSend: (String) -> Unit = {}
) {
    var draft by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 42.dp)) {
        TopTitle(title)
        Spacer(Modifier.height(20.dp))

        // Message list
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            messages.forEach { msg ->
                MessageBubble(msg)
            }
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("暂无消息，发送第一条消息吧", color = IslandMuted)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.76f)
            ) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    if (draft.isEmpty()) {
                        Text("说点什么吧...", color = Color(0xFF999999), fontSize = 16.sp)
                    }
                    // Using a simple approach — TextField without Material3 wrapper for cleaner look
                    androidx.compose.foundation.text.BasicTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = IslandText)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSend(draft.trim())
                        draft = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
            ) {
                Text("发送", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class ChatMessage(
    val id: String,
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean = false,
    val type: String = "text" // text, image, file
)

@Composable
fun MessageBubble(msg: ChatMessage) {
    val context = LocalContext.current
    val mediaUrl = ApiConfig.resolveAssetUrl(context, msg.text)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!msg.isMine) {
            Avatar("友", 40.dp)
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (msg.isMine) 18.dp else 4.dp,
                bottomEnd = if (msg.isMine) 4.dp else 18.dp
            ),
            color = if (msg.isMine) IslandGreen500 else Color.White.copy(alpha = 0.92f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!msg.isMine) {
                    Text(msg.author, fontSize = 12.sp, color = IslandGreen700, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                }
                if (msg.type == "image" && mediaUrl != null) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        msg.text,
                        fontSize = 16.sp,
                        color = if (msg.isMine) Color.White else IslandText
                    )
                }
                Text(
                    msg.time,
                    fontSize = 11.sp,
                    color = if (msg.isMine) Color.White.copy(alpha = 0.7f) else IslandMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
        if (msg.isMine) {
            Spacer(Modifier.width(8.dp))
            Avatar("我", 40.dp)
        }
    }
}

@Composable
private fun TopTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 34.sp, fontWeight = FontWeight.Black, color = IslandText)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(IslandGreen500, IslandGreen700))),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun ConversationRow(
    title: String,
    message: String,
    time: String,
    badge: String,
    icon: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 11.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(icon, 64.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 21.sp, fontWeight = FontWeight.Black, color = IslandText, maxLines = 1)
                Spacer(Modifier.height(5.dp))
                Text(message, fontSize = 15.sp, color = IslandMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(time, color = IslandMuted, fontSize = 14.sp)
                if (badge.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(IslandGreen500)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(badge, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private val defaultSessions = listOf(
    ConversationItem("1", "群聊天室", "小岛居民：今天的日落也太美了吧", "10:30", "12", "群"),
    ConversationItem("2", "设计小分队", "阿布：新方案的海报我来发一下", "09:48", "5", "设"),
    ConversationItem("3", "产品讨论组", "小新：这里的交互细节很棒", "昨天", "3", "产"),
    ConversationItem("4", "小岛生活频道", "周末一起去浮潜", "昨天", "8", "岛"),
    ConversationItem("5", "AI 助手", "你好！我是你的智能小助手", "09:15", "", "AI"),
    ConversationItem("6", "系统通知", "你的账号在新设备登录", "07-02", "", "通")
)
