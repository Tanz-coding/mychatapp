package com.example.mychatapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychatapp.data.model.AiChatRequest
import com.example.mychatapp.data.model.LoginRequest
import com.example.mychatapp.data.model.NewsDto
import com.example.mychatapp.data.remote.ApiClient
import com.example.mychatapp.data.remote.RuntimeSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val signedIn: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val userName: String = "小岛居民",
    val news: List<NewsDto> = emptyList(),
    val aiAnswer: String = "你好！我是你的智能小助手"
)

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    fun enterDemo() {
        _uiState.update { it.copy(signedIn = true, error = null) }
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
                RuntimeSession.set(response.token, response.user?.username ?: account)
                _uiState.update {
                    it.copy(
                        signedIn = true,
                        loading = false,
                        userName = response.user?.username ?: account,
                        error = null
                    )
                }
                loadNews()
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

    fun signOut() {
        RuntimeSession.reset()
        _uiState.value = AppUiState()
    }

    fun loadNews() {
        viewModelScope.launch {
            runCatching {
                ApiClient.api.getNews(page = 1, pageSize = 10)
            }.onSuccess { response ->
                _uiState.update { it.copy(news = response.list ?: response.data ?: emptyList()) }
            }
        }
    }

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
}
