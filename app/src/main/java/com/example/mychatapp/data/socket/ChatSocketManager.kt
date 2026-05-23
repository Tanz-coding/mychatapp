package com.example.mychatapp.data.socket

import com.example.mychatapp.data.remote.ApiConfig
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class ChatSocketManager(
    private val socketUrl: String = ApiConfig.DEFAULT_SOCKET_URL
) {
    private var socket: Socket? = null

    fun connect(token: String? = null) {
        if (socket?.connected() == true) return
        val options = IO.Options().apply {
            reconnection = true
            forceNew = true
            if (!token.isNullOrBlank()) {
                auth = mapOf("token" to token)
                extraHeaders = mapOf("token" to listOf(token))
            }
        }
        socket = IO.socket(socketUrl, options).also { it.connect() }
    }

    fun login(username: String, password: String) {
        val payload = JSONObject()
            .put("username", username)
            .put("password", password)
        socket?.emit("login", payload)
    }

    fun sendGroupMessage(content: String) {
        val from = JSONObject()
        val to = JSONObject()
            .put("id", "group_001")
            .put("name", "群聊天室")
            .put("type", "group")
        socket?.emit("message", from, to, content, "text")
    }

    fun onMessage(callback: (String) -> Unit) {
        socket?.on("message") { args ->
            callback(args.joinToString(separator = " ") { it.toString() })
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
