package com.example.mychatapp.data.repository

import com.example.mychatapp.data.model.LoginRequest
import com.example.mychatapp.data.model.LoginResponse
import com.example.mychatapp.data.model.RegisterRequest
import com.example.mychatapp.data.remote.ApiClient

class AuthRepository {
    suspend fun login(account: String, password: String): LoginResponse {
        return ApiClient.api.login(LoginRequest(account, password))
    }

    suspend fun register(account: String, password: String): LoginResponse {
        return ApiClient.api.register(RegisterRequest(account, password))
    }

    suspend fun getMe(): LoginResponse {
        return ApiClient.api.getMe()
    }
}
