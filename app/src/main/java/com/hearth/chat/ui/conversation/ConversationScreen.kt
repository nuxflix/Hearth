package com.hearth.chat.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hearth.chat.FirebaseSetup
import com.hearth.chat.R
import com.hearth.chat.data.ChatMessage
import com.hearth.chat.data.formatMessageTime
import com.hearth.chat.ui.theme.HearthCream
import com.hearth.chat.ui.theme.HearthEmber
import com.hearth.chat.ui.theme.HearthForest
import com.hearth.chat.ui.theme.HearthInk
import com.hearth.chat.ui.theme.HearthLine
import com.hearth.chat.ui.theme.HearthNight
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    state: ConversationUiState,
    onSend: suspend (String) -> Boolean,
    onDismissError: () -> Unit,
    onOpenDrawer: (() -> Unit)?,
    onOpenSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var draft by remember(state.room.id) { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val showJump by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    LaunchedEffect(state.room.id) {
        listState.scrollToItem(0)
    }

    fun submit() {
        if (sending || draft.isBlank()) return
        val text = draft
        scope.launch {
            sending = true
            val sent = onSend(text)
            sending = false
            if (sent) draft = ""
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ConversationTopBar(
            roomName = state.room.name,
            members = state.room.members,
            firebaseConfigured = state.firebaseConfigured,
            onOpenDrawer = onOpenDrawer,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        )
        if (!state.firebaseConfigured) {
            SetupStrip(onClick = onOpenSetup)
        }
        state.error?.let { message ->
            ErrorStrip(message = message, onClick = onDismissError)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (state.messages.isEmpty()) {
                EmptyRoom(roomName = state.room.name, firebaseConfigured = state.firebaseConfigured)
            } else {
                val newestFirst = state.messages.asReversed()
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(newestFirst, key = { _, message -> message.id }) { index, message ->
                        val older = newestFirst.getOrNull(index + 1)
                        val newer = newestFirst.getOrNull(index - 1)
                        MessageRow(
                            message = message,
                            mine = message.authorId == state.localUserId,
                            showName = older?.authorId != message.authorId,
                            showTime = newer?.authorId != message.authorId,
                        )
                    }
                }
            }
            if (showJump && state.messages.isNotEmpty()) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.jump_to_latest),
                    )
                }
            }
        }
        Composer(
            draft = draft,
            onDraftChange = { draft = it },
            onSend = { submit() },
            enabled = draft.isNotBlank() && !sending,
            hint = stringResource(R.string.message_hint, state.room.name),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.ime.union(WindowInsets.navigationBars),
            ),
        )
    }
}

@Composable
private fun ConversationTopBar(
    roomName: String,
    members: Int,
    firebaseConfigured: Boolean,
    onOpenDrawer: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onOpenDrawer != null) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_rooms),
                    )
                }
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.member_count, members),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SyncPill(firebaseConfigured)
        }
    }
}

@Composable
private fun SyncPill(firebaseConfigured: Boolean) {
    val label = if (firebaseConfigured) {
        stringResource(R.string.sync_live)
    } else {
        stringResource(R.string.sync_local)
    }
    val container = if (firebaseConfigured) HearthForest else HearthEmber.copy(alpha = 0.14f)
    val content = if (firebaseConfigured) HearthCream else HearthEmber
    Surface(shape = RoundedCornerShape(100.dp), color = container) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
        )
    }
}

@Composable
private fun SetupStrip(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = HearthEmber.copy(alpha = 0.12f),
    ) {
        Text(
            text = stringResource(R.string.firebase_banner, FirebaseSetup.CONFIG_PATH),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = HearthEmber,
        )
    }
}

@Composable
private fun ErrorStrip(message: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.errorContainer) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun EmptyRoom(roomName: String, firebaseConfigured: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.empty_room, roomName),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = if (firebaseConfigured) {
                stringResource(R.string.empty_room_live)
            } else {
                stringResource(R.string.empty_room_local)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    mine: Boolean,
    showName: Boolean,
    showTime: Boolean,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bubbleColor = when {
        mine && dark -> MaterialTheme.colorScheme.primary
        mine -> HearthInk
        dark -> MaterialTheme.colorScheme.surfaceVariant
        else -> HearthCream
    }
    val textColor = when {
        mine && dark -> HearthNight
        mine -> HearthCream
        else -> MaterialTheme.colorScheme.onSurface
    }
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = if (mine) 6.dp else 18.dp,
        bottomStart = if (mine) 18.dp else 6.dp,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (showName) 10.dp else 0.dp),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!mine) {
                if (showName) {
                    Avatar(message.authorName)
                    Spacer(Modifier.width(8.dp))
                } else {
                    Spacer(Modifier.width(44.dp))
                }
            }
            Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                if (!mine && showName) {
                    Text(
                        text = message.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                    )
                }
                Surface(
                    shape = shape,
                    color = bubbleColor,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .then(
                            if (!mine && !dark) {
                                Modifier.border(1.dp, HearthLine, shape)
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    Text(
                        text = message.body,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                    )
                }
                if (showTime) {
                    Text(
                        text = formatMessageTime(message.sentAt),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            color = HearthCream,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = (size.value * 0.38f).sp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Composer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier.padding(start = 18.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    maxLines = 4,
                    decorationBox = { inner ->
                        Box {
                            if (draft.isEmpty()) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        }
                    },
                )
                IconButton(onClick = onSend, enabled = enabled) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        },
                    )
                }
            }
        }
    }
}

private val avatarPalette = listOf(
    HearthEmber,
    HearthForest,
    androidx.compose.ui.graphics.Color(0xFF3E4C6B),
    androidx.compose.ui.graphics.Color(0xFF8A5A2A),
)

internal fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "?"
    return parts.take(2).joinToString("") { it.take(1) }.uppercase()
}

internal fun avatarColor(name: String): androidx.compose.ui.graphics.Color = when (name) {
    "Maya", "Alex" -> HearthEmber
    "Jordan" -> androidx.compose.ui.graphics.Color(0xFF3E4C6B)
    else -> avatarPalette[Math.floorMod(name.sumOf { it.code }, avatarPalette.size)]
}
