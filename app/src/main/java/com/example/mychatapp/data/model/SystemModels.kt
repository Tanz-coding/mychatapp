package com.example.mychatapp.data.model

data class AboutResponse(
    val name: String? = null,
    val version: String? = null,
    val build: String? = null,
    val time: Long? = null,
    val counts: AboutCounts? = null,
    val mysql: ServiceStatus? = null,
    val redis: ServiceStatus? = null
)

data class AboutCounts(
    val users: Int? = null,
    val news: Int? = null,
    val comments: Int? = null,
    val friendships: Int? = null
)

data class FileUploadResponse(
    val filePath: String? = null,
    val filename: String? = null,
    val size: Long? = null
)

data class HealthResponse(
    val status: String? = null,
    val time: Long? = null,
    val mysql: ServiceStatus? = null,
    val redis: ServiceStatus? = null
)

data class ServiceStatus(
    val ready: Boolean? = null,
    val error: String? = null
)
