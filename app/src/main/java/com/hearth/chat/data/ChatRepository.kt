package com.hearth.chat.data

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun messages(roomId: String): Flow<List<ChatMessage>>

    suspend fun send(roomId: String, authorName: String, body: String)
}
