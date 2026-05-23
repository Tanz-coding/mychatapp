package com.example.mychatapp.data.repository

import com.example.mychatapp.data.model.AiChatRequest
import com.example.mychatapp.data.remote.ApiClient

class AssistantRepository {
    suspend fun sendQuestion(question: String): String {
        val result = ApiClient.api.askAi(AiChatRequest(question))
        return result.answer ?: result.message ?: "小岛 AI 暂时没有回复"
    }
}
