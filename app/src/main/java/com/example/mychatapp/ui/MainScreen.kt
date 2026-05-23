package com.example.mychatapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.data.model.NewsDto
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.data.socket.SocketUser
import com.example.mychatapp.ui.components.SkyDecoration
import com.example.mychatapp.ui.screens.AiScreen
import com.example.mychatapp.ui.screens.ChatListScreen
import com.example.mychatapp.ui.screens.ConversationItem
import com.example.mychatapp.ui.screens.MineScreen
import com.example.mychatapp.ui.screens.NewsListScreen
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

enum class TabItem(val title: String, val icon: String) {
    Chat("会话", "聊"),
    News("新闻", "▤"),
    Ai("AI 助手", "AI"),
    Mine("我的", "我")
}

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    onNavigateToConversation: (String, String) -> Unit = { _, _ -> },
    onNavigateToNewsDetail: (NewsDto) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var tab by remember { mutableStateOf(TabItem.Chat) }

    // Build session list from online users + friends
    val sessions = remember(state.onlineUsers, state.friends) {
        buildSessions(state.onlineUsers, state.friends)
    }

    Scaffold(
        bottomBar = {
            IslandBottomBar(current = tab, onChange = { tab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFDF8ED), Color(0xFFFFFBF3), Color(0xFFF8EFD9))
                    )
                )
        ) {
            SkyDecoration()
            when (tab) {
                TabItem.Chat -> ChatListScreen(
                    sessions = sessions,
                    onSessionClick = { session ->
                        onNavigateToConversation(session.id, session.title)
                    }
                )
                TabItem.News -> NewsListScreen(
                    news = state.news,
                    categories = viewModel.categoryNames(),
                    onRefresh = { viewModel.loadNews() },
                    onSearch = { viewModel.searchNews(it) },
                    onNewsClick = { news -> onNavigateToNewsDetail(news) }
                )
                TabItem.Ai -> AiScreen(
                    state = state,
                    onAsk = { viewModel.askAssistant(it) }
                )
                TabItem.Mine -> MineScreen(
                    state = state,
                    onLogout = { viewModel.signOut() },
                    onNavigate = onNavigate,
                    serverUrl = ApiConfig.getBaseUrl(context),
                    onServerUrlChange = {
                        viewModel.reconnectSocket()
                        viewModel.loadNews()
                    }
                )
            }
        }
    }
}

private fun buildSessions(onlineUsers: List<SocketUser>, friends: List<com.example.mychatapp.data.model.FriendDto>): List<ConversationItem> {
    val sessions = mutableListOf<ConversationItem>()

    // Group chat is always first
    sessions.add(ConversationItem("group_001", "群聊天室", "小岛居民：来聊天吧", "", "", "群"))

    // Add friends
    friends.filter { it.status == "accepted" }.forEach { friend ->
        sessions.add(
            ConversationItem(
                id = "user:${friend.id}",
                title = friend.name ?: friend.username ?: "好友",
                message = if (friend.status == "sent") "好友申请中" else "",
                time = "",
                badge = "",
                icon = "友"
            )
        )
    }

    // Add other online users
    onlineUsers.filter { it.type == "user" }.take(10).forEach { user ->
        if (sessions.none { it.id == "user:${user.id}" }) {
            sessions.add(
                ConversationItem(
                    id = "user:${user.id}",
                    title = user.name,
                    message = "在线",
                    time = "",
                    badge = "",
                    icon = "在"
                )
            )
        }
    }

    return sessions
}

@Composable
fun IslandBottomBar(current: TabItem, onChange: (TabItem) -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, IslandLine),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(78.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem.entries.forEach { item ->
                Column(
                    modifier = Modifier.clickable { onChange(item) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.icon, fontSize = 26.sp, color = if (item == current) IslandGreen500 else IslandMuted)
                    Text(item.title, fontSize = 13.sp, color = if (item == current) IslandGreen500 else IslandMuted, fontWeight = if (item == current) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
