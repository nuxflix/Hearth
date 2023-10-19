package com.hearth.chat.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

/**
 * Used until app/google-services.json is added. Messages stay on this phone.
 */
class LocalChatRepository(
    private val localUserId: String,
) : ChatRepository {
    private val messagesByRoom: Map<String, MutableStateFlow<List<ChatMessage>>> =
        Rooms.all.associate { room ->
            room.id to MutableStateFlow(SampleConversation.forRoom(room.id, localUserId))
        }

    override fun messages(roomId: String): Flow<List<ChatMessage>> =
        messagesByRoom[roomId] ?: flowOf(emptyList())

    override suspend fun send(roomId: String, authorName: String, body: String) {
        val clean = outgoingBody(body) ?: return
        val flow = messagesByRoom[roomId] ?: return
        flow.value = flow.value + ChatMessage(
            id = UUID.randomUUID().toString(),
            authorId = localUserId,
            authorName = authorName,
            body = clean,
            sentAt = System.currentTimeMillis(),
        )
    }
}
