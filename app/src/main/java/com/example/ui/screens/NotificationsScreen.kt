package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.LocalStrings
import com.example.model.NotificationKind
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MintPulse
import com.example.viewmodel.RfTubeUiState

@Composable
fun NotificationsScreen(
    state: RfTubeUiState,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit
) {
    val strings = LocalStrings.current
    var filter by remember { mutableStateOf<NotificationKind?>(null) }

    val filtered = remember(state.notifications, filter) {
        if (filter == null) state.notifications
        else state.notifications.filter { it.kind == filter }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 660.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "notif_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.tabNotifications, style = MaterialTheme.typography.headlineMedium)
                    TextButton(
                        onClick = onMarkAllRead,
                        modifier = Modifier.testTag("mark_all_read_button")
                    ) {
                        Icon(Icons.Filled.DoneAll, contentDescription = strings.markAllRead, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.markAllRead)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text(strings.notifAll) })
                    FilterChip(selected = filter == NotificationKind.LIKE, onClick = { filter = NotificationKind.LIKE }, label = { Text(strings.notifLikes) })
                    FilterChip(selected = filter == NotificationKind.COMMENT, onClick = { filter = NotificationKind.COMMENT }, label = { Text(strings.notifComments) })
                    FilterChip(selected = filter == NotificationKind.FOLLOW, onClick = { filter = NotificationKind.FOLLOW }, label = { Text(strings.notifFollows) })
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    ) {
                        Icon(
                            Icons.Filled.NotificationsNone,
                            contentDescription = strings.noNotifications,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(strings.noNotifications, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filtered, key = { it.id }) { notif ->
                    val (icon, tint) = when (notif.kind) {
                        NotificationKind.LIKE -> Icons.Filled.Favorite to CrimsonCoral
                        NotificationKind.COMMENT -> Icons.Filled.ChatBubble to ElectricAmber
                        NotificationKind.FOLLOW -> Icons.Filled.PersonAdd to MintPulse
                        NotificationKind.LIVE -> Icons.Filled.LiveTv to LiveRed
                        NotificationKind.MODERATION -> Icons.Filled.Shield to MaterialTheme.colorScheme.primary
                    }
                    val bodyText = if (state.language == AppLanguage.BN) notif.messageBn else notif.messageEn

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMarkRead(notif.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(tint.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.actorName, style = MaterialTheme.typography.titleMedium)
                                Text(bodyText, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    notif.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonCoral)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
