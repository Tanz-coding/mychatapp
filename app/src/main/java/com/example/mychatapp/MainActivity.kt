package com.example.mychatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.ui.theme.MychatappTheme

private val Ink = Color(0xFF101D26)
private val Muted = Color(0xFF7D8589)
private val IslandGreen = Color(0xFF4AA060)
private val DeepGreen = Color(0xFF2F7D4D)
private val Line = Color(0xFFEDE2CF)
private val SoftBlue = Color(0xFFDDF7FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MychatappTheme {
                QChatIslandApp()
            }
        }
    }
}

private enum class TabItem(val title: String, val icon: String) {
    Chat("会话", "☁"),
    News("新闻", "▤"),
    Ai("AI 助手", "◉"),
    Mine("我的", "♙")
}

@Composable
fun QChatIslandApp() {
    var loggedIn by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(TabItem.Chat) }

    if (!loggedIn) {
        LoginScreen(onLogin = { loggedIn = true })
        return
    }

    Scaffold(
        bottomBar = {
            IslandBottomBar(current = tab, onChange = { tab = it })
        }
    ) { innerPadding ->
        IslandPageFrame(modifier = Modifier.padding(innerPadding)) {
            when (tab) {
                TabItem.Chat -> ChatListScreen()
                TabItem.News -> NewsScreen()
                TabItem.Ai -> AiScreen()
                TabItem.Mine -> MineScreen(onLogout = { loggedIn = false })
            }
        }
    }
}

@Composable
private fun IslandPageFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFDF8ED), Color(0xFFFFFBF3), Color(0xFFF8EFD9))
                )
            )
    ) {
        IslandSkyDecoration()
        content()
    }
}

@Composable
private fun LoginScreen(onLogin: () -> Unit) {
    IslandPageFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 56.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Q信", fontSize = 52.sp, fontWeight = FontWeight.Black, color = IslandGreen)
                Spacer(Modifier.height(22.dp))
                Text("欢迎回来", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(Modifier.height(10.dp))
                Text("登录后与小岛居民一起，开启美好生活", fontSize = 16.sp, color = Color(0xFF5D6468))
            }

            IslandHeroCard(
                title = "治愈小岛",
                subtitle = "聊天、新闻与 AI 助手都在这里",
                height = 250.dp
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                border = BorderStroke(1.dp, Line),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InputLike("👤", "请输入手机号或邮箱")
                    InputLike("🔒", "请输入密码")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("○  记住密码", color = Muted, fontSize = 14.sp)
                        Text("忘记密码？", color = DeepGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = onLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslandGreen)
                    ) {
                        Text("登录", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("还没有账号？", color = Muted)
                        Text("  立即注册", color = DeepGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IslandHeroCard(title: String, subtitle: String, height: androidx.compose.ui.unit.Dp) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = SoftBlue.copy(alpha = 0.78f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFDDF7FF), Color(0xFFEAF9D2), Color(0xFFFBE2A1))
                    )
                )
                .padding(22.dp)
        ) {
            Text("☁", modifier = Modifier.align(Alignment.TopEnd), fontSize = 56.sp, color = Color.White.copy(alpha = 0.85f))
            Text("🌴", modifier = Modifier.align(Alignment.BottomStart), fontSize = 58.sp)
            Text("🏡", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp), fontSize = 46.sp)
            Text("🌊", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp), fontSize = 42.sp)
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text(subtitle, fontSize = 16.sp, color = Color(0xFF4F6668))
            }
            Text("🤖", modifier = Modifier.align(Alignment.Center), fontSize = 82.sp)
        }
    }
}

