package com.example.mychatapp.data.repository

import com.example.mychatapp.data.model.SettingsDto
import com.example.mychatapp.data.remote.ApiClient

class SettingsRepository {
    suspend fun get(): SettingsDto? {
        return ApiClient.api.getSettings().settings
    }

    suspend fun save(settings: SettingsDto) {
        ApiClient.api.saveSettings(settings)
    }
}
