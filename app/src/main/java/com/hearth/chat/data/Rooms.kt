package com.hearth.chat.data

data class Room(
    val id: String,
    val name: String,
    val topic: String,
    val members: Int,
)

object Rooms {
    val studio = Room(
        id = "studio",
        name = "Studio",
        topic = "Design notes and critiques",
        members = 6,
    )
    val kitchen = Room(
        id = "kitchen",
        name = "Kitchen",
        topic = "Lunch, coffee, and who is out",
        members = 8,
    )
    val night = Room(
        id = "night-shift",
        name = "Night shift",
        topic = "On-call handoff",
        members = 4,
    )
    val all = listOf(studio, kitchen, night)

    fun byId(id: String): Room = all.first { it.id == id }
}
