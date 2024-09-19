package com.hearth.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hearth.chat.ui.HearthApp
import com.hearth.chat.ui.conversation.ConversationViewModel
import com.hearth.chat.ui.theme.HearthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as HearthApplication).graph
        setContent {
            val viewModel: ConversationViewModel = viewModel(
                factory = ConversationViewModel.factory(graph),
            )
            HearthTheme {
                HearthApp(viewModel)
            }
        }
    }
}
