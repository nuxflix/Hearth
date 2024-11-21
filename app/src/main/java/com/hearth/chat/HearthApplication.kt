package com.hearth.chat

import android.app.Application
import com.hearth.chat.data.ChatRepository
import com.hearth.chat.data.DisplayNameStore
import com.hearth.chat.data.LocalChatRepository

class HearthApplication : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}

class AppGraph(context: Application) {
    val names = DisplayNameStore(context)
    val firebaseConfigured: Boolean = false
    val repository: ChatRepository = LocalChatRepository(names.userId)
}
