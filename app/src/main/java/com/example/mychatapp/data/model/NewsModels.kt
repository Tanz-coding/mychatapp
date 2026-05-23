package com.example.mychatapp.data.model

import com.google.gson.annotations.SerializedName

data class NewsListResponse(
    @SerializedName(value = "list", alternate = ["items"])
    val list: List<NewsDto>? = null,
    val data: List<NewsDto>? = null,
    val pagination: PaginationDto? = null,
    val total: Int? = null,
    val page: Int? = null,
    val pageSize: Int? = null
)

data class PaginationDto(
    val page: Int? = null,
    val pageSize: Int? = null,
    val total: Int? = null
)

data class NewsDto(
    val id: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val content: String? = null,
    @SerializedName(value = "coverImage", alternate = ["coverUrl", "cover_image"])
    val coverUrl: String? = null,
    val slug: String? = null,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val authorId: Int? = null,
    @SerializedName(value = "author", alternate = ["authorName", "username"])
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val tags: List<String>? = null,
    val viewCount: Int? = null,
    val commentCount: Int? = null,
    @SerializedName(value = "createdAt", alternate = ["publishedAt"])
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val status: String? = null
)

data class NewsDetailResponse(
    // Can be a single NewsDto
    val id: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val content: String? = null,
    val coverUrl: String? = null,
    val categoryName: String? = null,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val viewCount: Int? = null,
    val createdAt: String? = null
)

data class CommentDto(
    val id: Int? = null,
    val content: String? = null,
    @SerializedName(value = "authorId", alternate = ["userId"])
    val authorId: Int? = null,
    @SerializedName(value = "authorName", alternate = ["username", "author"])
    val authorName: String? = null,
    @SerializedName(value = "authorAvatar", alternate = ["avatarUrl"])
    val authorAvatar: String? = null,
    val createdAt: String? = null
)

data class CommentListResponse(
    val data: List<CommentDto>? = null,
    val comments: List<CommentDto>? = null,
    val pagination: PaginationDto? = null,
    val total: Int? = null,
    val page: Int? = null,
    val pageSize: Int? = null
)

data class CommentRequest(
    val content: String
)

data class CategoryDto(
    val id: Int? = null,
    val name: String? = null,
    val categoryName: String? = null,
    val description: String? = null,
    val count: Int? = null
)

data class HotNewsResponse(
    val hotNews: List<NewsDto>? = null,
    val data: List<NewsDto>? = null
)

data class StatsResponse(
    val users: Int? = null,
    val news: Int? = null,
    val comments: Int? = null,
    val friendships: Int? = null
)

data class NewsAdminStatsResponse(
    val perCategory: List<NewsStatRow> = emptyList(),
    val perAuthor: List<NewsStatRow> = emptyList()
)

data class NewsStatRow(
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val userId: Int? = null,
    val username: String? = null,
    val newsCount: Int? = null
)

data class AuditLogDto(
    val id: Int? = null,
    val action: String? = null,
    val targetType: String? = null,
    val targetId: Int? = null,
    val admin: String? = null,
    val createdAt: String? = null
)

data class CreateNewsRequest(
    val title: String,
    val content: String,
    val summary: String? = null,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val slug: String? = null,
    @SerializedName("coverImage")
    val coverImage: String? = null,
    val tags: List<String>? = null
)
