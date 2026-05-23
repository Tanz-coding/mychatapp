package com.example.mychatapp.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val user: UserDto? = null,
    val token: String? = null,
    val message: String? = null
)

data class UserDto(
    val id: Int? = null,
    val username: String? = null,
    val name: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null
)
