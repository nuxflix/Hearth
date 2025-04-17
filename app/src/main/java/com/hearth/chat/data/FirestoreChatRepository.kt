package com.hearth.chat.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore path: rooms/{roomId}/messages.
 * Each document stores authorId, authorName, body, and sentAt.
 */
class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val localUserId: String,
) : ChatRepository {
    override fun messages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = firestore.collection(ROOMS)
            .document(roomId)
            .collection(MESSAGES)
            .orderBy(SENT_AT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { document ->
                    messageFromFields(document.id, document.data ?: emptyMap())
                }.orEmpty()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun send(roomId: String, authorName: String, body: String) {
        val clean = outgoingBody(body) ?: return
        val fields = mapOf(
            "authorId" to localUserId,
            "authorName" to authorName,
            "body" to clean,
            SENT_AT to System.currentTimeMillis(),
        )
        firestore.collection(ROOMS)
            .document(roomId)
            .collection(MESSAGES)
            .add(fields)
            .await()
    }

    private companion object {
        const val ROOMS = "rooms"
        const val MESSAGES = "messages"
        const val SENT_AT = "sentAt"
    }
}
