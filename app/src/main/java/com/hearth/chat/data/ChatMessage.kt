package com.hearth.chat.data

data class ChatMessage(
    val id: String,
    val authorId: String,
    val authorName: String,
    val body: String,
    val sentAt: Long,
)

fun outgoingBody(raw: String): String? = raw.trim().takeIf { it.isNotEmpty() }

fun messageFromFields(id: String, fields: Map<String, Any?>): ChatMessage? {
    val body = (fields["body"] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val sentAt = when (val raw = fields["sentAt"]) {
        is Long -> raw
        is Int -> raw.toLong()
        is Double -> raw.toLong()
        else -> return null
    }
    val authorId = fields["authorId"] as? String ?: ""
    val authorName = (fields["authorName"] as? String)?.trim()?.ifEmpty { null } ?: "Someone"
    return ChatMessage(
        id = id,
        authorId = authorId,
        authorName = authorName,
        body = body,
        sentAt = sentAt,
    )
}
