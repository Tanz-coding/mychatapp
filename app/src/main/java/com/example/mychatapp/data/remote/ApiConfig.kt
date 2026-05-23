package com.example.mychatapp.data.remote

import android.content.Context

object ApiConfig {
    const val DEFAULT_BASE_URL = "http://172.30.34.106:3000/"
    const val DEFAULT_SOCKET_URL = "http://172.30.34.106:3000"

    private const val PREFS_NAME = "mychat_config"
    private const val KEY_BASE_URL = "base_url"
    private var cachedBaseUrl: String? = null

    fun getBaseUrl(context: Context): String {
        if (cachedBaseUrl != null) return cachedBaseUrl!!
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cachedBaseUrl = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        return cachedBaseUrl!!
    }

    fun getSocketUrl(context: Context): String {
        val base = getBaseUrl(context).trimEnd('/')
        return if (base.startsWith("http")) {
            base.replace("https://", "http://")
        } else base
    }

    fun setBaseUrl(context: Context, url: String) {
        val normalized = url.trimEnd('/') + "/"
        cachedBaseUrl = normalized
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_BASE_URL, normalized).apply()
    }

    fun resolveAssetUrl(context: Context, value: String?): String? {
        val raw = value?.trim().orEmpty()
        if (raw.isBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
        val base = getBaseUrl(context).trimEnd('/')
        return when {
            raw.startsWith("/") -> "$base$raw"
            raw.startsWith("static/") || raw.startsWith("assets/") || raw.startsWith("upload/") -> "$base/$raw"
            else -> raw
        }
    }
}
