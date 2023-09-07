package com.hearth.chat.data

object SampleConversation {
    const val MAYA = "maya"
    const val JORDAN = "jordan"

    fun forRoom(roomId: String, localUserId: String): List<ChatMessage> = when (roomId) {
        Rooms.studio.id -> studio(localUserId)
        Rooms.kitchen.id -> kitchen()
        Rooms.night.id -> night()
        else -> emptyList()
    }

    private fun studio(localUserId: String): List<ChatMessage> = listOf(
        line("s1", MAYA, "Maya", "Left the type ramp on the board. 16 and 24 feels right for a message.", 9, 12),
        line("s2", JORDAN, "Jordan", "Keep the ink bubble for the person on this phone. Everyone else stays on paper.", 9, 14),
        line("s3", localUserId, "Alex", "Done. Ink is yours. Paper is everyone else.", 9, 16),
        line("s4", MAYA, "Maya", "I'll set my name on the profile before we show this.", 9, 18),
        line("s5", JORDAN, "Jordan", "Kitchen is lunch. Night shift is the handoff.", 9, 21),
    )

    private fun kitchen(): List<ChatMessage> = listOf(
        line("k1", MAYA, "Maya", "Soup and bread at noon. Save a seat.", 8, 2),
        line("k2", JORDAN, "Jordan", "I can bring the coffee.", 8, 11),
    )

    private fun night(): List<ChatMessage> = listOf(
        line("n1", JORDAN, "Jordan", "Pager was quiet. The notes are on the board.", 23, 40, dayOffset = -1),
        line("n2", MAYA, "Maya", "I'll take the first watch.", 23, 58, dayOffset = -1),
    )

    private fun line(
        id: String,
        authorId: String,
        authorName: String,
        body: String,
        hour: Int,
        minute: Int,
        dayOffset: Int = 0,
    ): ChatMessage = ChatMessage(
        id = id,
        authorId = authorId,
        authorName = authorName,
        body = body,
        sentAt = atLocalTime(hour, minute, dayOffset),
    )
}
