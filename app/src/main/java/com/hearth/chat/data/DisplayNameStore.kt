package com.hearth.chat.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class DisplayNameStore(context: Context) {
    private val prefs = context.getSharedPreferences("hearth", Context.MODE_PRIVATE)
    private val _name = MutableStateFlow(readName())
    val name: StateFlow<String> = _name.asStateFlow()

    val userId: String
        get() {
            val saved = prefs.getString(KEY_USER, null)
            if (saved != null) return saved
            val created = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_USER, created).apply()
            return created
        }

    val displayName: String
        get() = _name.value

    fun setName(value: String) {
        val clean = value.trim().ifEmpty { DEFAULT_NAME }
        prefs.edit().putString(KEY_NAME, clean).apply()
        _name.value = clean
    }

    private fun readName(): String = prefs.getString(KEY_NAME, null)?.trim()?.ifEmpty { null } ?: DEFAULT_NAME

    private companion object {
        const val KEY_USER = "user_id"
        const val KEY_NAME = "display_name"
        const val DEFAULT_NAME = "Alex"
    }
}
