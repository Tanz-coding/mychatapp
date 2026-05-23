package com.example.mychatapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.ui.AppUiState
import com.example.mychatapp.ui.components.IslandSceneCard
import com.example.mychatapp.ui.components.SkyDecoration
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandGreen700
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

private val Ink = Color(0xFF101D26)

enum class LoginTab { Account, Scan }

@Composable
fun LoginScreen(
    state: AppUiState,
    onLogin: (String, String) -> Unit,
    onDemo: () -> Unit,
    onRegister: (String, String, String) -> Unit = { _, _, _ -> },
    onScanQr: () -> Unit = {}
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFDF8ED), Color(0xFFFFFBF3), Color(0xFFF8EFD9))
                )
            )
    ) {
        SkyDecoration()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 34.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Column {
                Text("Q信", fontSize = 52.sp, fontWeight = FontWeight.Black, color = IslandGreen500)
                Spacer(Modifier.height(22.dp))
                Text("欢迎回来", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(Modifier.height(10.dp))
                Text("登录后与小岛居民一起，开启美好生活", fontSize = 16.sp, color = Color(0xFF5D6468))
            }

            IslandHeroCard("治愈小岛", "聊天、新闻与 AI 助手都在这里", 250.dp)

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                border = BorderStroke(1.dp, IslandLine),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TabChip("登录", selected = tab == 0) { tab = 0 }
                        TabChip("注册", selected = tab == 1) { tab = 1 }
                    }

                    if (tab == 0) {
                        IslandTextField(value = account, onValueChange = { account = it }, label = "请输入手机号或邮箱", leading = "号")
                        IslandTextField(value = password, onValueChange = { password = it }, label = "请输入密码", leading = "密", password = true)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("○  记住密码", color = IslandMuted, fontSize = 14.sp)
                            Text("忘记密码？", color = IslandGreen700, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (!state.error.isNullOrBlank()) {
                            Text(state.error, color = Color(0xFFE15A4F), fontSize = 13.sp)
                        }
                        Button(
                            onClick = { onLogin(account.trim(), password) },
                            modifier = Modifier.fillMaxWidth().height(58.dp),
                            enabled = !state.loading,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
                        ) {
                            Text(if (state.loading) "登录中..." else "登录", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onScanQr,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF4E8), contentColor = IslandGreen700)
                        ) {
                            Text("扫码配置服务器", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onDemo,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF4E8), contentColor = IslandGreen700)
                        ) {
                            Text("先体验演示版", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        RegisterFields(
                            account = account,
                            onAccountChange = { account = it },
                            onRegister = onRegister
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterFields(
    account: String,
    onAccountChange: (String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var regPassword by remember { mutableStateOf("") }
    var regConfirm by remember { mutableStateOf("") }
    var regError by remember { mutableStateOf<String?>(null) }

    IslandTextField(value = account, onValueChange = onAccountChange, label = "请输入账号（手机号/邮箱/Q号）", leading = "号")
    IslandTextField(value = regPassword, onValueChange = { regPassword = it }, label = "设置登录密码", leading = "密", password = true)
    IslandTextField(value = regConfirm, onValueChange = { regConfirm = it }, label = "再次输入密码", leading = "密", password = true)
    if (regError != null) {
        Text(regError!!, color = Color(0xFFE15A4F), fontSize = 13.sp)
    }
    Button(
        onClick = {
            regError = null
            when {
                account.isBlank() -> regError = "请输入账号"
                regPassword.isBlank() -> regError = "请输入密码"
                regPassword != regConfirm -> regError = "两次密码不一致"
                regPassword.length < 3 -> regError = "密码至少3位"
                else -> onRegister(account.trim(), regPassword, regConfirm)
            }
        },
        modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
    ) {
        Text("注册", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) IslandGreen500 else Color(0xFFEAF4E8),
            contentColor = if (selected) Color.White else IslandGreen700
        )
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun IslandTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leading: String,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        leadingIcon = { Text(leading, fontSize = 20.sp) },
        placeholder = { Text(label, color = Color(0xFF999999)) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = IslandGreen500,
            unfocusedBorderColor = IslandLine,
            focusedContainerColor = Color.White.copy(alpha = 0.8f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
        )
    )
}

@Composable
fun IslandHeroCard(title: String, subtitle: String, height: Dp) {
    IslandSceneCard(
        modifier = Modifier.fillMaxWidth().height(height),
        title = title,
        subtitle = subtitle
    )
}