@Composable
private fun ChatListScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        TopTitle("会话", action = "+")
        Spacer(Modifier.height(22.dp))
        SearchBar("搜索会话或联系人")
        Spacer(Modifier.height(20.dp))
        IslandLandscapeStrip()
        Spacer(Modifier.height(18.dp))
        FilterTabs(listOf("全部", "未读", "群聊", "联系人"))
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ConversationRow("群聊天室", "小岛居民：今天的日落也太美了吧～", "10:30", "12", "👥")
            ConversationRow("设计小分队", "阿布：新方案的海报我来发一下", "09:48", "5", "🎨")
            ConversationRow("产品讨论组", "小新：这里的交互细节很棒！", "昨天", "3", "🧑")
            ConversationRow("小岛生活频道", "椰子树：周末一起去浮潜呀～", "昨天", "8", "🌴")
            ConversationRow("AI 助手", "你好！我是你的智能小助手", "09:15", "", "🤖")
            ConversationRow("系统通知", "你的账号在新设备登录", "07-02", "", "🔔")
        }
    }
}

@Composable
private fun NewsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        TopTitle("新闻中心")
        Spacer(Modifier.height(22.dp))
        SearchBar("搜索新闻或关键词")
        Spacer(Modifier.height(20.dp))
        FilterTabs(listOf("全部", "岛屿公告", "行业资讯", "技术分享"))
        Spacer(Modifier.height(18.dp))
        BigNewsCard()
        Spacer(Modifier.height(16.dp))
        NewsRow("探索深蓝：夜间模式全新上线", "更舒适的视觉体验，陪伴你的每一个夜晚", "3.2K", "🌙")
        NewsRow("小岛新功能预告：个性化装扮系统", "打造属于你的独一无二的小岛", "2.1K", "🏞")
        NewsRow("小岛生活指南：如何快速升级", "新手必看，轻松解锁更多小岛乐趣", "1.8K", "🌱")
        NewsRow("用户故事：我的小岛日常", "听听大家在小岛上的温暖故事", "986", "🏠")
    }
}

@Composable
private fun AiScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        TopTitle("AI 助手")
        Spacer(Modifier.height(26.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SoftBlue.copy(alpha = 0.72f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("你好！我是小岛 AI 助手", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(Modifier.height(10.dp))
                Text("有什么可以帮你的吗？", fontSize = 18.sp, color = Ink)
                Spacer(Modifier.height(22.dp))
                IslandIllustration(height = 220.dp)
            }
        }
        Spacer(Modifier.height(22.dp))
        AiQuestion("✦", "Q信 2.0 有哪些新功能？")
        AiQuestion("👥", "如何创建一个群聊？")
        AiQuestion("🌴", "小岛生活有什么有趣的活动？")
        AiQuestion("🔔", "如何设置消息提醒？")
        Spacer(Modifier.height(16.dp))
        InputLike("", "输入你的问题…", trailing = "➤")
    }
}

