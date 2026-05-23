package com.example.mychatapp.data.repository

import com.example.mychatapp.data.model.*
import com.example.mychatapp.data.remote.ApiClient

class NewsRepository {
    suspend fun latest(page: Int = 1, pageSize: Int = 10, categoryId: Int? = null, keyword: String? = null): List<NewsDto> {
        val response = ApiClient.api.getNews(page = page, pageSize = pageSize, categoryId = categoryId, keyword = keyword)
        return response.list ?: response.data ?: emptyList()
    }

    suspend fun detail(id: Int): NewsDto {
        return ApiClient.api.getNewsDetail(id)
    }

    suspend fun hot(limit: Int = 10): List<NewsDto> {
        return ApiClient.api.getHotNews(limit)
    }

    suspend fun recent(limit: Int = 10): List<NewsDto> {
        return ApiClient.api.getRecentNews(limit)
    }

    suspend fun categories(): List<CategoryDto> {
        return ApiClient.api.getCategories()
    }

    suspend fun incrementView(id: Int) {
        ApiClient.api.incrementView(id)
    }

    suspend fun comments(newsId: Int, page: Int = 1): CommentListResponse {
        return ApiClient.api.getComments(newsId, page)
    }

    suspend fun addComment(newsId: Int, content: String) {
        ApiClient.api.addComment(newsId, CommentRequest(content))
    }

    suspend fun deleteComment(newsId: Int, commentId: Int) {
        ApiClient.api.deleteComment(newsId, commentId)
    }
}
