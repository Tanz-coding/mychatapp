package com.example.mychatapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychatapp.data.model.*
import com.example.mychatapp.data.remote.ApiClient
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.data.remote.RuntimeSession
import com.example.mychatapp.data.remote.TokenStore
import com.example.mychatapp.data.socket.ChatSocketManager
import com.example.mychatapp.data.socket.SocketEvent
import com.example.mychatapp.data.socket.SocketMessage
import com.example.mychatapp.data.socket.SocketUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val signedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val userId: Int? = null,
    val userName: String = "小岛居民",
    val userRole: String = "user",
    val userAvatar: String = "",
    val token: String? = null,
    val currentUser: SocketUser? = null,
    val news: List<NewsDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val selectedNews: NewsDto? = null,
    val comments: List<CommentDto> = emptyList(),
    val aiAnswer: String = "你好！我是你的智能小助手",
    val socketConnected: Boolean = false,
    val onlineUsers: List<SocketUser> = emptyList(),
    val chatMessages: List<SocketMessage> = emptyList(),
    val friends: List<FriendDto> = emptyList(),
    val settings: SettingsDto? = null,
    val aiConfig: AiConfigResponse? = null,
    val about: AboutResponse? = null,
    val health: HealthResponse? = null,
    val adminStats: NewsAdminStatsResponse? = null,
    val auditLogs: List<AuditLogDto> = emptyList()
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    private val tokenStore = TokenStore(application)
    val socketManager = ChatSocketManager()
    private val fallbackCategories = listOf("全部", "推荐", "社区动态", "活动聚会", "科技趋势", "生活指南", "人物故事", "通知公告")

    init {
        // Configure socket URL from saved config
        socketManager.updateSocketUrl(ApiConfig.getSocketUrl(getApplication()))

        // Collect socket events
        viewModelScope.launch {
            socketManager.events.collect { event ->
                handleSocketEvent(event)
            }
        }
        viewModelScope.launch {
            socketManager.connected.collect { connected ->
                _uiState.update { it.copy(socketConnected = connected) }
            }
        }
        viewModelScope.launch {
            socketManager.onlineUsers.collect { users ->
                _uiState.update { it.copy(onlineUsers = users) }
            }
        }

        // Auto-login
        autoLogin()
    }

    private fun autoLogin() {
        val savedToken = tokenStore.getToken()
        if (savedToken.isNullOrBlank()) return
        viewModelScope.launch {
            runCatching {
                RuntimeSession.set(savedToken, null)
                ApiClient.api.getMe()
            }.onSuccess { response ->
                val user = response.user ?: return@onSuccess
                RuntimeSession.set(response.token ?: savedToken, user.username)
                _uiState.update {
                    it.copy(
                        signedIn = true,
                        userId = user.id,
                        userName = user.username ?: "小岛居民",
                        userRole = user.role ?: "user",
                        userAvatar = user.avatarUrl ?: "",
                        token = response.token ?: savedToken
                    )
                }
                socketManager.connect(response.token ?: savedToken)
                loadAppData(user.role)
            }.onFailure {
                RuntimeSession.reset()
                tokenStore.clear()
            }
        }
    }

    private fun handleSocketEvent(event: SocketEvent) {
        when (event) {
            is SocketEvent.LoginSuccess -> {
                tokenStore.saveToken(event.data.token)
                RuntimeSession.set(event.data.token, event.data.user.name)
                _uiState.update {
                    it.copy(
                        signedIn = true,
                        error = null,
                        userId = event.data.user.id.toIntOrNull(),
                        userName = event.data.user.name,
                        userRole = event.data.user.role.ifBlank { "user" },
                        userAvatar = event.data.user.avatarUrl,
                        currentUser = event.data.user,
                        token = event.data.token,
                        friends = mergeFriends(it.friends, event.data.friends)
                    )
                }
                loadAppData(event.data.user.role)
            }
            is SocketEvent.LoginFail -> {
                _uiState.update { it.copy(error = event.message) }
            }
            is SocketEvent.Message -> {
                _uiState.update { state ->
                    state.copy(chatMessages = state.chatMessages + event.msg)
                }
            }
            is SocketEvent.HistoryMessages -> {
                _uiState.update { state ->
                    state.copy(chatMessages = event.messages)
                }
            }
            is SocketEvent.SystemEvent -> {
                // User joined/left — refresh online users
            }
            is SocketEvent.FriendRequest -> {
                // Refresh friends list
                loadFriends()
            }
            is SocketEvent.FriendRequestSent -> {
                loadFriends()
                _uiState.update { it.copy(notice = "好友申请已发送") }
            }
            is SocketEvent.FriendAccepted -> {
                loadFriends()
            }
            is SocketEvent.FriendDeleted -> {
                loadFriends()
            }
            is SocketEvent.FriendError -> {
                _uiState.update { it.copy(error = event.message) }
            }
            is SocketEvent.Error -> {
                _uiState.update { it.copy(error = event.message) }
            }
            else -> {}
        }
    }

    fun enterDemo() {
        _uiState.update { it.copy(signedIn = true, error = null, userName = "小岛游客") }
        loadNews()
        loadAbout()
    }

    fun signIn(account: String, password: String) {
        if (account.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "请输入账号和密码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                ApiClient.api.login(LoginRequest(account, password))
            }.onSuccess { response ->
                val token = response.token ?: ""
                RuntimeSession.set(token, response.user?.username ?: account)
                tokenStore.saveToken(token)
                _uiState.update {
                    it.copy(
                        signedIn = true,
                        loading = false,
                        userId = response.user?.id,
                        userName = response.user?.username ?: account,
                        userRole = response.user?.role ?: "user",
                        userAvatar = response.user?.avatarUrl ?: "",
                        token = token
                    )
                }
                socketManager.connect(token)
                loadAppData(response.user?.role)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = throwable.message ?: "登录失败，已保留演示入口"
                    )
                }
            }
        }
    }

    fun register(account: String, password: String) {
        if (account.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "请输入账号和密码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                ApiClient.api.register(RegisterRequest(account, password))
            }.onSuccess { response ->
                val token = response.token ?: ""
                RuntimeSession.set(token, response.user?.username ?: account)
                tokenStore.saveToken(token)
                _uiState.update {
                    it.copy(
                        signedIn = true,
                        loading = false,
                        userId = response.user?.id,
                        userName = response.user?.username ?: account,
                        userRole = response.user?.role ?: "user",
                        userAvatar = response.user?.avatarUrl ?: "",
                        token = token
                    )
                }
                socketManager.connect(token)
                loadAppData(response.user?.role)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = throwable.message ?: "注册失败，请重试"
                    )
                }
            }
        }
    }

    fun confirmQrLogin(sessionId: String) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.confirmQr(QrConfirmRequest(sessionId))
            }.onSuccess { response ->
                if (response.status == "confirmed") {
                    loadNews()
                }
            }.onFailure {
                _uiState.update { it.copy(error = "扫码确认失败：" + (it.message ?: "")) }
            }
        }
    }

    fun signOut() {
        socketManager.disconnect()
        RuntimeSession.reset()
        tokenStore.clear()
        _uiState.value = AppUiState()
    }

    fun reconnectSocket() {
        socketManager.disconnect()
        socketManager.updateSocketUrl(ApiConfig.getSocketUrl(getApplication()))
        _uiState.value.token?.let { token ->
            socketManager.connect(token)
        }
    }

    // --- Chat ---
    fun sendChatMessage(to: SocketUser, content: String, type: String = "text") {
        socketManager.sendMessage(_uiState.value.currentUser, to, content, type)
    }

    fun sendGroupMessage(content: String) {
        sendConversationMessage("group_001", content)
    }

    fun sendConversationMessage(sessionId: String, content: String, type: String = "text") {
        if (content.isBlank()) return
        val state = _uiState.value
        val from = state.currentUser ?: SocketUser(
            id = state.userId?.toString() ?: "",
            name = state.userName,
            username = state.userName,
            role = state.userRole,
            avatarUrl = state.userAvatar,
            type = "user"
        )
        val to = resolveConversationTarget(sessionId)
        if (to == null) {
            _uiState.update { it.copy(error = "会话不可用，请刷新后重试") }
            return
        }
        if (to.type == "user" && to.roomId.isBlank()) {
            _uiState.update { it.copy(error = "好友当前不在线，暂时只能给在线好友发送消息") }
            return
        }
        if (to.type == "group") {
            socketManager.sendGroupMessage(from, content, type)
        } else {
            socketManager.sendMessage(from, to, content, type)
        }
        val localMessage = SocketMessage(
            from = from,
            to = to,
            content = content,
            type = type
        )
        _uiState.update { it.copy(chatMessages = it.chatMessages + localMessage, error = null) }
    }

    private fun resolveConversationTarget(sessionId: String): SocketUser? {
        if (sessionId == "group_001") {
            return SocketUser("group_001", "群聊天室", "群聊天室", "", "", "group")
        }
        val userId = sessionId.removePrefix("user:")
        val online = _uiState.value.onlineUsers.firstOrNull { it.id == userId && it.type == "user" }
        if (online != null) return online
        val friend = _uiState.value.friends.firstOrNull { it.id?.toString() == userId }
        return friend?.let {
            SocketUser(
                id = it.id?.toString() ?: "",
                name = it.name ?: it.username ?: "好友",
                username = it.username ?: it.name ?: "好友",
                role = it.role ?: "user",
                avatarUrl = it.avatarUrl ?: "",
                type = it.type ?: "user"
            )
        }
    }

    // --- Friends ---
    fun loadFriends() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getFriends()
            }.onSuccess { response ->
                val groups = response.friends
                val all = mutableListOf<FriendDto>()
                groups?.accepted?.let { all.addAll(it.map { it.copy(status = "accepted") }) }
                groups?.sent?.let { all.addAll(it.map { it.copy(status = "sent") }) }
                groups?.received?.let { all.addAll(it.map { it.copy(status = "received") }) }
                _uiState.update { it.copy(friends = all) }
            }
        }
    }

    fun requestFriend(targetId: Int) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.requestFriend(FriendRequest(targetId))
            }.onSuccess {
                loadFriends()
            }.onFailure {
                _uiState.update { state -> state.copy(error = it.message ?: "好友请求失败") }
            }
        }
    }

    fun acceptFriend(otherId: Int) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.acceptFriend(otherId)
            }.onSuccess {
                loadFriends()
            }
        }
    }

    fun deleteFriend(otherId: Int) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.deleteFriend(otherId)
            }.onSuccess {
                loadFriends()
            }
        }
    }

    // --- News ---
    fun loadNews() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getNews(page = 1, pageSize = 20)
            }.onSuccess { response ->
                _uiState.update { it.copy(news = response.list ?: response.data ?: emptyList()) }
            }
        }
    }

    fun searchNews(keyword: String, categoryId: Int? = null) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getNews(page = 1, pageSize = 20, categoryId = categoryId, keyword = keyword.ifBlank { null })
            }.onSuccess { response ->
                _uiState.update { it.copy(news = response.list ?: response.data ?: emptyList(), error = null) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message ?: "新闻搜索失败") }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getCategories()
            }.onSuccess { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun loadNewsDetail(newsId: Int) {
        if (newsId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                runCatching { ApiClient.api.incrementView(newsId) }
                ApiClient.api.getNewsDetail(newsId)
            }.onSuccess { detail ->
                _uiState.update { it.copy(selectedNews = detail, loading = false) }
                loadComments(newsId)
            }.onFailure { throwable ->
                _uiState.update { it.copy(loading = false, error = throwable.message ?: "新闻详情加载失败") }
            }
        }
    }

    fun loadComments(newsId: Int) {
        if (newsId <= 0) return
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getComments(newsId)
            }.onSuccess { response ->
                _uiState.update { it.copy(comments = response.data ?: response.comments ?: emptyList()) }
            }
        }
    }

    fun addComment(newsId: Int, content: String) {
        if (newsId <= 0 || content.isBlank()) return
        viewModelScope.launch {
            runCatching {
                ApiClient.api.addComment(newsId, CommentRequest(content))
            }.onSuccess {
                loadComments(newsId)
                loadNews()
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message ?: "评论发送失败") }
            }
        }
    }

    fun publishNews(title: String, summary: String, content: String, categoryName: String) {
        if (title.isBlank() || content.isBlank()) {
            _uiState.update { it.copy(error = "标题和正文不能为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                ApiClient.api.createNews(
                    CreateNewsRequest(
                        title = title.trim(),
                        summary = summary.trim().ifBlank { null },
                        content = content.trim(),
                        categoryName = categoryName.ifBlank { "推荐" },
                        slug = "${System.currentTimeMillis()}-${title.hashCode().toString().replace("-", "n")}"
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(loading = false, notice = "新闻已发布") }
                loadNews()
                loadAdminData()
            }.onFailure { throwable ->
                _uiState.update { it.copy(loading = false, error = throwable.message ?: "新闻发布失败") }
            }
        }
    }

    fun deleteNews(newsId: Int) {
        if (newsId <= 0) return
        viewModelScope.launch {
            runCatching {
                ApiClient.api.deleteNews(newsId)
            }.onSuccess {
                _uiState.update { it.copy(news = it.news.filterNot { item -> item.id == newsId }) }
                loadAdminData()
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message ?: "删除新闻失败") }
            }
        }
    }

    // --- AI ---
    fun askAssistant(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                ApiClient.api.askAi(AiChatRequest(question))
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        aiAnswer = response.answer ?: response.message ?: "小岛 AI 暂时没有回复"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = throwable.message ?: "AI 请求失败"
                    )
                }
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getSettings()
            }.onSuccess { response ->
                _uiState.update { it.copy(settings = response.settings) }
            }
            runCatching {
                ApiClient.api.getAiConfig()
            }.onSuccess { response ->
                _uiState.update { it.copy(aiConfig = response) }
            }
        }
    }

    fun saveSettings(settings: SettingsDto) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.saveSettings(settings)
            }.onSuccess { response ->
                _uiState.update { it.copy(settings = response.settings ?: settings, notice = "设置已保存") }
            }.onFailure { throwable ->
                _uiState.update { it.copy(error = throwable.message ?: "设置保存失败") }
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.changePassword(mapOf("oldPassword" to oldPassword, "newPassword" to newPassword))
            }.onSuccess {
                _uiState.update { it.copy(notice = "密码修改成功") }
            }.onFailure {
                _uiState.update { it.copy(error = it.message ?: "密码修改失败") }
            }
        }
    }

    fun clearCache() {
        _uiState.update { it.copy(notice = "缓存已清理") }
    }

    fun loadAbout() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getAbout()
            }.onSuccess { response ->
                _uiState.update { it.copy(about = response) }
            }
        }
    }

    fun loadHealth() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.healthCheck()
            }.onSuccess { response ->
                _uiState.update { it.copy(health = response) }
            }
        }
    }

    fun loadAdminData() {
        if (_uiState.value.userRole != "admin") return
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getNewsStats()
            }.onSuccess { response ->
                _uiState.update { it.copy(adminStats = response) }
            }
            runCatching {
                ApiClient.api.getAuditLogs()
            }.onSuccess { logs ->
                _uiState.update { it.copy(auditLogs = logs) }
            }
        }
    }

    private fun loadAppData(role: String?) {
        loadNews()
        loadCategories()
        loadFriends()
        loadSettings()
        loadAbout()
        loadHealth()
        if (role == "admin") {
            loadAdminData()
        }
    }

    private fun mergeFriends(current: List<FriendDto>, incoming: List<FriendDto>): List<FriendDto> {
        if (incoming.isEmpty()) return current
        val keyed = LinkedHashMap<String, FriendDto>()
        (current + incoming).forEach { friend ->
            val key = friend.id?.toString() ?: friend.username ?: friend.name ?: return@forEach
            keyed[key] = friend
        }
        return keyed.values.toList()
    }

    fun categoryNames(): List<String> {
        val names = _uiState.value.categories.mapNotNull { it.name ?: it.categoryName }
        return (listOf("全部") + names).distinct().ifEmpty { fallbackCategories }
    }
}
