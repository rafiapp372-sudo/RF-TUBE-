package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.window.Dialog
import com.example.localization.LocalStrings
import com.example.model.ChannelProfile
import com.example.model.FeedPost
import com.example.model.LiveStreamSession
import com.example.model.PostKind
import com.example.model.ReportTargetType
import com.example.model.StoryItem
import com.example.model.VideoItem
import com.example.ui.components.AuthorAvatar
import com.example.ui.components.CommentsDialog
import com.example.ui.components.MediaArt
import com.example.ui.components.ReportDialog
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MintPulse
import com.example.viewmodel.RfTubeUiState

@Composable
fun FeedScreen(
    state: RfTubeUiState,
    onToggleLikePost: (String) -> Unit,
    onToggleSave: (itemType: String, targetId: String, title: String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onAddComment: (targetId: String, text: String) -> String?,
    onAddReply: (targetId: String, commentId: String, text: String) -> String?,
    onEditPost: (postId: String, newCaption: String) -> Unit,
    onDeletePost: (postId: String) -> Unit,
    onReport: (ReportTargetType, String, String) -> Unit,
    onSendLiveChat: (String) -> Unit,
    onEndLive: () -> Unit,
    onOpenCreateStory: () -> Unit,
    onOpenVideo: (VideoItem) -> Unit,
    onOpenChannel: (ChannelProfile) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var searchFilter by remember { mutableStateOf("ALL") }
    var activeStory by remember { mutableStateOf<StoryItem?>(null) }
    var commentTargetPost by remember { mutableStateOf<FeedPost?>(null) }
    var editingPost by remember { mutableStateOf<FeedPost?>(null) }
    var reportingPost by remember { mutableStateOf<FeedPost?>(null) }
    var showLiveDialog by remember { mutableStateOf(false) }

    val nowMs = System.currentTimeMillis()
    val validStories = remember(state.stories) { state.stories.filter { it.expiresAtMs > nowMs } }
    val query = searchQuery.trim().lowercase()

    val filteredPosts = remember(state.posts, query, searchFilter) {
        if (query.isEmpty()) state.posts
        else if (searchFilter == "ALL" || searchFilter == "POSTS") {
            state.posts.filter {
                it.caption.lowercase().contains(query) ||
                    it.authorName.lowercase().contains(query) ||
                    it.authorHandle.lowercase().contains(query)
            }
        } else emptyList()
    }

    val filteredUsers = remember(state.channels, query, searchFilter) {
        if (query.isEmpty()) emptyList()
        else if (searchFilter == "ALL" || searchFilter == "USERS") {
            state.channels.filter {
                !it.banned && (it.displayName.lowercase().contains(query) || it.handle.lowercase().contains(query))
            }
        } else emptyList()
    }

    val filteredVideos = remember(state.videos, query, searchFilter) {
        if (query.isEmpty()) emptyList()
        else if (searchFilter == "ALL" || searchFilter == "VIDEOS") {
            state.videos.filter {
                !it.removed && (it.title.lowercase().contains(query) ||
                    it.tags.any { t -> t.contains(query) } ||
                    it.authorName.lowercase().contains(query))
            }
        } else emptyList()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            item(key = "search_bar") {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = strings.searchPlaceholder) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = strings.close)
                                }
                            }
                        },
                        placeholder = { Text(strings.searchPlaceholder) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feed_search_input")
                    )

                    if (query.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "ALL" to strings.allFilter,
                                "USERS" to strings.usersFilter,
                                "VIDEOS" to strings.videosFilter,
                                "POSTS" to strings.postsFilter
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = searchFilter == key,
                                    onClick = { searchFilter = key },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // Search Results: Matching Users & Videos
            if (filteredUsers.isNotEmpty()) {
                items(filteredUsers, key = { "u_${it.userId}" }) { ch ->
                    val isFollowing = ch.userId in state.followedIds
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenChannel(ch) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            AuthorAvatar(name = ch.displayName, avatarKey = ch.avatarKey, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ch.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${ch.handle} • ${formatCompactCount(ch.followersCount)} ${strings.followers}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (ch.userId != state.currentUserId) {
                                TextButton(onClick = { onToggleFollow(ch.userId) }) {
                                    Text(if (isFollowing) strings.following else strings.follow)
                                }
                            }
                        }
                    }
                }
            }

            if (filteredVideos.isNotEmpty()) {
                items(filteredVideos, key = { "sv_${it.id}" }) { vid ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenVideo(vid) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                MediaArt(vid.thumbKey, vid.title, Modifier.fillMaxSize())
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = vid.title,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(vid.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${vid.authorName} • ${formatCompactCount(vid.viewsCount)} ${strings.viewsLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Stories Strip (when not filtering out)
            if (query.isEmpty()) {
                item(key = "stories_strip") {
                    Column {
                        Text(
                            strings.storiesTitle,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            item(key = "add_story") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(70.dp)
                                        .clickable { onOpenCreateStory() }
                                        .testTag("add_story_button")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = strings.addStory,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        strings.addStory,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            items(validStories, key = { it.id }) { story ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(72.dp)
                                        .clickable { activeStory = story }
                                ) {
                                    AuthorAvatar(
                                        name = story.authorName,
                                        avatarKey = null,
                                        size = 58.dp,
                                        hasStoryRing = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        story.authorName,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Broadcast Banner
                if (state.liveSession.isLive) {
                    item(key = "live_banner") {
                        LiveBannerCard(
                            session = state.liveSession,
                            onWatch = { showLiveDialog = true }
                        )
                    }
                }
            }

            if (filteredPosts.isEmpty() && filteredUsers.isEmpty() && filteredVideos.isEmpty()) {
                item(key = "no_results") {
                    Text(
                        text = strings.noResults,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    val isLiked = post.id in state.likedIds
                    val isSaved = state.savedRefs.any { it.targetId == post.id && it.itemType == "POST" }
                    val isFollowing = post.userId in state.followedIds
                    val isOwn = post.userId == state.currentUserId
                    val authorChannel = state.channels.firstOrNull { it.userId == post.userId }

                    FeedPostCard(
                        post = post,
                        isLiked = isLiked,
                        isSaved = isSaved,
                        isFollowing = isFollowing,
                        isOwn = isOwn,
                        onLike = { onToggleLikePost(post.id) },
                        onComment = { commentTargetPost = post },
                        onShare = { onShowMessage(strings.sharedMsg) },
                        onSave = { onToggleSave("POST", post.id, post.caption.take(60)) },
                        onFollow = { onToggleFollow(post.userId) },
                        onEdit = { editingPost = post },
                        onDelete = { onDeletePost(post.id) },
                        onReport = { reportingPost = post },
                        onAuthorClick = { if (authorChannel != null) onOpenChannel(authorChannel) }
                    )
                }
            }
        }
    }

    // Story Viewer Dialog
    if (activeStory != null) {
        val story = activeStory!!
        val hoursLeft = ((story.expiresAtMs - System.currentTimeMillis()) / 3600000L).coerceAtLeast(1L)
        Dialog(onDismissRequest = { activeStory = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
            ) {
                MediaArt(story.mediaKey, story.caption, Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x99000000), Color.Transparent, Color(0xCC000000))
                            )
                        )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AuthorAvatar(story.authorName, null, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(story.authorName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${hoursLeft}h left", color = MintPulse, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = { activeStory = null }) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close, tint = Color.White)
                    }
                }
                Text(
                    text = story.caption,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                )
            }
        }
    }

    // Comments Dialog
    if (commentTargetPost != null) {
        val post = commentTargetPost!!
        CommentsDialog(
            targetTitle = post.caption,
            comments = state.commentsByTarget[post.id].orEmpty(),
            onAddComment = { text -> onAddComment(post.id, text) },
            onAddReply = { cid, text -> onAddReply(post.id, cid, text) },
            onDismiss = { commentTargetPost = null }
        )
    }

    // Edit Post Dialog
    if (editingPost != null) {
        val post = editingPost!!
        var caption by remember(post.id) { mutableStateOf(post.caption) }
        AlertDialog(
            onDismissRequest = { editingPost = null },
            title = { Text(strings.edit) },
            text = {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onEditPost(post.id, caption)
                    editingPost = null
                }) {
                    Text(strings.saveChanges)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPost = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Report Post Dialog
    if (reportingPost != null) {
        val post = reportingPost!!
        ReportDialog(
            targetLabel = "${post.authorName}: ${post.caption.take(50)}",
            onSubmit = { reason ->
                onReport(ReportTargetType.POST, post.id, reason)
                onShowMessage(strings.reportSubmitted)
            },
            onDismiss = { reportingPost = null }
        )
    }

    // Live Stream Room Dialog
    if (showLiveDialog) {
        LiveStreamRoomDialog(
            session = state.liveSession,
            isHost = state.liveSession.hostId == state.currentUserId,
            onSendChat = onSendLiveChat,
            onEndLive = {
                onEndLive()
                showLiveDialog = false
            },
            onDismiss = { showLiveDialog = false }
        )
    }
}

@Composable
private fun LiveBannerCard(
    session: LiveStreamSession,
    onWatch: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWatch() }
            .testTag("live_banner_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(LiveRed.copy(alpha = 0.22f), MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(14.dp)
        ) {
            Surface(color = LiveRed, shape = RoundedCornerShape(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.LiveTv, contentDescription = strings.liveNow, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.liveNow, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${session.hostName} • ${session.viewersCount} ${strings.liveViewers}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onWatch,
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.watchLive, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun FeedPostCard(
    post: FeedPost,
    isLiked: Boolean,
    isSaved: Boolean,
    isFollowing: Boolean,
    isOwn: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onFollow: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    onAuthorClick: () -> Unit
) {
    val strings = LocalStrings.current
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAuthorClick() }
                ) {
                    AuthorAvatar(name = post.authorName, avatarKey = null, size = 42.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(post.authorName, style = MaterialTheme.typography.titleMedium)
                        Text(post.authorHandle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (!isOwn) {
                    if (isFollowing) {
                        OutlinedButton(onClick = onFollow, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text(strings.following, style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        TextButton(onClick = onFollow) {
                            Text(strings.follow, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        if (isOwn) {
                            DropdownMenuItem(text = { Text(strings.edit) }, onClick = { menuExpanded = false; onEdit() })
                            DropdownMenuItem(text = { Text(strings.delete) }, onClick = { menuExpanded = false; onDelete() })
                        }
                        DropdownMenuItem(text = { Text(strings.report) }, onClick = { menuExpanded = false; onReport() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(post.caption, style = MaterialTheme.typography.bodyLarge)

            if (post.postType == PostKind.PHOTO.wireValue || !post.mediaKey.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                MediaArt(
                    mediaKey = post.mediaKey,
                    contentDescription = post.caption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onLike() }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("like_post_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = strings.like,
                            tint = if (isLiked) CrimsonCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(formatCompactCount(post.likesCount), style = MaterialTheme.typography.labelLarge)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onComment() }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("comment_post_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = strings.comment,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(formatCompactCount(post.commentsCount), style = MaterialTheme.typography.labelLarge)
                    }

                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = strings.share,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = strings.save,
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LiveStreamRoomDialog(
    session: LiveStreamSession,
    isHost: Boolean,
    onSendChat: (String) -> Unit,
    onEndLive: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var chatInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 460.dp, max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    MediaArt("drawable:img_cover_studio", session.title, Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55000000))
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Surface(color = LiveRed, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "${strings.liveNow} • ${session.viewersCount} ${strings.liveViewers}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = strings.close, tint = Color.White)
                        }
                    }
                    Text(
                        text = session.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(session.messages, key = { it.id }) { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text("${msg.senderName}: ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Text(msg.text, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text(strings.liveChatHint) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (chatInput.isNotBlank()) {
                            onSendChat(chatInput)
                            chatInput = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = strings.send, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (isHost) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onEndLive,
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.endLive)
                    }
                }
            }
        }
    }
}
