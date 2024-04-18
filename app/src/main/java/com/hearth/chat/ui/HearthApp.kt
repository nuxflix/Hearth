package com.hearth.chat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hearth.chat.ui.components.RoomDrawer
import com.hearth.chat.ui.conversation.ConversationScreen
import com.hearth.chat.ui.conversation.ConversationViewModel
import com.hearth.chat.ui.profile.ProfileScreen
import com.hearth.chat.ui.theme.HearthCream
import com.hearth.chat.ui.theme.HearthDrawer
import kotlinx.coroutines.launch

@Composable
fun HearthApp(viewModel: ConversationViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var profileOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = profileOpen) { profileOpen = false }

    if (profileOpen) {
        ProfileScreen(
            displayName = state.displayName,
            firebaseConfigured = state.firebaseConfigured,
            onNameChange = viewModel::setDisplayName,
            onBack = { profileOpen = false },
        )
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                RoomDrawer(
                    rooms = state.rooms,
                    selectedId = state.room.id,
                    previews = state.previews,
                    displayName = state.displayName,
                    firebaseConfigured = state.firebaseConfigured,
                    onRoomSelected = viewModel::openRoom,
                    onProfile = { profileOpen = true },
                    modifier = Modifier.width(320.dp),
                )
                ConversationScreen(
                    state = state,
                    onSend = viewModel::send,
                    onDismissError = viewModel::dismissError,
                    onOpenDrawer = null,
                    onOpenSetup = { profileOpen = true },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = HearthDrawer,
                        drawerContentColor = HearthCream,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.width(320.dp),
                    ) {
                        RoomDrawer(
                            rooms = state.rooms,
                            selectedId = state.room.id,
                            previews = state.previews,
                            displayName = state.displayName,
                            firebaseConfigured = state.firebaseConfigured,
                            onRoomSelected = { roomId ->
                                viewModel.openRoom(roomId)
                                scope.launch { drawerState.close() }
                            },
                            onProfile = {
                                scope.launch { drawerState.close() }
                                profileOpen = true
                            },
                        )
                    }
                },
            ) {
                ConversationScreen(
                    state = state,
                    onSend = viewModel::send,
                    onDismissError = viewModel::dismissError,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenSetup = { profileOpen = true },
                )
            }
        }
    }
}
