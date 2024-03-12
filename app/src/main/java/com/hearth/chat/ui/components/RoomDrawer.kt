package com.hearth.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hearth.chat.FirebaseSetup
import com.hearth.chat.R
import com.hearth.chat.data.Room
import com.hearth.chat.ui.conversation.Avatar
import com.hearth.chat.ui.theme.HearthCream
import com.hearth.chat.ui.theme.HearthDrawer
import com.hearth.chat.ui.theme.HearthDrawerMuted
import com.hearth.chat.ui.theme.HearthEmber

@Composable
fun RoomDrawer(
    rooms: List<Room>,
    selectedId: String,
    previews: Map<String, String>,
    displayName: String,
    firebaseConfigured: Boolean,
    onRoomSelected: (String) -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxHeight()
            .background(HearthDrawer)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = HearthCream,
        )
        Text(
            text = stringResource(R.string.rooms_label),
            modifier = Modifier.padding(start = 8.dp, top = 22.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = HearthDrawerMuted,
        )
        rooms.forEach { room ->
            RoomRow(
                room = room,
                preview = previews[room.id].orEmpty().ifEmpty { stringResource(R.string.no_messages_yet) },
                selected = room.id == selectedId,
                onClick = { onRoomSelected(room.id) },
            )
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onProfile)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(displayName)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(displayName, style = MaterialTheme.typography.labelLarge, color = HearthCream)
                Text(
                    text = stringResource(R.string.profile),
                    style = MaterialTheme.typography.bodySmall,
                    color = HearthDrawerMuted,
                )
            }
        }
        Text(
            text = if (firebaseConfigured) {
                stringResource(R.string.drawer_live)
            } else {
                stringResource(R.string.drawer_local, FirebaseSetup.CONFIG_PATH)
            },
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            style = MaterialTheme.typography.bodySmall,
            color = HearthDrawerMuted,
        )
    }
}

@Composable
private fun RoomRow(
    room: Room,
    preview: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) HearthEmber.copy(alpha = 0.18f) else HearthDrawer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            Modifier
                .size(width = 3.dp, height = 36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) HearthEmber else HearthDrawer),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = room.name,
                style = MaterialTheme.typography.titleMedium,
                color = HearthCream,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = HearthDrawerMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
