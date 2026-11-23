package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.localization.LocalStrings
import com.example.model.ReportTargetType
import com.example.model.VideoItem
import com.example.ui.components.AuthorAvatar
import com.example.ui.components.CommentsDialog
import com.example.ui.components.MediaArt
import com.example.ui.components.ReportDialog
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.MintPulse
import com.example.viewmodel.RfTubeUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShortVideosPlayer(
    state: RfTubeUiState,
    shorts: List<VideoItem>,
    onToggleLikeVideo: (String) -> Unit,
    onToggleSave: (itemType: String, targetId: String, title: String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onAddComment: (targetId: String, text: String) -> String?,
    onAddReply: (targetId: String, commentId: String, text: String) -> String?,
    onRecordWatch: (VideoItem, Long) -> Unit,
    onReport: (ReportTargetType, String, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalStrings.current
    if (shorts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(strings.noResults)
        }
        return
    }

    // Infinite scroll feed backed by personalized tag-ranked shorts
    var pageMultiplier by remember { mutableIntStateOf(3) }
    val infiniteList = remember(shorts, pageMultiplier) {
        List(shorts.size * pageMultiplier) { idx -> shorts[idx % shorts.size] }
    }
    val pagerState = rememberPagerState(pageCount = { infiniteList.size })

    LaunchedEffect(pagerState.currentPage, infiniteList.size) {
        if (pagerState.currentPage >= infiniteList.size - 2) {
            pageMultiplier += 2
        }
        val currentShort = infiniteList.getOrNull(pagerState.currentPage)
        if (currentShort != null) {
            onRecordWatch(currentShort, 4L)
        }
    }

    var commentShort by remember { mutableStateOf<VideoItem?>(null) }
    var reportShort by remember { mutableStateOf<VideoItem?>(null) }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_vertical_pager")
    ) { page ->
        val shortItem = infiniteList[page]
        val liveShort = state.videos.firstOrNull { it.id == shortItem.id } ?: shortItem
        val isLiked = liveShort.id in state.likedIds
        val isSaved = state.savedRefs.any { it.targetId == liveShort.id && it.itemType == "VIDEO" }
        val isFollowing = liveShort.userId in state.followedIds
        val isMatchedRec = liveShort.tags.any { it in state.preferredTags }

        var isPlaying by remember(page) { mutableStateOf(true) }
        var progress by remember(page) { mutableFloatStateOf(0f) }
        var showHeartBurst by remember(page) { mutableStateOf(false) }

        LaunchedEffect(isPlaying, pagerState.currentPage) {
            if (pagerState.currentPage == page && isPlaying) {
                while (isPlaying) {
                    delay(100L)
                    progress += 1f / (liveShort.durationSec.coerceAtLeast(5L) * 10f)
                    if (progress >= 1f) progress = 0f
                }
            }
        }

        LaunchedEffect(showHeartBurst) {
            if (showHeartBurst) {
                delay(650L)
                showHeartBurst = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(liveShort.id) {
                    detectTapGestures(
                        onTap = { isPlaying = !isPlaying },
                        onDoubleTap = {
                            if (!isLiked) onToggleLikeVideo(liveShort.id)
                            showHeartBurst = true
                        }
                    )
                }
        ) {
            MediaArt(
                mediaKey = liveShort.thumbKey,
                contentDescription = liveShort.title,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x66000000),
                                Color.Transparent,
                                Color(0xD90B0D14)
                            )
                        )
                    )
            )

            // Top recommendation badge & swipe hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMatchedRec) {
                    Surface(
                        color = MintPulse.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "★ ${strings.recommendedForYou}",
                            color = MintPulse,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Text(
                    text = strings.swipeHint,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Center Pause indicator or Double-Tap Heart Burst
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            AnimatedVisibility(
                visible = showHeartBurst,
                enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = strings.liked,
                    tint = CrimsonCoral,
                    modifier = Modifier.size(104.dp)
                )
            }

            // Right Action Rail
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 36.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { onToggleLikeVideo(liveShort.id) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("short_like_${liveShort.id}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = strings.like,
                            tint = if (isLiked) CrimsonCoral else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(formatCompactCount(liveShort.likesCount), color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { commentShort = liveShort },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = strings.comment,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text("${state.commentsByTarget[liveShort.id].orEmpty().size}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                IconButton(
                    onClick = { onToggleSave("VIDEO", liveShort.id, liveShort.title) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = strings.save,
                        tint = if (isSaved) MintPulse else Color.White
                    )
                }

                IconButton(
                    onClick = { onShowMessage(strings.sharedMsg) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = strings.share, tint = Color.White)
                }

                IconButton(
                    onClick = { reportShort = liveShort },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                ) {
                    Icon(Icons.Filled.Flag, contentDescription = strings.report, tint = Color.White)
                }
            }

            // Bottom Creator & Metadata Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.78f)
                    .padding(start = 16.dp, bottom = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AuthorAvatar(liveShort.authorName, null, size = 38.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(liveShort.authorHandle, color = Color.White, fontWeight = FontWeight.Bold)
                    if (liveShort.userId != state.currentUserId) {
                        Spacer(modifier = Modifier.width(10.dp))
                        if (isFollowing) {
                            OutlinedButton(
                                onClick = { onToggleFollow(liveShort.userId) },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(strings.following, color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Button(
                                onClick = { onToggleFollow(liveShort.userId) },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonCoral),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(strings.follow, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    liveShort.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    liveShort.description,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    liveShort.tags.forEach { tag ->
                        Text("#$tag", color = MintPulse, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${liveShort.authorName} • Original Audio",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Bottom Play Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                color = CrimsonCoral,
                trackColor = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }

    if (commentShort != null) {
        val item = commentShort!!
        CommentsDialog(
            targetTitle = item.title,
            comments = state.commentsByTarget[item.id].orEmpty(),
            onAddComment = { text -> onAddComment(item.id, text) },
            onAddReply = { cid, text -> onAddReply(item.id, cid, text) },
            onDismiss = { commentShort = null }
        )
    }

    if (reportShort != null) {
        val item = reportShort!!
        ReportDialog(
            targetLabel = item.title,
            onSubmit = { reason ->
                onReport(ReportTargetType.VIDEO, item.id, reason)
                onShowMessage(strings.reportSubmitted)
            },
            onDismiss = { reportShort = null }
        )
    }
}
