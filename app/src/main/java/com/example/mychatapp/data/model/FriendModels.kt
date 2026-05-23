package com.example.mychatapp.data.model

data class FriendRequest(
    val targetId: Int
)

data class FriendDto(
    val id: Int? = null,
    val name: String? = null,
    val username: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val type: String? = null,
    val status: String? = null
)

data class FriendListResponse(
    val friends: FriendGroups? = null
)

data class FriendGroups(
    val accepted: List<FriendDto>? = null,
    val sent: List<FriendDto>? = null,
    val received: List<FriendDto>? = null
)

data class FriendStatusResponse(
    val status: String? = null,
    val message: String? = null
)
