package com.example.mychatapp.data.remote

import android.content.Context

class TokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("mychat_mobile", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = preferences.getString(KEY_TOKEN, null)

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
    }
}
