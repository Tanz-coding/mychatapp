package com.example.mychatapp.data.socket

import com.example.mychatapp.data.model.FriendDto
import com.example.mychatapp.data.remote.ApiConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SocketUser(
    val id: String,
    val name: String,
    val username: String,
    val role: String,
    val avatarUrl: String,
    val type: String, // "user" or "group"
    val roomId: String = "",
    val ip: String = ""
)

data class SocketMessage(
    val id: String = System.currentTimeMillis().toString(),
    val from: SocketUser,
    val to: SocketUser,
    val content: String,
    val type: String = "text", // text, image, file
    val time: Long = System.currentTimeMillis()
)

data class SocketLoginData(
    val token: String,
    val user: SocketUser,
    val friends: List<FriendDto> = emptyList(),
    val onlineUsers: List<SocketUser> = emptyList()
)

sealed class SocketEvent {
    data class LoginSuccess(val data: SocketLoginData) : SocketEvent()
    data class LoginFail(val message: String) : SocketEvent()
    data class Message(val msg: SocketMessage) : SocketEvent()
    data class SystemEvent(val user: SocketUser, val action: String) : SocketEvent()
    data class HistoryMessages(val messages: List<SocketMessage>) : SocketEvent()
    data class FriendRequest(val from: SocketUser, val to: SocketUser) : SocketEvent()
    data class FriendRequestSent(val from: SocketUser, val to: SocketUser) : SocketEvent()
    data class FriendAccepted(val from: SocketUser, val to: SocketUser) : SocketEvent()
    data class FriendDeleted(val from: SocketUser, val to: SocketUser) : SocketEvent()
    data class FriendError(val message: String) : SocketEvent()
    data class Error(val message: String) : SocketEvent()
}

class ChatSocketManager {
    private var socketUrl: String = ApiConfig.DEFAULT_SOCKET_URL

    fun updateSocketUrl(url: String) {
        socketUrl = url
    }
    private var socket: Socket? = null

    private val _events = MutableSharedFlow<SocketEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<SocketEvent> = _events

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _onlineUsers = MutableStateFlow<List<SocketUser>>(emptyList())
    val onlineUsers: StateFlow<List<SocketUser>> = _onlineUsers

