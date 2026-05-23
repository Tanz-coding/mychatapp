package com.example.mychatapp.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.data.model.NewsDto
import com.example.mychatapp.ui.components.Avatar
import com.example.mychatapp.ui.components.FilterTabs
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandGreen700
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

@Composable
fun NewsListScreen(
    news: List<NewsDto>,
    categories: List<String> = listOf("全部", "岛屿公告", "行业资讯", "技术分享"),
    onRefresh: () -> Unit = {},
    onNewsClick: (NewsDto) -> Unit = {},
    onSearch: (String) -> Unit = {}
) {
    var keyword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("新闻中心", fontSize = 34.sp, fontWeight = FontWeight.Black, color = IslandText)
        }
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索新闻或关键词", color = IslandMuted) },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslandGreen500,
                    unfocusedBorderColor = IslandLine,
                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                )
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { onSearch(keyword.trim()) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
            ) {
                Text("搜索")
            }
        }
        Spacer(Modifier.height(20.dp))
        FilterTabs(categories)
        Spacer(Modifier.height(18.dp))

        // Big hero card
        BigNewsCard()
        Spacer(Modifier.height(16.dp))

        if (news.isEmpty()) {
            NewsRow("探索深蓝：夜间模式全新上线", "更舒适的视觉体验，陪伴你的每一个夜晚", "3.2K", "夜") {}
            NewsRow("小岛新功能预告：个性化装扮系统", "打造属于你的独一无二的小岛", "2.1K", "岛") {}
            NewsRow("小岛生活指南：如何快速升级", "新手必看，轻松解锁更多小岛乐趣", "1.8K", "新") {}
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEAF4E8),
                    contentColor = IslandGreen700
                )
            ) {
                Text("刷新后端新闻")
            }
        } else {
            news.forEach { item ->
                NewsRow(
                    title = item.title ?: "未命名新闻",
                    desc = item.summary ?: item.content ?: "暂无摘要",
                    views = (item.viewCount ?: 0).toString(),
                    icon = item.coverUrl ?: "新"
                ) { onNewsClick(item) }
            }
        }
    }
}

@Composable
fun NewsDetailScreen(
    title: String,
    author: String,
    date: String,
    views: Int,
    content: String,
    comments: List<CommentItem> = emptyList(),
    onAddComment: (String) -> Unit = {}
) {
    var commentDraft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Black, color = IslandText)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(author.ifBlank { "新" }, 32.dp)
            Spacer(Modifier.width(8.dp))
            Text(author, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IslandGreen700)
            Spacer(Modifier.width(16.dp))
            Text(date, fontSize = 13.sp, color = IslandMuted)
            Spacer(Modifier.width(16.dp))
            Text("$views 阅读", fontSize = 13.sp, color = IslandMuted)
        }
        Spacer(Modifier.height(20.dp))
        Text(content, fontSize = 16.sp, color = IslandText, lineHeight = 26.sp)
        Spacer(Modifier.height(32.dp))

        // Comments
        Text("评论 (${comments.size})", fontSize = 20.sp, fontWeight = FontWeight.Black, color = IslandText)
        Spacer(Modifier.height(12.dp))

        comments.forEach { comment ->
            CommentRow(comment)
        }

        Spacer(Modifier.height(16.dp))
        // Comment input
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = commentDraft,
                onValueChange = { commentDraft = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                placeholder = { Text("说点什么吧...", color = Color(0xFF999999)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslandGreen500,
                    unfocusedBorderColor = IslandLine,
                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                )
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    if (commentDraft.isNotBlank()) {
                        onAddComment(commentDraft.trim())
                        commentDraft = ""
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
            ) {
                Text("发送")
            }
        }
    }
}

data class CommentItem(
    val id: String,
    val author: String,
    val text: String,
    val time: String
)

@Composable
private fun CommentRow(comment: CommentItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Avatar("评", 36.dp)
        Spacer(Modifier.width(10.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslandText)
                    Spacer(Modifier.width(10.dp))
                    Text(comment.time, fontSize = 12.sp, color = IslandMuted)
                }
                Spacer(Modifier.height(4.dp))
                Text(comment.text, fontSize = 15.sp, color = IslandText)
            }
        }
    }
}

@Composable
private fun BigNewsCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(210.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D6F66)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Background decoration
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFFDDF7FF), Color(0xFFEAF9D2), Color(0xFFFBE2A1))))
            )
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
fun NewsRow(
    title: String,
    desc: String,
    views: String,
    icon: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(icon, 78.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = IslandText)
            Spacer(Modifier.height(5.dp))
            Text(desc, fontSize = 14.sp, color = IslandMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("$views 阅", color = IslandMuted, fontSize = 13.sp)
    }
}
