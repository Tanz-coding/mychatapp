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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.mychatapp.ui.components.IslandSceneCard
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

@Composable
fun AiScreen(
    state: AppUiState,
    onAsk: (String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateOf(listOf<Pair<String, String>>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 42.dp)
    ) {
        Text("AI 助手", fontSize = 34.sp, fontWeight = FontWeight.Black, color = IslandText)
        Spacer(Modifier.height(26.dp))

        // Latest answer card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF7FF).copy(alpha = 0.72f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("你好！我是小岛 AI 助手", fontSize = 26.sp, fontWeight = FontWeight.Black, color = IslandText)
                Spacer(Modifier.height(10.dp))
                if (state.loading) {
                    CircularProgressIndicator(color = IslandGreen500)
                } else {
                    Text(state.aiAnswer, fontSize = 18.sp, color = IslandText)
                }
                Spacer(Modifier.height(22.dp))
                IslandSceneCard(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    title = "小岛 AI",
                    subtitle = "结合新闻、聊天和系统状态回答",
                    compact = false
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // Chat history
        chatHistory.value.forEach { (q, a) ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Card(
                        shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                        colors = CardDefaults.cardColors(containerColor = IslandGreen500)
                    ) {
                        Text(q, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 15.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Avatar("🧑", 32.dp)
                    Spacer(Modifier.width(8.dp))
                    Card(
                        shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                    ) {
                        Text(a, modifier = Modifier.padding(12.dp), color = IslandText, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Quick question chips — with plenty of space above input
        Text("快捷提问", fontSize = 18.sp, fontWeight = FontWeight.Black, color = IslandGreen700)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = false, onClick = { onAsk("Q信 2.0 有哪些新功能？") },
                label = { Text("最新功能", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, IslandLine),
                shape = RoundedCornerShape(16.dp)
            )
            FilterChip(
                selected = false, onClick = { onAsk("如何创建一个群聊？") },
                label = { Text("创建群聊", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, IslandLine),
                shape = RoundedCornerShape(16.dp)
            )
            FilterChip(
                selected = false, onClick = { onAsk("小岛生活有什么有趣的活动？") },
                label = { Text("小岛生活", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, IslandLine),
                shape = RoundedCornerShape(16.dp)
            )
            FilterChip(
                selected = false, onClick = { onAsk("总结最近的新闻动态") },
                label = { Text("总结动态", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, IslandLine),
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // Input area — input and send button same height
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(Color.White.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = IslandText),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (question.isEmpty()) {
                        Text("输入你的问题…", color = Color(0xFF999999), fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        onAsk(question)
                        question = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
            ) {
                Text("发送", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
