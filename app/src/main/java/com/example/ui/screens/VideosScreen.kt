package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.LocalStrings
import com.example.model.ReportTargetType
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.model.VideoSubTab
import com.example.ui.components.AuthorAvatar
import com.example.ui.components.CommentsDialog
import com.example.ui.components.EngagementBarChartCard
import com.example.ui.components.MediaArt
import com.example.ui.components.ReportDialog
import com.example.ui.components.ViewsTrendChartCard
import com.example.ui.components.formatCompactCount
import com.example.ui.components.formatDuration
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.MintPulse
import com.example.viewmodel.RfTubeUiState
import kotlinx.coroutines.delay

@Composable
fun VideosScreen(
    state: RfTubeUiState,
    subTab: VideoSubTab,
    onSubTabChange: (VideoSubTab) -> Unit,
    selectedVideo: VideoItem?,
    onSelectVideo: (VideoItem?) -> Unit,
    recommendedShorts: List<VideoItem>,
    onToggleLikeVideo: (String) -> Unit,
    onToggleSave: (itemType: String, targetId: String, title: String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onAddComment: (targetId: String, text: String) -> String?,
    onAddReply: (targetId: String, commentId: String, text: String) -> String?,
    onRecordWatch: (VideoItem, Long) -> Unit,
    onClearHistory: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onAddToPlaylist: (playlistId: String, videoId: String) -> Unit,
    onReport: (ReportTargetType, String, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalStrings.current
    var showPlaylists by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }

    val longVideos = remember(state.videos) {
        state.videos.filter { it.videoKind == VideoKind.LONG.wireValue && !it.removed }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Segmented Switch: Long (16:9) vs Shorts (9:16)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = subTab == VideoSubTab.LONG,
                    onClick = { onSubTabChange(VideoSubTab.LONG) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("long_videos_tab")
                ) {
                    Text(strings.longVideosTab)
                }
                SegmentedButton(
                    selected = subTab == VideoSubTab.SHORT,
                    onClick = { onSubTabChange(VideoSubTab.SHORT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("short_videos_tab")
                ) {
                    Text(strings.shortVideosTab)
                }
            }
        }

        if (subTab == VideoSubTab.LONG) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(key = "quick_tools") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AssistChip(
                                onClick = { showPlaylists = true },
                                leadingIcon = { Icon(Icons.Filled.PlaylistPlay, contentDescription = strings.playlistsBtn, modifier = Modifier.size(18.dp)) },
                                label = { Text(strings.playlistsBtn) }
                            )
                            AssistChip(
                                onClick = { showHistory = true },
                                leadingIcon = { Icon(Icons.Filled.History, contentDescription = strings.watchHistoryBtn, modifier = Modifier.size(18.dp)) },
                                label = { Text(strings.watchHistoryBtn) }
                            )
                            AssistChip(
                                onClick = { showAnalytics = true },
                                leadingIcon = { Icon(Icons.Filled.Analytics, contentDescription = strings.analyticsBtn, modifier = Modifier.size(18.dp)) },
                                label = { Text(strings.analyticsBtn) }
                            )
                        }
                    }

                    items(longVideos, key = { it.id }) { video ->
                        LongVideoCard(
                            video = video,
                            onClick = { onSelectVideo(video) }
                        )
                    }
                }
            }
        } else {
            ShortVideosPlayer(
                state = state,
                shorts = recommendedShorts,
                onToggleLikeVideo = onToggleLikeVideo,
                onToggleSave = onToggleSave,
                onToggleFollow = onToggleFollow,
                onAddComment = onAddComment,
                onAddReply = onAddReply,
                onRecordWatch = onRecordWatch,
                onReport = onReport,
                onShowMessage = onShowMessage
            )
        }
    }

    if (selectedVideo != null) {
        val liveVideo = state.videos.firstOrNull { it.id == selectedVideo.id } ?: selectedVideo
        LongVideoPlayerDialog(
            video = liveVideo,
            state = state,
            onToggleLike = { onToggleLikeVideo(liveVideo.id) },
            onToggleSave = { onToggleSave("VIDEO", liveVideo.id, liveVideo.title) },
            onToggleFollow = { onToggleFollow(liveVideo.userId) },
            onAddComment = { text -> onAddComment(liveVideo.id, text) },
            onAddReply = { cid, text -> onAddReply(liveVideo.id, cid, text) },
            onRecordWatch = { secs -> onRecordWatch(liveVideo, secs) },
            onAddToPlaylist = { plId -> onAddToPlaylist(plId, liveVideo.id) },
            onReport = { reason -> onReport(ReportTargetType.VIDEO, liveVideo.id, reason) },
            onShowMessage = onShowMessage,
            onDismiss = { onSelectVideo(null) }
        )
    }

    if (showPlaylists) {
        PlaylistsDialog(
            state = state,
            onCreatePlaylist = onCreatePlaylist,
            onSelectVideo = { vid ->
                showPlaylists = false
                onSelectVideo(vid)
            },
            onDismiss = { showPlaylists = false }
        )
    }

    if (showHistory) {
        WatchHistoryDialog(
            state = state,
            onClearHistory = onClearHistory,
            onSelectVideo = { vid ->
                showHistory = false
                onSelectVideo(vid)
            },
            onDismiss = { showHistory = false }
        )
    }

    if (showAnalytics) {
        CreatorAnalyticsDialog(
            state = state,
            onDismiss = { showAnalytics = false }
        )
    }
}

