package com.example.mychatapp.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private var _api: MyChatApi? = null
    val api: MyChatApi
        get() {
            if (_api == null) {
                throw IllegalStateException("ApiClient not initialized. Call ApiClient.init(context) first.")
            }
            return _api!!
        }

    fun init(context: Context, baseUrl: String = ApiConfig.getBaseUrl(context)) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val value = RuntimeSession.authValue
                val request = if (value.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $value")
                        .addHeader("token", value)
                        .build()
                }
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        _api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyChatApi::class.java)
    }

    fun reinit(context: Context, baseUrl: String) {
        _api = null
        init(context, baseUrl)
    }
}
