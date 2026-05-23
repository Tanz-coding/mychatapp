package com.example.mychatapp.data.model

data class AiChatRequest(
    val prompt: String
)

data class AiChatResponse(
    val answer: String? = null,
    val provider: String? = null,
    val message: String? = null
)
