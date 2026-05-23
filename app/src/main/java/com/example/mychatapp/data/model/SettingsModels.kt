package com.example.mychatapp.data.model

data class SettingsDto(
    val theme: String? = null,
    val fontSize: String? = null,
    val isVoice: Boolean? = null,
    val notifications: Boolean? = null
)

data class SettingsResponse(
    val settings: SettingsDto? = null
)

data class AiConfigRequest(
    val enabled: Boolean? = null,
    val provider: String? = null,
    val model: String? = null,
    val endpoint: String? = null,
    val apiKey: String? = null
)

data class AiConfigResponse(
    val enabled: Boolean? = null,
    val provider: String? = null,
    val providerLabel: String? = null,
    val model: String? = null,
    val endpoint: String? = null,
    val hasApiKey: Boolean? = null
)
