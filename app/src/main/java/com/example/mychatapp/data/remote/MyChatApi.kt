package com.example.mychatapp.data.remote

import com.example.mychatapp.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface MyChatApi {
    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun getMe(): LoginResponse

    @PUT("api/auth/password")
    suspend fun changePassword(@Body body: Map<String, String>): Map<String, String>

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): LoginResponse

    @POST("api/auth/qr/confirm")
    suspend fun confirmQr(@Body body: QrConfirmRequest): QrConfirmResponse

    // Friends
    @GET("api/friends")
    suspend fun getFriends(): FriendListResponse

    @POST("api/friends/request")
    suspend fun requestFriend(@Body body: FriendRequest): FriendStatusResponse

    @POST("api/friends/{id}/accept")
    suspend fun acceptFriend(@Path("id") id: Int, @Body body: Map<String, String> = emptyMap()): FriendStatusResponse

    @DELETE("api/friends/{id}")
    suspend fun deleteFriend(@Path("id") id: Int): FriendStatusResponse

    // Settings
    @GET("api/settings")
    suspend fun getSettings(): SettingsResponse

    @PUT("api/settings")
    suspend fun saveSettings(@Body settings: SettingsDto): SettingsResponse

    // AI
    @POST("api/ai/chat")
    suspend fun askAi(@Body body: AiChatRequest): AiChatResponse

    @GET("api/ai/config")
    suspend fun getAiConfig(): AiConfigResponse

    @PUT("api/ai/config")
    suspend fun saveAiConfig(@Body config: AiConfigRequest): AiConfigResponse

    // News
    @GET("api/news")
    suspend fun getNews(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("sort") sort: String = "newest",
        @Query("categoryId") categoryId: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null
    ): NewsListResponse

    @GET("api/news/hot")
    suspend fun getHotNews(@Query("limit") limit: Int = 10): List<NewsDto>

    @GET("api/news/recent")
    suspend fun getRecentNews(@Query("limit") limit: Int = 10): List<NewsDto>

    @GET("api/news/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/news/{id}")
    suspend fun getNewsDetail(@Path("id") id: Int): NewsDto

    @POST("api/news/{id}/views")
    suspend fun incrementView(@Path("id") id: Int)

    @GET("api/news/{id}/comments")
    suspend fun getComments(
        @Path("id") newsId: Int,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): CommentListResponse

    @POST("api/news/{id}/comments")
    suspend fun addComment(@Path("id") newsId: Int, @Body body: CommentRequest)

    @DELETE("api/news/{id}/comments/{commentId}")
    suspend fun deleteComment(@Path("id") newsId: Int, @Path("commentId") commentId: Int)

    @POST("api/news")
    suspend fun createNews(@Body body: CreateNewsRequest): Map<String, Int>

    @PUT("api/news/{id}")
    suspend fun updateNews(@Path("id") id: Int, @Body body: CreateNewsRequest): Map<String, Int>

    @DELETE("api/news/{id}")
    suspend fun deleteNews(@Path("id") id: Int)

    @GET("api/news/stats")
    suspend fun getNewsStats(): NewsAdminStatsResponse

    @GET("api/news/audit/logs")
    suspend fun getAuditLogs(@Query("limit") limit: Int = 50): List<AuditLogDto>

    // System
    @GET("api/system/about")
    suspend fun getAbout(): AboutResponse

    @GET("api/health")
    suspend fun healthCheck(): HealthResponse

    // File upload
    @Multipart
    @POST("upload/file")
    suspend fun uploadFile(@Part file: MultipartBody.Part): FileUploadResponse
}
