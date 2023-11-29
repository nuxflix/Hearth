package com.hearth.chat.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.hearth.chat.AppGraph
import com.hearth.chat.data.ChatMessage
import com.hearth.chat.data.ChatRepository
import com.hearth.chat.data.DisplayNameStore
import com.hearth.chat.data.Room
import com.hearth.chat.data.Rooms
import com.hearth.chat.data.SampleConversation
import com.hearth.chat.data.outgoingBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ConversationUiState(
    val room: Room,
    val rooms: List<Room>,
    val messages: List<ChatMessage>,
    val previews: Map<String, String>,
    val displayName: String,
    val localUserId: String,
    val firebaseConfigured: Boolean,
    val error: String?,
)

class ConversationViewModel(
    private val repository: ChatRepository,
    private val names: DisplayNameStore,
    private val firebaseConfigured: Boolean,
) : ViewModel() {
    private val roomId = MutableStateFlow(Rooms.studio.id)
    private val error = MutableStateFlow<String?>(null)

    val state: StateFlow<ConversationUiState> = combine(
        combine(roomId, names.name, error) { id, name, err -> Triple(id, name, err) },
        repository.messages(Rooms.studio.id),
        repository.messages(Rooms.kitchen.id),
        repository.messages(Rooms.night.id),
    ) { header, studio, kitchen, night ->
        val byRoom = mapOf(
            Rooms.studio.id to studio,
            Rooms.kitchen.id to kitchen,
            Rooms.night.id to night,
        )
        ConversationUiState(
            room = Rooms.byId(header.first),
            rooms = Rooms.all,
            messages = byRoom.getValue(header.first),
            previews = byRoom.mapValues { (_, messages) -> messages.lastOrNull()?.body.orEmpty() },
            displayName = header.second,
            localUserId = names.userId,
            firebaseConfigured = firebaseConfigured,
            error = header.third,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialState(names, firebaseConfigured),
    )

    fun openRoom(id: String) {
        if (Rooms.all.any { it.id == id }) {
            roomId.value = id
            error.value = null
        }
    }

    fun setDisplayName(value: String) {
        names.setName(value)
    }

    fun dismissError() {
        error.value = null
    }

    suspend fun send(body: String): Boolean {
        val clean = outgoingBody(body) ?: return false
        return try {
            repository.send(roomId.value, names.displayName, clean)
            true
        } catch (exception: Exception) {
            error.value = exception.message ?: "Could not send that message"
            false
        }
    }

    companion object {
        fun factory(graph: AppGraph): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = createViewModel(graph) as T

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    createViewModel(graph) as T

                private fun createViewModel(graph: AppGraph): ConversationViewModel =
                    ConversationViewModel(
                        repository = graph.repository,
                        names = graph.names,
                        firebaseConfigured = graph.firebaseConfigured,
                    )
            }
    }
}

private fun initialState(names: DisplayNameStore, firebaseConfigured: Boolean): ConversationUiState {
    val localUserId = names.userId
    val seeded = !firebaseConfigured
    val studio = if (seeded) SampleConversation.forRoom(Rooms.studio.id, localUserId) else emptyList()
    val previews = if (!seeded) {
        emptyMap()
    } else {
        Rooms.all.associate { room ->
            room.id to SampleConversation.forRoom(room.id, localUserId).lastOrNull()?.body.orEmpty()
        }
    }
    return ConversationUiState(
        room = Rooms.studio,
        rooms = Rooms.all,
        messages = studio,
        previews = previews,
        displayName = names.displayName,
        localUserId = localUserId,
        firebaseConfigured = firebaseConfigured,
        error = null,
    )
}
