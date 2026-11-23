package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DemoData
import com.example.data.RfTubeRepository
import com.example.data.SpamDetector
import com.example.localization.AppLanguage
import com.example.model.ChannelProfile
import com.example.model.CommentItem
import com.example.model.CommentReply
import com.example.model.FeedPost
import com.example.model.LiveChatMessage
import com.example.model.LiveStreamSession
import com.example.model.ModerationReport
import com.example.model.NotificationItem
import com.example.model.NotificationKind
import com.example.model.PostKind
import com.example.model.PrivacyMode
import com.example.model.ReportStatus
import com.example.model.ReportTargetType
import com.example.model.StoryItem
import com.example.model.UserActivityEntry
import com.example.model.UserPlaylist
import com.example.model.UserPrivateSettings
import com.example.model.UserSavedRef
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.model.WatchHistoryEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class RfTubeUiState(
    val currentUserId: String = DemoData.DEMO_UID,
    val isDemoMode: Boolean = true,
    val isAdmin: Boolean = true,
    val needsProfileSetup: Boolean = false,
    val darkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.EN,
    val privacyMode: PrivacyMode = PrivacyMode.PUBLIC,
    val twoFactorEnabled: Boolean = false,
    val currentChannel: ChannelProfile = DemoData.demoChannels().first(),
    val channels: List<ChannelProfile> = DemoData.demoChannels(),
    val stories: List<StoryItem> = DemoData.demoStories(),
    val posts: List<FeedPost> = DemoData.demoPosts(),
    val videos: List<VideoItem> = DemoData.demoVideos(),
    val commentsByTarget: Map<String, List<CommentItem>> = DemoData.demoComments(),
    val notifications: List<NotificationItem> = DemoData.demoNotifications(),
    val playlists: List<UserPlaylist> = DemoData.demoPlaylists(),
    val savedRefs: List<UserSavedRef> = DemoData.demoSaved(),
    val watchHistory: List<WatchHistoryEntry> = DemoData.demoWatchHistory(),
    val activityLog: List<UserActivityEntry> = DemoData.demoActivityLog(),
    val reports: List<ModerationReport> = DemoData.demoReports(),
    val likedIds: Set<String> = emptySet(),
    val followedIds: Set<String> = setOf("creator_tanisha"),
    val preferredTags: Set<String> = setOf("dhaka", "tech"),
    val liveSession: LiveStreamSession = DemoData.demoLiveSession(),
    val statusBanner: String? = null,
    val firestoreError: String? = null
)

