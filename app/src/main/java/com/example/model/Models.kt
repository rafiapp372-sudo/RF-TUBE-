package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class MainTab {
    FEEDS, VIDEOS, CREATE, NOTIFICATIONS, CHANNEL
}

enum class VideoSubTab {
    LONG, SHORT
}

enum class CreateType {
    POST, LONG_VIDEO, SHORT_VIDEO, STORY, LIVE
}

enum class PrivacyMode(val wireValue: String) {
    PUBLIC("PUBLIC"),
    FOLLOWERS("FOLLOWERS"),
    PRIVATE("PRIVATE");

    companion object {
        fun fromWire(raw: String?): PrivacyMode =
            entries.firstOrNull { it.wireValue == raw } ?: PUBLIC
    }
}

enum class PostKind(val wireValue: String) {
    TEXT("TEXT"),
    PHOTO("PHOTO"),
    LIVE("LIVE");

    companion object {
        fun fromWire(raw: String?): PostKind =
            entries.firstOrNull { it.wireValue == raw } ?: TEXT
    }
}

enum class VideoKind(val wireValue: String) {
    LONG("LONG"),
    SHORT("SHORT");

    companion object {
        fun fromWire(raw: String?): VideoKind =
            entries.firstOrNull { it.wireValue == raw } ?: LONG
    }
}

enum class NotificationKind {
    LIKE, COMMENT, FOLLOW, LIVE, MODERATION
}

enum class ReportTargetType(val wireValue: String) {
    POST("POST"),
    VIDEO("VIDEO"),
    USER("USER");

    companion object {
        fun fromWire(raw: String?): ReportTargetType =
            entries.firstOrNull { it.wireValue == raw } ?: POST
    }
}

enum class ReportStatus(val wireValue: String) {
    OPEN("OPEN"),
    RESOLVED("RESOLVED"),
    DISMISSED("DISMISSED");

    companion object {
        fun fromWire(raw: String?): ReportStatus =
            entries.firstOrNull { it.wireValue == raw } ?: OPEN
    }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class ChannelProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val handle: String = "",
    val bio: String = "",
    val avatarKey: String? = null,
    val coverKey: String? = null,
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    @get:PropertyName("isBanned") @PropertyName("isBanned")
    val banned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("displayName", displayName.take(60))
        put("handle", handle.take(35))
        if (bio.isNotBlank()) put("bio", bio.take(300))
        if (!avatarKey.isNullOrBlank()) put("avatarKey", avatarKey.take(300))
        if (!coverKey.isNullOrBlank()) put("coverKey", coverKey.take(300))
        put("followersCount", followersCount.coerceAtLeast(0L))
        put("followingCount", followingCount.coerceAtLeast(0L))
        put("isBanned", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class UserPrivateSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String? = null,
    val language: String = "en",
    val darkMode: Boolean = true,
    val privacyMode: String = PrivacyMode.PUBLIC.wireValue,
    val twoFactorEnabled: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toWriteMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        if (!email.isNullOrBlank()) put("email", email.take(120))
        put("language", if (language == "bn") "bn" else "en")
        put("darkMode", darkMode)
        put("privacyMode", PrivacyMode.fromWire(privacyMode).wireValue)
        put("twoFactorEnabled", twoFactorEnabled)
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class FeedPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val caption: String = "",
    val postType: String = PostKind.TEXT.wireValue,
    val mediaKey: String? = null,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    @get:PropertyName("isRemoved") @PropertyName("isRemoved")
    val removed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("authorName", authorName.take(60))
        put("authorHandle", authorHandle.take(35))
        put("caption", caption.take(1500))
        put("postType", PostKind.fromWire(postType).wireValue)
        if (!mediaKey.isNullOrBlank()) put("mediaKey", mediaKey.take(300))
        put("likesCount", likesCount.coerceAtLeast(0L))
        put("commentsCount", commentsCount.coerceAtLeast(0L))
        put("isRemoved", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class VideoItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val videoKind: String = VideoKind.LONG.wireValue,
    val durationSec: Long = 60L,
    val viewsCount: Long = 0L,
    val watchTimeSec: Long = 0L,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val thumbKey: String? = null,
    val videoUri: String? = null,
    @get:PropertyName("isRemoved") @PropertyName("isRemoved")
    val removed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("authorName", authorName.take(60))
        put("authorHandle", authorHandle.take(35))
        put("title", title.take(140))
        put("description", description.take(2000))
        put("tags", tags.take(10))
        put("videoKind", VideoKind.fromWire(videoKind).wireValue)
        put("durationSec", durationSec.coerceIn(1L, 43200L))
        put("viewsCount", viewsCount.coerceAtLeast(0L))
        put("watchTimeSec", watchTimeSec.coerceAtLeast(0L))
        put("likesCount", likesCount.coerceAtLeast(0L))
        put("commentsCount", commentsCount.coerceAtLeast(0L))
        if (!thumbKey.isNullOrBlank()) put("thumbKey", thumbKey.take(300))
        put("isRemoved", false)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class StoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val caption: String = "",
    val mediaKey: String? = null,
    val expiresAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("authorName", authorName.take(60))
        put("caption", caption.take(240))
        if (!mediaKey.isNullOrBlank()) put("mediaKey", mediaKey.take(300))
        put("expiresAtMs", expiresAtMs)
        put("createdAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class ModerationReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = ReportTargetType.POST.wireValue,
    val targetId: String = "",
    val reason: String = "",
    val status: String = ReportStatus.OPEN.wireValue,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "reporterId" to reporterId,
        "targetType" to ReportTargetType.fromWire(targetType).wireValue,
        "targetId" to targetId.take(128),
        "reason" to reason.take(300),
        "status" to ReportStatus.OPEN.wireValue,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class UserPlaylist(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val videoIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(80),
        "videoIds" to videoIds.take(50),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class UserSavedRef(
    @DocumentId val id: String = "",
    val userId: String = "",
    val itemType: String = "POST",
    val targetId: String = "",
    val title: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "itemType" to itemType,
        "targetId" to targetId.take(128),
        "title" to title.take(140),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class UserActivityEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val action: String = "",
    val detail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "action" to action.take(60),
        "detail" to detail.take(240),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

data class CommentReply(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val text: String,
    val timeAgo: String
)

data class CommentItem(
    val id: String,
    val targetId: String,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val text: String,
    val timeAgo: String,
    val replies: List<CommentReply> = emptyList()
)

data class NotificationItem(
    val id: String,
    val kind: NotificationKind,
    val actorName: String,
    val messageEn: String,
    val messageBn: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val targetId: String? = null
)

data class WatchHistoryEntry(
    val id: String,
    val videoId: String,
    val title: String,
    val authorName: String,
    val watchedSec: Long,
    val thumbKey: String?,
    val timestampLabel: String
)

data class LiveChatMessage(
    val id: String,
    val senderName: String,
    val text: String
)

data class LiveStreamSession(
    val id: String,
    val hostId: String,
    val hostName: String,
    val title: String,
    val viewersCount: Int,
    val isLive: Boolean,
    val messages: List<LiveChatMessage> = emptyList()
)
