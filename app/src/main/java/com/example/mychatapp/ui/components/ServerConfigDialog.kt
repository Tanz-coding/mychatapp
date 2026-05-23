package com.example.mychatapp.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mychatapp.data.remote.ApiClient
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandLine
import com.example.mychatapp.ui.theme.IslandText

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White.copy(alpha = 0.95f),
        title = {
            Text("服务器地址", fontWeight = FontWeight.Black, color = IslandText, fontSize = 22.sp)
        },
        text = {
            Column {
                Text("设置后端服务器地址，用于聊天、新闻和 AI 功能", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如: http://192.168.1.100:3000") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslandGreen500,
                        unfocusedBorderColor = IslandLine
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalized = url.trim().trimEnd('/') + "/"
                    ApiConfig.setBaseUrl(context, normalized)
                    ApiClient.reinit(context, normalized)
                    onSave(normalized)
                    Toast.makeText(context, "服务器地址已更新", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IslandGreen500)
            ) {
                Text("保存", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = IslandGreen500)
            }
        }
    )
}
