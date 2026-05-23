package com.example.mychatapp.data.repository

import com.example.mychatapp.data.model.NewsDto
import com.example.mychatapp.data.remote.ApiClient

class NewsRepository {
    suspend fun latest(): List<NewsDto> {
        val response = ApiClient.api.getNews(page = 1, pageSize = 10)
        return response.list ?: response.data ?: emptyList()
    }
}
