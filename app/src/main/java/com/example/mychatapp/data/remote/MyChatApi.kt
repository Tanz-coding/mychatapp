package com.example.mychatapp.data.remote

import com.example.mychatapp.data.model.AiChatRequest
import com.example.mychatapp.data.model.AiChatResponse
import com.example.mychatapp.data.model.LoginRequest
import com.example.mychatapp.data.model.LoginResponse
import com.example.mychatapp.data.model.NewsListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MyChatApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/news")
    suspend fun getNews(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("sort") sort: String = "newest"
    ): NewsListResponse

    @POST("api/ai/chat")
    suspend fun askAi(@Body body: AiChatRequest): AiChatResponse
}
