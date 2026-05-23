package com.example.mychatapp.ui

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mychatapp.data.remote.ApiClient
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.ui.screens.*
import org.json.JSONObject

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val CONVERSATION = "conversation/{id}/{title}"
    const val NEWS_DETAIL = "news_detail/{newsId}"
    const val QR_SCAN = "qr_scan"
    const val PUBLISH_NEWS = "publish_news"
    const val SETTINGS = "settings"
    const val ADMIN = "admin"
    const val ABOUT = "about"

    fun conversation(id: String, title: String) = "conversation/${Uri.encode(id)}/${Uri.encode(title)}"
    fun newsDetail(newsId: Int) = "news_detail/$newsId"
}

@Composable
fun AppNavigation(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                state = state,
                onLogin = { account, password -> viewModel.signIn(account, password) },
                onDemo = { viewModel.enterDemo() },
                onRegister = { account, password, _ -> viewModel.register(account, password) },
                onScanQr = { navController.navigate(Routes.QR_SCAN) }
            )
            if (state.signedIn) {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.MAIN) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToConversation = { id, title ->
                    navController.navigate(Routes.conversation(id, title))
                },
                onNavigateToNewsDetail = { news ->
                    news.id?.let { id ->
                        viewModel.loadNewsDetail(id)
                        navController.navigate(Routes.newsDetail(id))
                    }
                },
                onNavigate = { route ->
                    when (route) {
                        "publish" -> navController.navigate(Routes.PUBLISH_NEWS)
                        "settings" -> navController.navigate(Routes.SETTINGS)
                        "admin" -> navController.navigate(Routes.ADMIN)
                        "about" -> navController.navigate(Routes.ABOUT)
                    }
                }
            )
        }

        composable(Routes.CONVERSATION) { backStackEntry ->
            val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "聊天")
            val sessionId = Uri.decode(backStackEntry.arguments?.getString("id") ?: "")
            ConversationScreen(
                title = title,
                messages = filterConversationMessages(state, sessionId).map { msg ->
                    ChatMessage(
                        id = msg.id,
                        author = msg.from.name,
                        text = msg.content,
                        time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.time)),
                        isMine = msg.from.id == state.userId?.toString() || msg.from.username == state.userName,
                        type = msg.type
                    )
                },
                onSend = { text ->
                    viewModel.sendConversationMessage(sessionId, text)
                }
            )
        }

        composable(Routes.NEWS_DETAIL) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")?.toIntOrNull() ?: 0
            LaunchedEffect(newsId) {
                viewModel.loadNewsDetail(newsId)
            }
            val newsItem = state.selectedNews ?: state.news.find { it.id == newsId }
            NewsDetailScreen(
                title = newsItem?.title ?: "新闻详情",
                author = newsItem?.authorName ?: "",
                date = newsItem?.createdAt ?: "",
                views = newsItem?.viewCount ?: 0,
                content = newsItem?.content ?: "加载中...",
                comments = state.comments.map {
                    CommentItem(
                        id = (it.id ?: 0).toString(),
                        author = it.authorName ?: "小岛居民",
                        text = it.content ?: "",
                        time = it.createdAt ?: ""
                    )
                },
                onAddComment = { viewModel.addComment(newsId, it) }
            )
        }

        composable(Routes.PUBLISH_NEWS) {
            PublishNewsScreen(
                categories = viewModel.categoryNames().filter { it != "全部" },
                loading = state.loading,
                onPublish = { title, summary, content, category ->
                    viewModel.publishNews(title, summary, content, category)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                state = state,
                onSave = { viewModel.saveSettings(it) },
                onBack = { navController.popBackStack() },
                onChangePassword = { oldPwd, newPwd -> viewModel.changePassword(oldPwd, newPwd) },
                onClearCache = { viewModel.clearCache() }
            )
        }

        composable(Routes.ADMIN) {
            LaunchedEffect(Unit) { viewModel.loadAdminData() }
            AdminScreen(
                state = state,
                onRefresh = {
                    viewModel.loadNews()
                    viewModel.loadAdminData()
                },
                onDeleteNews = { viewModel.deleteNews(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            LaunchedEffect(Unit) {
                viewModel.loadAbout()
                viewModel.loadHealth()
            }
            AboutScreen(
                state = state,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QR_SCAN) {
            val context = LocalContext.current
            QrScanScreen(
                onQrScanned = { result ->
                    try {
                        val json = JSONObject(result)
                        val host = json.optString("host", "")
                        val action = json.optString("action", "")
                        val sessionId = json.optString("sessionId", "")

                        // Always configure host if present
                        if (host.isNotBlank()) {
                            ApiConfig.setBaseUrl(context, host)
                            ApiClient.reinit(context, host)
                            viewModel.socketManager.updateSocketUrl(ApiConfig.getSocketUrl(context))
                            Toast.makeText(context, "已连接到 $host", Toast.LENGTH_SHORT).show()
                        }

                        // If this is a QR login action, confirm the session
                        if (action == "qr_login" && sessionId.isNotBlank()) {
                            viewModel.confirmQrLogin(sessionId)
                            Toast.makeText(context, "登录确认已发送", Toast.LENGTH_SHORT).show()
                        }
                    } catch (_: Exception) {
                        if (result.startsWith("http")) {
                            ApiConfig.setBaseUrl(context, result)
                            ApiClient.reinit(context, result)
                            viewModel.socketManager.updateSocketUrl(ApiConfig.getSocketUrl(context))
                            Toast.makeText(context, "已连接到 $result", Toast.LENGTH_SHORT).show()
                        }
                    }
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}

private fun filterConversationMessages(state: AppUiState, sessionId: String) =
    if (sessionId == "group_001") {
        state.chatMessages.filter { it.to.id == "group_001" || it.to.type == "group" }
    } else {
        val userId = sessionId.removePrefix("user:")
        state.chatMessages.filter { it.from.id == userId || it.to.id == userId }
    }
