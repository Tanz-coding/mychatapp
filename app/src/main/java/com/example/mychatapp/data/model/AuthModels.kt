package com.example.mychatapp.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val user: UserDto? = null,
    val token: String? = null,
    val message: String? = null
)

data class QrConfirmRequest(
    val sessionId: String
)

data class QrConfirmResponse(
    val status: String? = null,
    val user: UserDto? = null,
    val message: String? = null
)

data class UserDto(
    val id: Int? = null,
    val username: String? = null,
    val name: String? = null,
    val role: String? = null,
    @SerializedName(value = "avatarUrl", alternate = ["avatar_url"])
    val avatarUrl: String? = null,
    val email: String? = null
)