    fun connect(token: String? = null) {
        if (socket?.connected() == true) return
        val options = IO.Options().apply {
            reconnection = true
            reconnectionAttempts = Int.MAX_VALUE
            reconnectionDelay = 1000
            reconnectionDelayMax = 5000
            timeout = 10000
            forceNew = true
            if (!token.isNullOrBlank()) {
                auth = mapOf("token" to token)
                extraHeaders = mapOf("token" to listOf(token))
            }
        }
        socket = IO.socket(socketUrl, options).also { s ->
            s.on(Socket.EVENT_CONNECT) {
                _connected.value = true
            }
            s.on(Socket.EVENT_DISCONNECT) {
                _connected.value = false
            }
            s.on(Socket.EVENT_CONNECT_ERROR) {
                _connected.value = false
                try {
                    _events.tryEmit(SocketEvent.Error("连接服务器失败"))
                } catch (_: Exception) {}
            }

            // Login result
            s.on("loginSuccess") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = parseLoginSuccess(args[0])
                        val online = parseOnlineUsers(if (args.size > 1) args[1] else null)
                        _onlineUsers.value = online
                        if (data != null) {
                            _events.tryEmit(SocketEvent.LoginSuccess(data))
                        }
                    }
                } catch (_: Exception) {}
            }

            s.on("loginFail") { args ->
                val msg = args.firstOrNull()?.toString() ?: "登录失败"
                _events.tryEmit(SocketEvent.LoginFail(msg))
            }

            // Messages
            s.on("message") { args ->
                try {
                    val msg = parseMessage(args)
                    if (msg != null) _events.tryEmit(SocketEvent.Message(msg))
                } catch (_: Exception) {}
            }

            s.on("history-message") { args ->
                try {
                    val messages = mutableListOf<SocketMessage>()
                    if (args.size > 1) {
                        val arr = args[1] as? JSONArray
                        arr?.let {
                            for (i in 0 until it.length()) {
                                val msg = parseSingleMessage(it.optJSONObject(i))
                                if (msg != null) messages.add(msg)
                            }
                        }
                    }
                    _events.tryEmit(SocketEvent.HistoryMessages(messages))
                } catch (_: Exception) {}
            }

            s.on("system") { args ->
                try {
                    val user = parseSocketUser(args.firstOrNull())
                    val action = args.getOrNull(1)?.toString() ?: ""
                    if (user != null) _events.tryEmit(SocketEvent.SystemEvent(user, action))
                } catch (_: Exception) {}
            }

            // Friend events
            s.on("friend-request") { args ->
                val from = parseSocketUser(args.firstOrNull())
                val to = parseSocketUser(args.getOrNull(1))
                if (from != null && to != null) {
                    _events.tryEmit(SocketEvent.FriendRequest(from, to))
                }
            }
            s.on("friend-request-sent") { args ->
                val from = parseSocketUser(args.firstOrNull())
                val to = parseSocketUser(args.getOrNull(1))
                if (from != null && to != null) {
                    _events.tryEmit(SocketEvent.FriendRequestSent(from, to))
                }
            }
            s.on("friend-accepted") { args ->
                val from = parseSocketUser(args.firstOrNull())
                val to = parseSocketUser(args.getOrNull(1))
                if (from != null && to != null) {
                    _events.tryEmit(SocketEvent.FriendAccepted(from, to))
                }
            }
            s.on("friend-deleted") { args ->
                val from = parseSocketUser(args.firstOrNull())
                val to = parseSocketUser(args.getOrNull(1))
                if (from != null && to != null) {
                    _events.tryEmit(SocketEvent.FriendDeleted(from, to))
                }
            }
            s.on("friend-error") { args ->
                val msg = args.firstOrNull()?.toString() ?: "好友操作失败"
                _events.tryEmit(SocketEvent.FriendError(msg))
            }

            s.connect()
        }
    }

    fun login(username: String, password: String) {
        val payload = JSONObject()
            .put("username", username)
            .put("password", password)
        socket?.emit("login", payload)
    }

    fun sendMessage(from: SocketUser?, to: SocketUser, content: String, type: String = "text") {
        val fromJson = from?.toJson() ?: JSONObject()
        val toJson = JSONObject()
            .put("id", to.id)
            .put("name", to.name)
            .put("username", to.username)
            .put("roomId", to.roomId)
            .put("type", to.type)
        socket?.emit("message", fromJson, toJson, content, type)
    }

    fun sendGroupMessage(from: SocketUser?, content: String, type: String = "text") {
        val to = JSONObject()
            .put("id", "group_001")
            .put("name", "群聊天室")
            .put("type", "group")
        socket?.emit("message", from?.toJson() ?: JSONObject(), to, content, type)
    }

    fun sendFriendRequest(targetUser: SocketUser) {
        val to = JSONObject()
            .put("id", targetUser.id)
            .put("name", targetUser.name)
            .put("type", targetUser.type)
        socket?.emit("friend-request", JSONObject(), to)
    }

    fun acceptFriend(targetUser: SocketUser) {
        val to = JSONObject()
            .put("id", targetUser.id)
            .put("name", targetUser.name)
        socket?.emit("friend-accept", JSONObject(), to)
    }

    fun deleteFriend(targetUser: SocketUser) {
        val to = JSONObject()
            .put("id", targetUser.id)
            .put("name", targetUser.name)
        socket?.emit("friend-delete", JSONObject(), to)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connected.value = false
        _onlineUsers.value = emptyList()
    }

    private fun parseLoginSuccess(arg: Any?): SocketLoginData? {
        val obj = arg as? JSONObject ?: return null
        val userJson = obj.optJSONObject("user")
        val token = obj.optString("token", "")
        val user = parseSocketUser(userJson) ?: return null

        val friendsJson = obj.optJSONObject("friends")
        val friends = mutableListOf<FriendDto>()
        parseFriendArray(friendsJson, "accepted", "accepted", friends)
        parseFriendArray(friendsJson, "sent", "sent", friends)
        parseFriendArray(friendsJson, "received", "received", friends)

        return SocketLoginData(
            token = token,
            user = user,
            friends = friends,
            onlineUsers = emptyList()
        )
    }

    private fun parseFriendArray(source: JSONObject?, key: String, status: String, out: MutableList<FriendDto>) {
        val arr = source?.optJSONArray(key) ?: return
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            out.add(
                FriendDto(
                    id = item.optInt("id"),
                    name = item.optString("name", item.optString("username", "")),
                    username = item.optString("username", item.optString("name", "")),
                    role = item.optString("role", "user"),
                    avatarUrl = item.optString("avatarUrl", ""),
                    type = item.optString("type", "user"),
                    status = status
                )
            )
        }
    }

    private fun parseOnlineUsers(arg: Any?): List<SocketUser> {
        if (arg == null) return emptyList()
        if (arg is JSONArray) {
            val list = mutableListOf<SocketUser>()
            for (i in 0 until arg.length()) {
                val user = parseSocketUser(arg.optJSONObject(i))
                if (user != null) list.add(user)
            }
            return list
        }
        return emptyList()
    }

    private fun parseMessage(args: Array<Any>): SocketMessage? {
        val from = parseSocketUser(args.firstOrNull())
        val to = parseSocketUser(args.getOrNull(1))
        val content = args.getOrNull(2)?.toString() ?: ""
        val type = args.getOrNull(3)?.toString() ?: "text"
        if (from == null) return null
        return SocketMessage(
            from = from,
            to = to ?: SocketUser("", "", "", "", "", ""),
            content = content,
            type = type
        )
    }

    private fun parseSingleMessage(obj: JSONObject?): SocketMessage? {
        if (obj == null) return null
        val fromJson = obj.optJSONObject("from")
        val toJson = obj.optJSONObject("to")
        val from = parseSocketUser(fromJson) ?: return null
        val to = parseSocketUser(toJson) ?: SocketUser("", "", "", "", "", "")
        return SocketMessage(
            id = obj.optString("_id", System.currentTimeMillis().toString()),
            from = from,
            to = to,
            content = obj.optString("content", ""),
            type = obj.optString("type", "text"),
            time = obj.optLong("time", System.currentTimeMillis())
        )
    }

    private fun parseSocketUser(arg: Any?): SocketUser? {
        val obj = arg as? JSONObject ?: return null
        return SocketUser(
            id = obj.optString("id", ""),
            name = obj.optString("name", obj.optString("username", "")),
            username = obj.optString("username", obj.optString("name", "")),
            role = obj.optString("role", "user"),
            avatarUrl = obj.optString("avatarUrl", ""),
            type = obj.optString("type", "user"),
            roomId = obj.optString("roomId", ""),
            ip = obj.optString("ip", "")
        )
    }

    private fun SocketUser.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("username", username)
        .put("role", role)
        .put("avatarUrl", avatarUrl)
        .put("type", type)
        .put("roomId", roomId)
        .put("ip", ip)
}
