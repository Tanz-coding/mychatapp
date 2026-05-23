package com.example.mychatapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.mychatapp.data.model.FileUploadResponse
import com.example.mychatapp.data.remote.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ChatRepository(private val context: Context) {
    suspend fun uploadFile(uri: Uri): FileUploadResponse {
        val file = uriToFile(uri)
        val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return ApiClient.api.uploadFile(part)
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open file")
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return file
    }
}