class RfTubeViewModel(
    private val repository: RfTubeRepository?,
    private val userId: String,
    private val userEmail: String?,
    private val userDisplayName: String?,
    private val isEmailVerified: Boolean,
    private val isDemoMode: Boolean
) : ViewModel() {

    private val initialChannels = DemoData.demoChannels()
    private val userChannelDefault = ChannelProfile(
        id = userId,
        userId = userId,
        displayName = userDisplayName?.takeIf { it.isNotBlank() } ?: "Rafi Studio",
        handle = "@" + (userDisplayName?.lowercase()?.replace(Regex("[^a-z0-9]"), "")?.takeIf { it.length >= 3 } ?: "rftuber"),
        bio = "Creator on RF TUBE • Feeds, 16:9 Films & 9:16 Shorts",
        avatarKey = "avatar_rafi",
        coverKey = "drawable:img_cover_studio",
        followersCount = if (isDemoMode) 18420L else 12L,
        followingCount = if (isDemoMode) 142L else 1L
    )

    private val _uiState = MutableStateFlow(
        RfTubeUiState(
            currentUserId = userId,
            isDemoMode = isDemoMode,
            isAdmin = isDemoMode || (isEmailVerified && userEmail == "rafiapp372@gmail.com"),
            needsProfileSetup = false,
            currentChannel = if (isDemoMode) initialChannels.first() else userChannelDefault,
            channels = if (isDemoMode) initialChannels else listOf(userChannelDefault) + initialChannels.drop(1)
        )
    )
    val uiState: StateFlow<RfTubeUiState> = _uiState.asStateFlow()

    init {
        if (!isDemoMode && repository != null) {
            observeFirestoreStreams(repository, userId)
        }
    }

    private fun observeFirestoreStreams(repo: RfTubeRepository, uid: String) {
        viewModelScope.launch {
            repo.observePrivateSettings(uid)
                .catch { e -> handleStreamError(e) }
                .collect { settings ->
                    if (settings != null) {
                        _uiState.update { state ->
                            state.copy(
                                darkMode = settings.darkMode,
                                language = if (settings.language == "bn") AppLanguage.BN else AppLanguage.EN,
                                privacyMode = PrivacyMode.fromWire(settings.privacyMode),
                                twoFactorEnabled = settings.twoFactorEnabled
                            )
                        }
                    } else {
                        repo.savePrivateSettings(
                            UserPrivateSettings(
                                userId = uid,
                                email = userEmail,
                                language = _uiState.value.language.code,
                                darkMode = _uiState.value.darkMode,
                                privacyMode = _uiState.value.privacyMode.wireValue,
                                twoFactorEnabled = false
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            repo.observeChannels()
                .catch { e -> handleStreamError(e) }
                .collect { remoteChannels ->
                    val ownRemote = remoteChannels.firstOrNull { it.userId == uid }
                    if (ownRemote == null) {
                        repo.upsertChannelProfile(_uiState.value.currentChannel)
                    }
                    val merged = (remoteChannels + DemoData.demoChannels())
                        .distinctBy { it.userId }
                    _uiState.update { state ->
                        state.copy(
                            currentChannel = ownRemote ?: state.currentChannel,
                            channels = merged
                        )
                    }
                }
        }

        viewModelScope.launch {
            repo.observePosts()
                .catch { e -> handleStreamError(e) }
                .collect { remotePosts ->
                    val merged = (remotePosts + DemoData.demoPosts()).distinctBy { it.id }
                    _uiState.update { it.copy(posts = merged) }
                }
        }

        viewModelScope.launch {
            repo.observeVideos()
                .catch { e -> handleStreamError(e) }
                .collect { remoteVideos ->
                    val merged = (remoteVideos + DemoData.demoVideos()).distinctBy { it.id }
                    _uiState.update { it.copy(videos = merged) }
                }
        }

        viewModelScope.launch {
            repo.observeStories()
                .catch { e -> handleStreamError(e) }
                .collect { remoteStories ->
                    val now = System.currentTimeMillis()
                    val merged = (remoteStories + DemoData.demoStories(now))
                        .distinctBy { it.id }
                        .filter { it.expiresAtMs > now }
                    _uiState.update { it.copy(stories = merged) }
                }
        }

        viewModelScope.launch {
            repo.observePlaylists(uid)
                .catch { e -> handleStreamError(e) }
                .collect { remote ->
                    if (remote.isNotEmpty()) {
                        _uiState.update { it.copy(playlists = remote) }
                    }
                }
        }

        viewModelScope.launch {
            repo.observeSaved(uid)
                .catch { e -> handleStreamError(e) }
                .collect { remote ->
                    if (remote.isNotEmpty()) {
                        _uiState.update { it.copy(savedRefs = remote) }
                    }
                }
        }

        viewModelScope.launch {
            repo.observeActivity(uid)
                .catch { e -> handleStreamError(e) }
                .collect { remote ->
                    if (remote.isNotEmpty()) {
                        _uiState.update { it.copy(activityLog = remote) }
                    }
                }
        }

        viewModelScope.launch {
            repo.observeReports()
                .catch { e -> handleStreamError(e) }
                .collect { remote ->
                    val merged = (remote + DemoData.demoReports()).distinctBy { it.id }
                    _uiState.update { it.copy(reports = merged) }
                }
        }
    }

    private fun handleStreamError(error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        _uiState.update { it.copy(firestoreError = root.message ?: "Sync error") }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBanner = null, firestoreError = null) }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkMode = enabled) }
        persistSettings()
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        persistSettings()
    }

    fun setPrivacyMode(mode: PrivacyMode) {
        _uiState.update { it.copy(privacyMode = mode) }
        addActivity("Privacy Updated", mode.wireValue)
        persistSettings()
    }

    fun enableTwoFactor(code: String): Boolean {
        if (code.trim().length != 6 || !code.trim().all { it.isDigit() }) return false
        _uiState.update { it.copy(twoFactorEnabled = true) }
        addActivity("Security", "Enabled Two-Factor Authentication")
        persistSettings()
        return true
    }

    fun disableTwoFactor() {
        _uiState.update { it.copy(twoFactorEnabled = false) }
        addActivity("Security", "Disabled Two-Factor Authentication")
        persistSettings()
    }

    private fun persistSettings() {
        val repo = repository ?: return
        if (isDemoMode) return
        val s = _uiState.value
        viewModelScope.launch {
            repo.savePrivateSettings(
                UserPrivateSettings(
                    userId = userId,
                    email = userEmail,
                    language = s.language.code,
                    darkMode = s.darkMode,
                    privacyMode = s.privacyMode.wireValue,
                    twoFactorEnabled = s.twoFactorEnabled
                )
            )
        }
    }

    fun updateProfile(displayName: String, handle: String, bio: String, avatarUri: String?, coverUri: String?) {
        val cleanName = displayName.trim().ifBlank { _uiState.value.currentChannel.displayName }
        val cleanHandle = handle.trim().let { if (it.startsWith("@")) it else "@$it" }
        val updated = _uiState.value.currentChannel.copy(
            displayName = cleanName,
            handle = cleanHandle,
            bio = bio.trim(),
            avatarKey = avatarUri ?: _uiState.value.currentChannel.avatarKey,
            coverKey = coverUri ?: _uiState.value.currentChannel.coverKey
        )
        _uiState.update { state ->
            state.copy(
                currentChannel = updated,
                channels = state.channels.map { if (it.userId == updated.userId) updated else it }
            )
        }
        addActivity("Profile Updated", "$cleanName ($cleanHandle)")
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.upsertChannelProfile(updated) }
        }
    }

    fun toggleFollow(targetUserId: String) {
        if (targetUserId == userId) return
        val wasFollowing = targetUserId in _uiState.value.followedIds
        val delta = if (wasFollowing) -1L else 1L
        _uiState.update { state ->
            val nextFollowed = if (wasFollowing) state.followedIds - targetUserId else state.followedIds + targetUserId
            val nextChannels = state.channels.map { ch ->
                when (ch.userId) {
                    targetUserId -> ch.copy(followersCount = (ch.followersCount + delta).coerceAtLeast(0L))
                    userId -> ch.copy(followingCount = (ch.followingCount + delta).coerceAtLeast(0L))
                    else -> ch
                }
            }
            val nextCurrent = nextChannels.firstOrNull { it.userId == userId } ?: state.currentChannel
            state.copy(
                followedIds = nextFollowed,
                channels = nextChannels,
                currentChannel = nextCurrent
            )
        }
        val targetName = _uiState.value.channels.firstOrNull { it.userId == targetUserId }?.displayName ?: targetUserId
        addActivity(if (wasFollowing) "Unfollowed" else "Followed Creator", targetName)
    }

    fun toggleLikePost(postId: String) {
        val already = postId in _uiState.value.likedIds
        val delta = if (already) -1L else 1L
        _uiState.update { state ->
            state.copy(
                likedIds = if (already) state.likedIds - postId else state.likedIds + postId,
                posts = state.posts.map { p ->
                    if (p.id == postId) p.copy(likesCount = (p.likesCount + delta).coerceAtLeast(0L)) else p
                }
            )
        }
        if (!isDemoMode && repository != null && !postId.startsWith("post_")) {
            viewModelScope.launch { repository.incrementPostLikes(postId, delta) }
        }
    }

    fun toggleLikeVideo(videoId: String) {
        val already = videoId in _uiState.value.likedIds
        val delta = if (already) -1L else 1L
        val video = _uiState.value.videos.firstOrNull { it.id == videoId }
        _uiState.update { state ->
            val updatedTags = if (!already && video != null) state.preferredTags + video.tags else state.preferredTags
            state.copy(
                likedIds = if (already) state.likedIds - videoId else state.likedIds + videoId,
                preferredTags = updatedTags,
                videos = state.videos.map { v ->
                    if (v.id == videoId) v.copy(likesCount = (v.likesCount + delta).coerceAtLeast(0L)) else v
                }
            )
        }
        if (!isDemoMode && repository != null && !videoId.startsWith("vid_")) {
            viewModelScope.launch { repository.recordVideoMetrics(videoId, 0L, 0L, delta) }
        }
    }

    fun toggleSaveItem(itemType: String, targetId: String, title: String) {
        val exists = _uiState.value.savedRefs.any { it.targetId == targetId && it.itemType == itemType }
        val newRef = UserSavedRef(
            id = "${itemType}_$targetId",
            userId = userId,
            itemType = itemType,
            targetId = targetId,
            title = title
        )
        _uiState.update { state ->
            val next = if (exists) {
                state.savedRefs.filterNot { it.targetId == targetId && it.itemType == itemType }
            } else {
                listOf(newRef) + state.savedRefs
            }
            state.copy(savedRefs = next)
        }
        addActivity(if (exists) "Removed Saved" else "Saved $itemType", title)
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.toggleSavedRef(newRef, save = !exists) }
        }
    }

    fun addComment(targetId: String, text: String): String? {
        val spam = SpamDetector.inspect(text, enforceRateLimit = false)
        if (spam.isSpam) {
            return if (_uiState.value.language == AppLanguage.BN) spam.reasonBn else spam.reasonEn
        }
        val clean = text.trim()
        if (clean.isEmpty()) return null
        val channel = _uiState.value.currentChannel
        val comment = CommentItem(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            targetId = targetId,
            authorId = channel.userId,
            authorName = channel.displayName,
            authorHandle = channel.handle,
            text = clean,
            timeAgo = "Just now"
        )
        _uiState.update { state ->
            val currentList = state.commentsByTarget[targetId].orEmpty()
            state.copy(
                commentsByTarget = state.commentsByTarget + (targetId to (listOf(comment) + currentList)),
                posts = state.posts.map { if (it.id == targetId) it.copy(commentsCount = it.commentsCount + 1) else it },
                videos = state.videos.map { if (it.id == targetId) it.copy(commentsCount = it.commentsCount + 1) else it }
            )
        }
        addActivity("Commented", clean.take(80))
        return null
    }

    fun addReply(targetId: String, commentId: String, text: String): String? {
        val spam = SpamDetector.inspect(text, enforceRateLimit = false)
        if (spam.isSpam) {
            return if (_uiState.value.language == AppLanguage.BN) spam.reasonBn else spam.reasonEn
        }
        val clean = text.trim()
        if (clean.isEmpty()) return null
        val channel = _uiState.value.currentChannel
        val reply = CommentReply(
            id = "r_${UUID.randomUUID().toString().take(8)}",
            authorName = channel.displayName,
            authorHandle = channel.handle,
            text = clean,
            timeAgo = "Just now"
        )
        _uiState.update { state ->
            val list = state.commentsByTarget[targetId].orEmpty().map { c ->
                if (c.id == commentId) c.copy(replies = c.replies + reply) else c
            }
            state.copy(commentsByTarget = state.commentsByTarget + (targetId to list))
        }
        return null
    }

    fun createPost(caption: String, mediaUri: String?): String? {
        val spam = SpamDetector.inspect(caption, enforceRateLimit = true)
        if (spam.isSpam) {
            return if (_uiState.value.language == AppLanguage.BN) spam.reasonBn else spam.reasonEn
        }
        val ch = _uiState.value.currentChannel
        val newId = "user_post_${System.currentTimeMillis()}"
        val post = FeedPost(
            id = newId,
            userId = ch.userId,
            authorName = ch.displayName,
            authorHandle = ch.handle,
            caption = caption.trim(),
            postType = if (mediaUri != null) PostKind.PHOTO.wireValue else PostKind.TEXT.wireValue,
            mediaKey = mediaUri,
            likesCount = 0L,
            commentsCount = 0L,
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _uiState.update { it.copy(posts = listOf(post) + it.posts) }
        addActivity("Published Post", caption.trim().take(80))
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.createPost(post) }
        }
        return null
    }

    fun editPost(postId: String, newCaption: String) {
        val clean = newCaption.trim()
        if (clean.isEmpty()) return
        _uiState.update { state ->
            state.copy(posts = state.posts.map { if (it.id == postId) it.copy(caption = clean) else it })
        }
        if (!isDemoMode && repository != null && !postId.startsWith("post_")) {
            viewModelScope.launch { repository.updatePostCaption(postId, clean) }
        }
    }

    fun deletePost(postId: String) {
        _uiState.update { state ->
            state.copy(posts = state.posts.filterNot { it.id == postId })
        }
        addActivity("Deleted Post", postId)
        if (!isDemoMode && repository != null && !postId.startsWith("post_")) {
            viewModelScope.launch { repository.deletePost(postId) }
        }
    }

    fun createVideo(
        title: String,
        description: String,
        tagsRaw: String,
        kind: VideoKind,
        thumbUri: String?,
        videoUri: String?
    ): String? {
        val spam = SpamDetector.inspect("$title $description", enforceRateLimit = true)
        if (spam.isSpam) {
            return if (_uiState.value.language == AppLanguage.BN) spam.reasonBn else spam.reasonEn
        }
        val ch = _uiState.value.currentChannel
        val tags = tagsRaw.split(",").map { it.trim().removePrefix("#").lowercase() }.filter { it.isNotBlank() }.take(10)
        val defaultThumb = if (kind == VideoKind.SHORT) "drawable:img_short_neon_dance" else "drawable:img_cover_studio"
        val video = VideoItem(
            id = "user_vid_${System.currentTimeMillis()}",
            userId = ch.userId,
            authorName = ch.displayName,
            authorHandle = ch.handle,
            title = title.trim(),
            description = description.trim(),
            tags = tags.ifEmpty { listOf("rftube", "creator") },
            videoKind = kind.wireValue,
            durationSec = if (kind == VideoKind.SHORT) 30L else 420L,
            viewsCount = 1L,
            watchTimeSec = 0L,
            likesCount = 0L,
            commentsCount = 0L,
            thumbKey = thumbUri ?: defaultThumb,
            videoUri = videoUri,
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        _uiState.update { it.copy(videos = listOf(video) + it.videos) }
        addActivity("Uploaded ${kind.wireValue} Video", video.title)
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.createVideo(video) }
        }
        return null
    }

    fun createStory(caption: String, mediaUri: String?): String? {
        val spam = SpamDetector.inspect(caption, enforceRateLimit = false)
        if (spam.isSpam) {
            return if (_uiState.value.language == AppLanguage.BN) spam.reasonBn else spam.reasonEn
        }
        val ch = _uiState.value.currentChannel
        val story = StoryItem(
            id = "story_${System.currentTimeMillis()}",
            userId = ch.userId,
            authorName = ch.displayName,
            caption = caption.trim(),
            mediaKey = mediaUri ?: "drawable:img_cover_studio",
            expiresAtMs = System.currentTimeMillis() + 24L * 3600L * 1000L
        )
        _uiState.update { state ->
            val now = System.currentTimeMillis()
            state.copy(stories = listOf(story) + state.stories.filter { it.expiresAtMs > now })
        }
        addActivity("Shared Story (24h)", story.caption)
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.createStory(story) }
        }
        return null
    }

    fun startLiveBroadcast(title: String) {
        val ch = _uiState.value.currentChannel
        val cleanTitle = title.trim().ifBlank { "${ch.displayName} Live Stream" }
        _uiState.update { state ->
            state.copy(
                liveSession = LiveStreamSession(
                    id = "live_${System.currentTimeMillis()}",
                    hostId = ch.userId,
                    hostName = ch.displayName,
                    title = cleanTitle,
                    viewersCount = 18,
                    isLive = true,
                    messages = listOf(
                        LiveChatMessage("lc_start", "RF TUBE", "Stream started — welcome viewers!")
                    )
                )
            )
        }
        addActivity("Started Live Stream", cleanTitle)
    }

    fun endLiveBroadcast() {
        _uiState.update { state ->
            state.copy(liveSession = state.liveSession.copy(isLive = false))
        }
        addActivity("Ended Live Stream", _uiState.value.liveSession.title)
    }

    fun sendLiveChat(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val ch = _uiState.value.currentChannel
        _uiState.update { state ->
            val msg = LiveChatMessage("lc_${System.currentTimeMillis()}", ch.displayName, clean)
            state.copy(
                liveSession = state.liveSession.copy(
                    messages = state.liveSession.messages + msg,
                    viewersCount = state.liveSession.viewersCount + 1
                )
            )
        }
    }

    fun recordVideoWatch(video: VideoItem, watchedSeconds: Long) {
        _uiState.update { state ->
            val entry = WatchHistoryEntry(
                id = "h_${video.id}",
                videoId = video.id,
                title = video.title,
                authorName = video.authorName,
                watchedSec = watchedSeconds,
                thumbKey = video.thumbKey,
                timestampLabel = "Just now"
            )
            val filtered = state.watchHistory.filterNot { it.videoId == video.id }
            state.copy(
                watchHistory = listOf(entry) + filtered,
                preferredTags = state.preferredTags + video.tags,
                videos = state.videos.map {
                    if (it.id == video.id) it.copy(
                        viewsCount = it.viewsCount + 1,
                        watchTimeSec = it.watchTimeSec + watchedSeconds
                    ) else it
                }
            )
        }
        if (!isDemoMode && repository != null && !video.id.startsWith("vid_")) {
            viewModelScope.launch { repository.recordVideoMetrics(video.id, 1L, watchedSeconds, 0L) }
        }
    }

    fun clearWatchHistory() {
        _uiState.update { it.copy(watchHistory = emptyList()) }
        addActivity("Cleared Watch History", "All history entries cleared")
    }

    fun createPlaylist(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val pl = UserPlaylist(
            id = "pl_${System.currentTimeMillis()}",
            userId = userId,
            title = clean,
            videoIds = emptyList()
        )
        _uiState.update { it.copy(playlists = listOf(pl) + it.playlists) }
        addActivity("Created Playlist", clean)
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.savePlaylist(pl) }
        }
    }

    fun addVideoToPlaylist(playlistId: String, videoId: String) {
        var updatedPlaylist: UserPlaylist? = null
        _uiState.update { state ->
            val next = state.playlists.map { pl ->
                if (pl.id == playlistId && videoId !in pl.videoIds) {
                    pl.copy(videoIds = pl.videoIds + videoId).also { updatedPlaylist = it }
                } else pl
            }
            state.copy(playlists = next)
        }
        val pl = updatedPlaylist ?: return
        addActivity("Added to Playlist", pl.title)
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.savePlaylist(pl) }
        }
    }

    fun markNotificationRead(id: String) {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { if (it.id == id) it.copy(isRead = true) else it })
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }
    }

    fun submitReport(targetType: ReportTargetType, targetId: String, reason: String) {
        val clean = reason.trim()
        if (clean.length < 3) return
        val report = ModerationReport(
            id = "rep_${System.currentTimeMillis()}",
            reporterId = userId,
            targetType = targetType.wireValue,
            targetId = targetId,
            reason = clean,
            status = ReportStatus.OPEN.wireValue
        )
        _uiState.update { it.copy(reports = listOf(report) + it.reports) }
        addActivity("Submitted Report", "${targetType.wireValue}: $clean")
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.submitReport(report) }
        }
    }

    fun adminRemoveContent(report: ModerationReport) {
        _uiState.update { state ->
            state.copy(
                posts = if (report.targetType == ReportTargetType.POST.wireValue) {
                    state.posts.filterNot { it.id == report.targetId }
                } else state.posts,
                videos = if (report.targetType == ReportTargetType.VIDEO.wireValue) {
                    state.videos.filterNot { it.id == report.targetId }
                } else state.videos,
                reports = state.reports.map {
                    if (it.id == report.id) it.copy(status = ReportStatus.RESOLVED.wireValue) else it
                }
            )
        }
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.resolveReport(report.id, ReportStatus.RESOLVED) }
        }
    }

    fun adminToggleBanUser(targetUserId: String, reportId: String?) {
        _uiState.update { state ->
            state.copy(
                channels = state.channels.map {
                    if (it.userId == targetUserId) it.copy(banned = !it.banned) else it
                },
                reports = if (reportId != null) {
                    state.reports.map { if (it.id == reportId) it.copy(status = ReportStatus.RESOLVED.wireValue) else it }
                } else state.reports
            )
        }
        if (reportId != null && !isDemoMode && repository != null) {
            viewModelScope.launch { repository.resolveReport(reportId, ReportStatus.RESOLVED) }
        }
    }

    fun adminDismissReport(reportId: String) {
        _uiState.update { state ->
            state.copy(
                reports = state.reports.map {
                    if (it.id == reportId) it.copy(status = ReportStatus.DISMISSED.wireValue) else it
                }
            )
        }
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.resolveReport(reportId, ReportStatus.DISMISSED) }
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            if (!isDemoMode && repository != null) {
                repository.deleteUserAccountData(userId)
            }
            onComplete()
        }
    }

    fun recommendedShorts(): List<VideoItem> {
        val s = _uiState.value
        val prefs = s.preferredTags
        return s.videos
            .filter { it.videoKind == VideoKind.SHORT.wireValue && !it.removed }
            .sortedByDescending { v ->
                val tagScore = v.tags.count { it in prefs } * 50000L
                val followScore = if (v.userId in s.followedIds) 25000L else 0L
                tagScore + followScore + v.likesCount
            }
    }

    private fun addActivity(action: String, detail: String) {
        val entry = UserActivityEntry(
            id = "act_${System.currentTimeMillis()}",
            userId = userId,
            action = action,
            detail = detail
        )
        _uiState.update { it.copy(activityLog = listOf(entry) + it.activityLog.take(30)) }
        if (!isDemoMode && repository != null) {
            viewModelScope.launch { repository.logActivity(action, detail) }
        }
    }
}
