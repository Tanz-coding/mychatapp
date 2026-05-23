package com.example.mychatapp.data.remote

object RuntimeSession {
    var authValue: String? = null
    var displayName: String? = null

    fun set(value: String?, name: String?) {
        authValue = value
        displayName = name
    }

    fun reset() {
        authValue = null
        displayName = null
    }
}