@Composable
private fun LongVideoCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("long_video_${video.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                MediaArt(video.thumbKey, video.title, Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA0B0D14)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = video.title, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Surface(
                    color = Color(0xCC0B0D14),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = formatDuration(video.durationSec),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(14.dp)
            ) {
                AuthorAvatar(name = video.authorName, avatarKey = null, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(video.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${video.authorName} • ${formatCompactCount(video.viewsCount)} ${strings.viewsLabel} • ${video.tags.joinToString(" ") { "#$it" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LongVideoPlayerDialog(
    video: VideoItem,
    state: RfTubeUiState,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollow: () -> Unit,
    onAddComment: (String) -> String?,
    onAddReply: (String, String) -> String?,
    onRecordWatch: (Long) -> Unit,
    onAddToPlaylist: (String) -> Unit,
    onReport: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var isPlaying by remember(video.id) { mutableStateOf(true) }
    var elapsedSec by remember(video.id) { mutableLongStateOf(0L) }
    var speed by remember(video.id) { mutableFloatStateOf(1f) }
    var showComments by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }

    val isLiked = video.id in state.likedIds
    val isSaved = state.savedRefs.any { it.targetId == video.id && it.itemType == "VIDEO" }
    val isFollowing = video.userId in state.followedIds

    LaunchedEffect(video.id) {
        onRecordWatch(5L)
    }

    LaunchedEffect(isPlaying, speed, video.id) {
        while (isPlaying) {
            delay((1000L / speed).toLong().coerceAtLeast(350L))
            elapsedSec = (elapsedSec + 1L).coerceAtMost(video.durationSec)
            if (elapsedSec % 10L == 0L) {
                onRecordWatch(10L)
            }
            if (elapsedSec >= video.durationSec) {
                isPlaying = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 16:9 Player Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    if (!video.videoUri.isNullOrBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoURI(Uri.parse(video.videoUri))
                                    val mc = MediaController(ctx)
                                    mc.setAnchorView(this)
                                    setMediaController(mc)
                                    start()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MediaArt(video.thumbKey, video.title, Modifier.fillMaxSize())
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x55000000))
                        )
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CrimsonCoral.copy(alpha = 0.88f))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = video.title,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0x88000000))
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close, tint = Color.White)
                    }

                    Surface(
                        color = Color(0xAA0B0D14),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clickable {
                                speed = when (speed) {
                                    1f -> 1.5f
                                    1.5f -> 2f
                                    else -> 1f
                                }
                            }
                    ) {
                        Text(
                            "${strings.playbackSpeed}: ${speed}x",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Scrubber + Watch Time
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Slider(
                        value = elapsedSec.toFloat(),
                        onValueChange = { elapsedSec = it.toLong() },
                        valueRange = 0f..video.durationSec.toFloat().coerceAtLeast(1f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${formatDuration(elapsedSec)} / ${formatDuration(video.durationSec)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${strings.watchTimeLabel}: ${formatCompactCount(video.watchTimeSec)}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintPulse
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(video.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatCompactCount(video.viewsCount)} ${strings.viewsLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        video.tags.forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // Creator bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AuthorAvatar(video.authorName, null, size = 44.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(video.authorName, style = MaterialTheme.typography.titleMedium)
                            Text(video.authorHandle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (video.userId != state.currentUserId) {
                            if (isFollowing) {
                                OutlinedButton(onClick = onToggleFollow) { Text(strings.following) }
                            } else {
                                Button(onClick = onToggleFollow) { Text(strings.follow) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = onToggleLike) {
                            Icon(
                                if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = strings.like,
                                tint = if (isLiked) CrimsonCoral else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(formatCompactCount(video.likesCount))
                        }
                        TextButton(onClick = { showComments = true }) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = strings.comment)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${state.commentsByTarget[video.id].orEmpty().size}")
                        }
                        TextButton(onClick = { showPlaylistPicker = true }) {
                            Icon(Icons.Filled.PlaylistAdd, contentDescription = strings.addToPlaylist)
                        }
                        TextButton(onClick = onToggleSave) {
                            Icon(
                                if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = strings.save,
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TextButton(onClick = { onShowMessage(strings.sharedMsg) }) {
                            Icon(Icons.Outlined.Share, contentDescription = strings.share)
                        }
                        TextButton(onClick = { showReport = true }) {
                            Icon(Icons.Filled.Flag, contentDescription = strings.report)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            video.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }

    if (showComments) {
        CommentsDialog(
            targetTitle = video.title,
            comments = state.commentsByTarget[video.id].orEmpty(),
            onAddComment = onAddComment,
            onAddReply = onAddReply,
            onDismiss = { showComments = false }
        )
    }

    if (showPlaylistPicker) {
        AlertDialog(
            onDismissRequest = { showPlaylistPicker = false },
            title = { Text(strings.addToPlaylist) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.playlists.isEmpty()) {
                        Text(strings.noPlaylists)
                    } else {
                        state.playlists.forEach { pl ->
                            OutlinedButton(
                                onClick = {
                                    onAddToPlaylist(pl.id)
                                    showPlaylistPicker = false
                                    onShowMessage("${pl.title} ✓")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${pl.title} (${pl.videoIds.size})")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistPicker = false }) { Text(strings.close) }
            }
        )
    }

    if (showReport) {
        ReportDialog(
            targetLabel = video.title,
            onSubmit = { reason ->
                onReport(reason)
                onShowMessage(strings.reportSubmitted)
            },
            onDismiss = { showReport = false }
        )
    }
}

@Composable
fun PlaylistsDialog(
    state: RfTubeUiState,
    onCreatePlaylist: (String) -> Unit,
    onSelectVideo: (VideoItem) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var newTitle by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.playlistsBtn, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text(strings.playlistTitleHint) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newTitle.isNotBlank()) {
                            onCreatePlaylist(newTitle)
                            newTitle = ""
                        }
                    }) {
                        Text(strings.createBtn)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.playlists.isEmpty()) {
                        item { Text(strings.noPlaylists, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(state.playlists, key = { it.id }) { pl ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${pl.title} (${pl.videoIds.size})", style = MaterialTheme.typography.titleMedium)
                                    pl.videoIds.forEach { vidId ->
                                        val vid = state.videos.firstOrNull { it.id == vidId }
                                        if (vid != null) {
                                            Text(
                                                text = "▶ ${vid.title}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .clickable { onSelectVideo(vid) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchHistoryDialog(
    state: RfTubeUiState,
    onClearHistory: () -> Unit,
    onSelectVideo: (VideoItem) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.watchHistoryBtn, style = MaterialTheme.typography.titleLarge)
                    Row {
                        if (state.watchHistory.isNotEmpty()) {
                            TextButton(onClick = onClearHistory) { Text(strings.clearHistory) }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = strings.close)
                        }
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.watchHistory.isEmpty()) {
                        item { Text(strings.noHistory, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(state.watchHistory, key = { it.id }) { item ->
                            val video = state.videos.firstOrNull { it.id == item.videoId }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (video != null) onSelectVideo(video) }
                                    .padding(vertical = 4.dp)
                            ) {
                                MediaArt(
                                    item.thumbKey,
                                    item.title,
                                    Modifier
                                        .width(88.dp)
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${item.authorName} • ${item.watchedSec}s • ${item.timestampLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorAnalyticsDialog(
    state: RfTubeUiState,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val myVideos = state.videos.filter { it.userId == state.currentUserId }.ifEmpty { state.videos }
    val totalViews = myVideos.sumOf { it.viewsCount }
    val totalHours = myVideos.sumOf { it.watchTimeSec } / 3600.0
    val totalLikes = myVideos.sumOf { it.likesCount }
    val followers = state.currentChannel.followersCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.creatorDashboard, style = MaterialTheme.typography.headlineMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close)
                    }
                }

                // KPI Cards
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiStatCard(strings.totalViews, formatCompactCount(totalViews), CrimsonCoral, Modifier.weight(1f))
                    KpiStatCard(strings.totalWatchHours, String.format("%.1fh", totalHours), MintPulse, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiStatCard(strings.totalLikes, formatCompactCount(totalLikes), ElectricAmber, Modifier.weight(1f))
                    KpiStatCard(strings.followerGrowth, "+${formatCompactCount(followers)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }

                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                ViewsTrendChartCard(
                    title = strings.viewsChartTitle,
                    points = listOf(4200f, 5800f, 5100f, 7400f, 8900f, 11200f, (totalViews / 4f).coerceAtLeast(9500f)),
                    labels = dayLabels
                )

                EngagementBarChartCard(
                    title = strings.engagementChartTitle,
                    likesSeries = listOf(320f, 480f, 410f, 650f, 790f, 920f, 1140f),
                    followerSeries = listOf(120f, 190f, 240f, 310f, 420f, 560f, 680f),
                    labels = dayLabels
                )
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}
