package com.example.mychatapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.ui.AppUiState
import com.example.mychatapp.ui.components.Avatar
import com.example.mychatapp.ui.components.IslandLandscapeStrip
import com.example.mychatapp.ui.components.ServerConfigDialog
import com.example.mychatapp.ui.components.SettingRow
import com.example.mychatapp.ui.components.StatItem
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

@Composable
fun MineScreen(
    state: AppUiState,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit = {},
    serverUrl: String = "",
    onServerUrlChange: (String) -> Unit = {}
) {
    var showServerConfig by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        // Profile header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(state.userAvatar.ifBlank { "我" }, 92.dp)
            Spacer(Modifier.width(18.dp))
            Column {
                Text(state.userName, fontSize = 30.sp, fontWeight = FontWeight.Black, color = IslandText)
                Text("ID: ${state.userId ?: "--"} · ${if (state.socketConnected) "在线" else "离线"}", fontSize = 18.sp, color = IslandMuted)
                Text(if (state.userRole == "admin") "管理员" else "普通用户", fontSize = 14.sp, color = IslandGreen500, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        IslandLandscapeStrip()
        Spacer(Modifier.height(18.dp))

        // Stats
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
            border = BorderStroke(1.dp, IslandLine),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(state.friends.count { it.status == "accepted" }.toString(), "好友")
                StatItem(state.onlineUsers.count { it.type == "user" }.toString(), "在线")
                StatItem((state.about?.counts?.news ?: state.news.size).toString(), "新闻")
                StatItem((state.about?.counts?.comments ?: 0).toString(), "评论")
            }
        }
        Spacer(Modifier.height(18.dp))

        // Settings items
        SettingRow("发", "发布新闻") { onNavigate("publish") }
        if (state.userRole == "admin") {
            SettingRow("管", "管理后台") { onNavigate("admin") }
        }
        SettingRow("设", "个人设置") { onNavigate("settings") }
        SettingRow("服", "服务器地址") { showServerConfig = true }
        SettingRow("关", "关于 Q信") { onNavigate("about") }
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
        ) {
            Text("退出登录", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showServerConfig) {
        ServerConfigDialog(
            currentUrl = serverUrl,
            onDismiss = { showServerConfig = false },
            onSave = { url ->
                onServerUrlChange(url)
                showServerConfig = false
            }
        )
    }
}
