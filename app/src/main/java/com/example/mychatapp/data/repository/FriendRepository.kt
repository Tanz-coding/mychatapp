package com.example.mychatapp.data.repository

import com.example.mychatapp.data.model.FriendDto
import com.example.mychatapp.data.model.FriendRequest
import com.example.mychatapp.data.model.FriendStatusResponse
import com.example.mychatapp.data.remote.ApiClient

class FriendRepository {
    suspend fun list(): List<FriendDto> {
        val response = ApiClient.api.getFriends()
        val groups = response.friends
        val all = mutableListOf<FriendDto>()
        groups?.accepted?.let { all.addAll(it) }
        groups?.sent?.let { all.addAll(it) }
        groups?.received?.let { all.addAll(it) }
        return all
    }

    suspend fun request(targetId: Int): FriendStatusResponse {
        return ApiClient.api.requestFriend(FriendRequest(targetId))
    }

    suspend fun accept(otherId: Int): FriendStatusResponse {
        return ApiClient.api.acceptFriend(otherId)
    }

    suspend fun delete(otherId: Int): FriendStatusResponse {
        return ApiClient.api.deleteFriend(otherId)
    }
}