@Composable
private fun MineScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar("👒", 92.dp)
            Spacer(Modifier.width(18.dp))
            Column {
                Text("小岛居民", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink)
                Text("ID: islander_001", fontSize = 18.sp, color = Muted)
            }
        }
        Spacer(Modifier.height(20.dp))
        IslandLandscapeStrip()
        Spacer(Modifier.height(18.dp))
        StatsCard()
        Spacer(Modifier.height(18.dp))
        SettingItem("♙", "个人资料")
        SettingItem("🔔", "消息通知")
        SettingItem("🛡", "隐私设置")
        SettingItem("⚙", "通用设置")
        SettingItem("?", "帮助与反馈")
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IslandGreen)
        ) {
            Text("退出登录", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IslandBottomBar(current: TabItem, onChange: (TabItem) -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem.entries.forEach { item ->
                Column(
                    modifier = Modifier.clickable { onChange(item) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.icon, fontSize = 26.sp, color = if (item == current) IslandGreen else Muted)
                    Text(item.title, fontSize = 13.sp, color = if (item == current) IslandGreen else Muted, fontWeight = if (item == current) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun TopTitle(title: String, action: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Ink)
        if (action != null) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(IslandGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(action, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}

@Composable
private fun SearchBar(text: String) {
    InputLike("⌕", text)
}

@Composable
private fun InputLike(leading: String, text: String, trailing: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading.isNotEmpty()) Text(leading, fontSize = 22.sp, color = IslandGreen)
        if (leading.isNotEmpty()) Spacer(Modifier.width(12.dp))
        Text(text, color = Color(0xFF999999), fontSize = 17.sp, modifier = Modifier.weight(1f))
        if (trailing.isNotEmpty()) Text(trailing, color = IslandGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FilterTabs(tabs: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
        tabs.forEachIndexed { index, text ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text, color = if (index == 0) DeepGreen else Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (index == 0) Box(Modifier.padding(top = 8.dp).size(width = 42.dp, height = 4.dp).clip(CircleShape).background(IslandGreen))
            }
        }
    }
}

@Composable
private fun ConversationRow(title: String, message: String, time: String, badge: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(icon, 64.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Ink, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            Text(message, fontSize = 15.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time, color = Muted, fontSize = 14.sp)
            if (badge.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.clip(CircleShape).background(IslandGreen).padding(horizontal = 9.dp, vertical = 4.dp)) {
                    Text(badge, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Avatar(icon: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFD5F4FF), Color(0xFFFFF1CB)))),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = (size.value * 0.42).sp)
    }
}

@Composable
private fun IslandLandscapeStrip() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SoftBlue.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Text("☁", modifier = Modifier.align(Alignment.TopEnd).padding(14.dp), fontSize = 42.sp, color = Color.White)
            Text("🌴  🌊  🏡  🌿", modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp), fontSize = 36.sp)
            Text("小岛生活", modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), color = DeepGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IslandIllustration(height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFDDF7FF), Color(0xFFEAF9D2), Color(0xFFFBE2A1))))
    ) {
        Text("☁", modifier = Modifier.align(Alignment.TopEnd).padding(18.dp), fontSize = 54.sp, color = Color.White)
        Text("🌴", modifier = Modifier.align(Alignment.BottomStart).padding(20.dp), fontSize = 52.sp)
        Text("🤖", modifier = Modifier.align(Alignment.Center), fontSize = 88.sp)
        Text("🌼  🌊  🌼", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp), fontSize = 24.sp)
    }
}

@Composable
private fun AiQuestion(icon: String, text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 22.sp, color = IslandGreen)
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 17.sp, color = Ink, modifier = Modifier.weight(1f))
            Text("›", fontSize = 30.sp, color = Muted)
        }
    }
}

@Composable
private fun BigNewsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D6F66)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            IslandIllustration(height = 210.dp)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA063B38)))))
            Text(
                "Q信 2.0 版本正式发布，\n带来全新小岛体验",
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun NewsRow(title: String, desc: String, views: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(icon, 78.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
            Spacer(Modifier.height(5.dp))
            Text(desc, fontSize = 14.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("👁 $views", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun StatsCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Stat("128", "好友")
            Stat("12", "群聊")
            Stat("56", "收藏")
            Stat("280", "积分")
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
        Text(label, fontSize = 14.sp, color = Muted)
    }
}

@Composable
private fun SettingItem(icon: String, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp, color = Muted)
        Spacer(Modifier.width(18.dp))
        Text(title, fontSize = 20.sp, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("›", fontSize = 30.sp, color = Muted)
    }
}

@Composable
private fun IslandSkyDecoration() {
    Text("☁", modifier = Modifier.padding(start = 235.dp, top = 64.dp), fontSize = 88.sp, color = Color.White.copy(alpha = 0.35f))
    Text("☁", modifier = Modifier.padding(start = 300.dp, top = 108.dp), fontSize = 52.sp, color = Color.White.copy(alpha = 0.38f))
}

@Preview(showBackground = true)
@Composable
private fun QChatIslandPreview() {
    MychatappTheme {
        QChatIslandApp()
    }
}
