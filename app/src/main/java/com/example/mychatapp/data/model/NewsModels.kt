package com.example.mychatapp.data.model

data class NewsListResponse(
    val list: List<NewsDto>? = null,
    val data: List<NewsDto>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val pageSize: Int? = null
)

data class NewsDto(
    val id: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val content: String? = null,
    val coverUrl: String? = null,
    val categoryName: String? = null,
    val authorName: String? = null,
    val viewCount: Int? = null,
    val createdAt: String? = null
)
