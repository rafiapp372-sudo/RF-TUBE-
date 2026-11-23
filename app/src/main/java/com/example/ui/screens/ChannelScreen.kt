package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.localization.LocalStrings
import com.example.model.ChannelProfile
import com.example.model.VideoItem
import com.example.ui.components.AuthorAvatar
import com.example.ui.components.FollowersDialog
import com.example.ui.components.MediaArt
import com.example.ui.components.QrProfileDialog
import com.example.ui.components.formatCompactCount
import com.example.viewmodel.RfTubeUiState

@Composable
fun ChannelScreen(
    state: RfTubeUiState,
    inspectedChannel: ChannelProfile?,
    onClearInspectedChannel: () -> Unit,
    onUpdateProfile: (name: String, handle: String, bio: String, avatarUri: String?, coverUri: String?) -> Unit,
    onToggleFollow: (String) -> Unit,
    onOpenVideo: (VideoItem) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    val strings = LocalStrings.current
    val channel = inspectedChannel ?: state.currentChannel
    val isOwnChannel = channel.userId == state.currentUserId

    if (inspectedChannel != null) {
        BackHandler { onClearInspectedChannel() }
    }

    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showFollowers by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }

    val channelPosts = remember(state.posts, channel.userId) {
        state.posts.filter { it.userId == channel.userId }
    }
    val channelVideos = remember(state.videos, channel.userId) {
        state.videos.filter { it.userId == channel.userId && !it.removed }
    }

    val subTabs = if (isOwnChannel) {
        listOf(
            strings.myPostsTab,
            strings.myVideosTab,
            strings.playlistsBtn,
            strings.savedTab,
            strings.historyTab,
            strings.activityLogTab
        )
    } else {
        listOf(strings.myPostsTab, strings.myVideosTab)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "channel_header") {
                Column {
                    // Cover + Overlapping Avatar
                    Box(modifier = Modifier.fillMaxWidth().height(175.dp)) {
                        MediaArt(
                            mediaKey = channel.coverKey ?: "drawable:img_cover_studio",
                            contentDescription = channel.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x44000000), Color(0x880B0D14))
                                    )
                                )
                        )

                        if (inspectedChannel != null) {
                            IconButton(
                                onClick = onClearInspectedChannel,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x88000000))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.close, tint = Color.White)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.isAdmin) {
                                    IconButton(
                                        onClick = onOpenAdminPanel,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0x99000000))
                                            .testTag("open_admin_button")
                                    ) {
                                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = strings.adminPanelBtn, tint = Color.White)
                                    }
                                }
                                IconButton(
                                    onClick = onOpenSettings,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x99000000))
                                        .testTag("open_settings_button")
                                ) {
                                    Icon(Icons.Filled.Settings, contentDescription = strings.settingsBtn, tint = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp)
                                .offset(y = 14.dp)
                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .clip(CircleShape)
                        ) {
                            AuthorAvatar(name = channel.displayName, avatarKey = channel.avatarKey, size = 76.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(channel.displayName, style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    channel.handle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (channel.banned) {
                                Surface(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        strings.userBannedBadge,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (channel.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(channel.bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            Text(
                                text = "${formatCompactCount(channel.followersCount)} ${strings.followers}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable { showFollowers = true }
                            )
                            Text(
                                text = "${formatCompactCount(channel.followingCount)} ${strings.following}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable { showFollowers = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            if (isOwnChannel) {
                                OutlinedButton(
                                    onClick = { showEditProfile = true },
                                    modifier = Modifier.weight(1f).testTag("edit_profile_button")
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = strings.editProfile, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.editProfile, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                OutlinedButton(
                                    onClick = { showQr = true },
                                    modifier = Modifier.testTag("qr_profile_button")
                                ) {
                                    Icon(Icons.Filled.QrCode2, contentDescription = strings.qrProfile, modifier = Modifier.size(18.dp))
                                }
                                OutlinedButton(onClick = { showAnalytics = true }) {
                                    Icon(Icons.Filled.Analytics, contentDescription = strings.analyticsBtn, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                val isFollowing = channel.userId in state.followedIds
                                Button(
                                    onClick = { onToggleFollow(channel.userId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isFollowing) strings.following else strings.follow)
                                }
                                OutlinedButton(onClick = { showQr = true }) {
                                    Icon(Icons.Filled.QrCode2, contentDescription = strings.qrProfile, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subTabs.indices.toList()) { idx ->
                                FilterChip(
                                    selected = selectedSubTab == idx,
                                    onClick = { selectedSubTab = idx },
                                    label = { Text(subTabs[idx]) }
                                )
                            }
                        }
                    }
                }
            }

            when (selectedSubTab) {
                0 -> { // Posts
                    items(channelPosts, key = { it.id }) { post ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(post.caption, style = MaterialTheme.typography.bodyLarge)
                                if (!post.mediaKey.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MediaArt(
                                        post.mediaKey,
                                        post.caption,
                                        Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> { // Videos
                    items(channelVideos, key = { it.id }) { vid ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onOpenVideo(vid) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    MediaArt(vid.thumbKey, vid.title, Modifier.fillMaxSize())
                                    Icon(Icons.Filled.PlayArrow, contentDescription = vid.title, tint = Color.White, modifier = Modifier.align(Alignment.Center))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vid.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${formatCompactCount(vid.viewsCount)} ${strings.viewsLabel} • ${vid.videoKind}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> { // Playlists
                    items(state.playlists, key = { it.id }) { pl ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(pl.title, style = MaterialTheme.typography.titleMedium)
                                Text("${pl.videoIds.size} ${strings.videosFilter}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                3 -> { // Saved
                    if (state.savedRefs.isEmpty()) {
                        item {
                            Text(
                                strings.noSavedPosts,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(state.savedRefs, key = { it.id }) { ref ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            ref.itemType,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(ref.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                4 -> { // History
                    if (state.watchHistory.isNotEmpty()) {
                        item {
                            TextButton(
                                onClick = onClearHistory,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(strings.clearHistory)
                            }
                        }
                    }
                    items(state.watchHistory, key = { it.id }) { h ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(h.title, style = MaterialTheme.typography.titleMedium)
                                Text("${h.authorName} • ${h.watchedSec}s • ${h.timestampLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                5 -> { // Activity Log
                    if (state.activityLog.isEmpty()) {
                        item {
                            Text(strings.noActivity, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    } else {
                        items(state.activityLog, key = { it.id }) { act ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(act.action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    Text(act.detail, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfile) {
        EditProfileDialog(
            channel = channel,
            onSave = { name, handle, bio, avatarUri, coverUri ->
                onUpdateProfile(name, handle, bio, avatarUri, coverUri)
                showEditProfile = false
            },
            onDismiss = { showEditProfile = false }
        )
    }

    if (showQr) {
        QrProfileDialog(channel = channel, onDismiss = { showQr = false })
    }

    if (showFollowers) {
        FollowersDialog(
            title = strings.followers,
            channels = state.channels,
            followedIds = state.followedIds,
            currentUserId = state.currentUserId,
            onToggleFollow = onToggleFollow,
            onDismiss = { showFollowers = false }
        )
    }

    if (showAnalytics) {
        CreatorAnalyticsDialog(state = state, onDismiss = { showAnalytics = false })
    }
}

@Composable
private fun EditProfileDialog(
    channel: ChannelProfile,
    onSave: (name: String, handle: String, bio: String, avatarUri: String?, coverUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(channel.displayName) }
    var handle by remember { mutableStateOf(channel.handle) }
    var bio by remember { mutableStateOf(channel.bio) }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    var coverUri by remember { mutableStateOf<String?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) avatarUri = uri.toString()
    }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) coverUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editProfile) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.displayNameLabel) }, singleLine = true)
                OutlinedTextField(value = handle, onValueChange = { handle = it }, label = { Text(strings.handleLabel) }, singleLine = true)
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text(strings.bioLabel) }, minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (avatarUri != null) "Avatar ✓" else "Avatar", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (coverUri != null) "Cover ✓" else "Cover", maxLines = 1)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, handle, bio, avatarUri, coverUri) }) {
                Text(strings.saveChanges)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
