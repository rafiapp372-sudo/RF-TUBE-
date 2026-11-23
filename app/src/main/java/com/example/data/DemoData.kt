package com.example.data

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
import com.example.model.ReportStatus
import com.example.model.ReportTargetType
import com.example.model.StoryItem
import com.example.model.UserActivityEntry
import com.example.model.UserPlaylist
import com.example.model.UserSavedRef
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.model.WatchHistoryEntry
import com.google.firebase.Timestamp

object DemoData {
    const val DEMO_UID = "demo_rafi_01"

    fun demoChannels(): List<ChannelProfile> = listOf(
        ChannelProfile(
            id = DEMO_UID,
            userId = DEMO_UID,
            displayName = "Rafi Studio",
            handle = "@rafistudio",
            bio = "Filmmaker & full-stack creator from Dhaka. Sharing 16:9 tech films, street food vlogs & 9:16 neon shorts.",
            avatarKey = "avatar_rafi",
            coverKey = "drawable:img_cover_studio",
            followersCount = 18420L,
            followingCount = 142L,
            banned = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        ChannelProfile(
            id = "creator_tanisha",
            userId = "creator_tanisha",
            displayName = "Tanisha Noor",
            handle = "@tanishanoor",
            bio = "Travel stories & nocturnal cityscapes across Bangladesh.",
            avatarKey = "avatar_tanisha",
            coverKey = "drawable:img_thumb_dhaka_night",
            followersCount = 42900L,
            followingCount = 89L,
            banned = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        ChannelProfile(
            id = "creator_foodie",
            userId = "creator_foodie",
            displayName = "Dhaka Cravings",
            handle = "@dhakacravings",
            bio = "Authentic Old Dhaka street food, spice trails & late-night chai.",
            avatarKey = "avatar_foodie",
            coverKey = "drawable:img_feed_streetfood",
            followersCount = 67300L,
            followingCount = 210L,
            banned = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        ChannelProfile(
            id = "creator_rhythm",
            userId = "creator_rhythm",
            displayName = "Rhythm Pulse",
            handle = "@rhythmpulse",
            bio = "Freestyle choreography, neon beats & 9:16 shorts every evening.",
            avatarKey = "avatar_rhythm",
            coverKey = "drawable:img_short_neon_dance",
            followersCount = 91400L,
            followingCount = 64L,
            banned = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    )

    fun demoStories(nowMs: Long = System.currentTimeMillis()): List<StoryItem> = listOf(
        StoryItem(
            id = "story_1",
            userId = "creator_tanisha",
            authorName = "Tanisha Noor",
            caption = "Midnight shoot over Hatirjheel bridge tonight ✨",
            mediaKey = "drawable:img_thumb_dhaka_night",
            expiresAtMs = nowMs + 19L * 3600L * 1000L
        ),
        StoryItem(
            id = "story_2",
            userId = "creator_foodie",
            authorName = "Dhaka Cravings",
            caption = "Freshly crisped fuchka & smoky masala chai! 🔥",
            mediaKey = "drawable:img_feed_streetfood",
            expiresAtMs = nowMs + 14L * 3600L * 1000L
        ),
        StoryItem(
            id = "story_3",
            userId = "creator_rhythm",
            authorName = "Rhythm Pulse",
            caption = "Behind the scenes — Neon Rain Dance choreography",
            mediaKey = "drawable:img_short_neon_dance",
            expiresAtMs = nowMs + 22L * 3600L * 1000L
        ),
        StoryItem(
            id = DEMO_UID,
            userId = DEMO_UID,
            authorName = "Rafi Studio",
            caption = "Editing our next 4K episode in the studio!",
            mediaKey = "drawable:img_cover_studio",
            expiresAtMs = nowMs + 23L * 3600L * 1000L
        )
    )

    fun demoPosts(): List<FeedPost> = listOf(
        FeedPost(
            id = "post_1",
            userId = "creator_foodie",
            authorName = "Dhaka Cravings",
            authorHandle = "@dhakacravings",
            caption = "Golden-hour street food walk in Dhanmondi! Nothing beats hot crispy fuchka and spiced clay-cup chai after rain. #dhaka #streetfood #foodie",
            postType = PostKind.PHOTO.wireValue,
            mediaKey = "drawable:img_feed_streetfood",
            likesCount = 1284L,
            commentsCount = 2L,
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        FeedPost(
            id = "post_2",
            userId = DEMO_UID,
            authorName = "Rafi Studio",
            authorHandle = "@rafistudio",
            caption = "Upgraded the RF TUBE editing rig tonight! Dropping a complete creator workflow breakdown + Bengali/English dual subtitles soon. #studio #creator #tech",
            postType = PostKind.PHOTO.wireValue,
            mediaKey = "drawable:img_cover_studio",
            likesCount = 842L,
            commentsCount = 1L,
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        FeedPost(
            id = "post_3",
            userId = "creator_tanisha",
            authorName = "Tanisha Noor",
            authorHandle = "@tanishanoor",
            caption = "Question for night photographers: 24mm f/1.4 or 35mm f/1.8 for handheld neon city reflections? Drop your favorites below! #photography #dhaka",
            postType = PostKind.TEXT.wireValue,
            mediaKey = null,
            likesCount = 419L,
            commentsCount = 1L,
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    )

    fun demoVideos(): List<VideoItem> = listOf(
        VideoItem(
            id = "vid_long_1",
            userId = "creator_tanisha",
            authorName = "Tanisha Noor",
            authorHandle = "@tanishanoor",
            title = "Dhaka After Midnight: Cinematic 4K Neon Cityscape Tour",
            description = "A slow-cinema journey across illuminated bridges, river reflections, and midnight tea stalls of Dhaka. Shot in 4K 24fps with custom coral-cyan grading.",
            tags = listOf("dhaka", "cinema", "travel", "night"),
            videoKind = VideoKind.LONG.wireValue,
            durationSec = 742L,
            viewsCount = 48200L,
            watchTimeSec = 192800L,
            likesCount = 3910L,
            commentsCount = 2L,
            thumbKey = "drawable:img_thumb_dhaka_night",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        VideoItem(
            id = "vid_long_2",
            userId = DEMO_UID,
            authorName = "Rafi Studio",
            authorHandle = "@rafistudio",
            title = "Building a Pro Creator Desk & Dual-Monitor Video Studio",
            description = "Full desk tour, acoustic treatment tips, warm rim-lighting setup, and audio routing for crisp Bengali and English voiceovers.",
            tags = listOf("tech", "studio", "setup", "creator"),
            videoKind = VideoKind.LONG.wireValue,
            durationSec = 915L,
            viewsCount = 31450L,
            watchTimeSec = 141500L,
            likesCount = 2740L,
            commentsCount = 1L,
            thumbKey = "drawable:img_cover_studio",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        VideoItem(
            id = "vid_long_3",
            userId = "creator_foodie",
            authorName = "Dhaka Cravings",
            authorHandle = "@dhakacravings",
            title = "Old Dhaka Sunset Fuchka & Masala Chai Masterclass",
            description = "Exploring the secret tamarind water recipe and crisp semolina shells from third-generation street vendors.",
            tags = listOf("foodie", "streetfood", "dhaka", "vlog"),
            videoKind = VideoKind.LONG.wireValue,
            durationSec = 610L,
            viewsCount = 89400L,
            watchTimeSec = 312000L,
            likesCount = 7420L,
            commentsCount = 1L,
            thumbKey = "drawable:img_feed_streetfood",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        VideoItem(
            id = "vid_short_1",
            userId = "creator_rhythm",
            authorName = "Rhythm Pulse",
            authorHandle = "@rhythmpulse",
            title = "Neon Rain Freestyle — 60fps Beat Drop",
            description = "Wait for the bass switch at 0:12! Filmed in one continuous gimbal take under magenta & cyan rain lights.",
            tags = listOf("dance", "neon", "music", "shorts"),
            videoKind = VideoKind.SHORT.wireValue,
            durationSec = 28L,
            viewsCount = 154200L,
            watchTimeSec = 616800L,
            likesCount = 19300L,
            commentsCount = 1L,
            thumbKey = "drawable:img_short_neon_dance",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        VideoItem(
            id = "vid_short_2",
            userId = "creator_foodie",
            authorName = "Dhaka Cravings",
            authorHandle = "@dhakacravings",
            title = "15-Second Spicy Fuchka Crunch ASMR",
            description = "Turn your volume up for that golden crunch! #streetfood #asmr #dhaka",
            tags = listOf("streetfood", "foodie", "asmr"),
            videoKind = VideoKind.SHORT.wireValue,
            durationSec = 18L,
            viewsCount = 92800L,
            watchTimeSec = 278400L,
            likesCount = 11400L,
            commentsCount = 0L,
            thumbKey = "drawable:img_feed_streetfood",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        VideoItem(
            id = "vid_short_3",
            userId = "creator_tanisha",
            authorName = "Tanisha Noor",
            authorHandle = "@tanishanoor",
            title = "Twilight Timelapse Over Dhaka Lights",
            description = "From dusk to electric midnight in 22 seconds. #dhaka #cinema #timelapse",
            tags = listOf("dhaka", "cinema", "night"),
            videoKind = VideoKind.SHORT.wireValue,
            durationSec = 22L,
            viewsCount = 74100L,
            watchTimeSec = 222300L,
            likesCount = 8950L,
            commentsCount = 0L,
            thumbKey = "drawable:img_thumb_dhaka_night",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ),
        VideoItem(
            id = "vid_short_4",
            userId = DEMO_UID,
            authorName = "Rafi Studio",
            authorHandle = "@rafistudio",
            title = "3 Lighting Hacks for Night Creators",
            description = "Use warm key + crimson rim light to instantly level up your desk shots. #tech #studio #creator",
            tags = listOf("tech", "studio", "creator"),
            videoKind = VideoKind.SHORT.wireValue,
            durationSec = 32L,
            viewsCount = 41200L,
            watchTimeSec = 164800L,
            likesCount = 5120L,
            commentsCount = 0L,
            thumbKey = "drawable:img_cover_studio",
            removed = false,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    )

    fun demoComments(): Map<String, List<CommentItem>> = mapOf(
        "post_1" to listOf(
            CommentItem(
                id = "c_1",
                targetId = "post_1",
                authorId = "creator_tanisha",
                authorName = "Tanisha Noor",
                authorHandle = "@tanishanoor",
                text = "That masala chai lighting looks straight out of a movie!",
                timeAgo = "12m",
                replies = listOf(
                    CommentReply("r_1", "Dhaka Cravings", "@dhakacravings", "Thank you Tanisha! Sunset hour was magic today.", "8m")
                )
            ),
            CommentItem(
                id = "c_2",
                targetId = "post_1",
                authorId = DEMO_UID,
                authorName = "Rafi Studio",
                authorHandle = "@rafistudio",
                text = "Saving this spot for our weekend food vlog!",
                timeAgo = "5m"
            )
        ),
        "vid_long_1" to listOf(
            CommentItem(
                id = "c_3",
                targetId = "vid_long_1",
                authorId = DEMO_UID,
                authorName = "Rafi Studio",
                authorHandle = "@rafistudio",
                text = "Color grading at 04:15 is unreal. What LUT did you use?",
                timeAgo = "1h",
                replies = listOf(
                    CommentReply("r_2", "Tanisha Noor", "@tanishanoor", "Custom Phantom + Halation curve, sharing it in the description!", "42m")
                )
            )
        ),
        "vid_short_1" to listOf(
            CommentItem(
                id = "c_4",
                targetId = "vid_short_1",
                authorId = "creator_foodie",
                authorName = "Dhaka Cravings",
                authorHandle = "@dhakacravings",
                text = "Watched this 5 times on loop — cleanest footwork!",
                timeAgo = "24m"
            )
        )
    )

    fun demoNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "n_1",
            kind = NotificationKind.LIKE,
            actorName = "Tanisha Noor",
            messageEn = "liked your studio post and 16:9 breakdown.",
            messageBn = "আপনার স্টুডিও পোস্ট ও ১৬:৯ ভিডিওতে লাইক দিয়েছেন।",
            timeAgo = "4m",
            isRead = false,
            targetId = "post_2"
        ),
        NotificationItem(
            id = "n_2",
            kind = NotificationKind.COMMENT,
            actorName = "Dhaka Cravings",
            messageEn = "replied to your comment: \"Saving this spot!\"",
            messageBn = "আপনার মন্তব্যে উত্তর দিয়েছেন: \"উইকেন্ড ফুড ভ্লগের জন্য দারুণ!\"",
            timeAgo = "18m",
            isRead = false,
            targetId = "post_1"
        ),
        NotificationItem(
            id = "n_3",
            kind = NotificationKind.FOLLOW,
            actorName = "Rhythm Pulse",
            messageEn = "started following your RF TUBE channel.",
            messageBn = "আপনার RF TUBE চ্যানেল ফলো করা শুরু করেছেন।",
            timeAgo = "1h",
            isRead = true
        ),
        NotificationItem(
            id = "n_4",
            kind = NotificationKind.LIVE,
            actorName = "Tanisha Noor",
            messageEn = "is Live now: Midnight Q&A & Color Grading!",
            messageBn = "এখন লাইভে আছেন: মিডনাইট প্রশ্নোত্তর ও কালার গ্রেডিং!",
            timeAgo = "2h",
            isRead = false
        )
    )

    fun demoPlaylists(): List<UserPlaylist> = listOf(
        UserPlaylist(
            id = "pl_1",
            userId = DEMO_UID,
            title = "Dhaka Night Inspiration",
            videoIds = listOf("vid_long_1", "vid_short_3")
        ),
        UserPlaylist(
            id = "pl_2",
            userId = DEMO_UID,
            title = "Creator Gear & Food Vlogs",
            videoIds = listOf("vid_long_2", "vid_long_3")
        )
    )

    fun demoSaved(): List<UserSavedRef> = listOf(
        UserSavedRef(
            id = "saved_1",
            userId = DEMO_UID,
            itemType = "POST",
            targetId = "post_1",
            title = "Golden-hour street food walk in Dhanmondi!"
        ),
        UserSavedRef(
            id = "saved_2",
            userId = DEMO_UID,
            itemType = "VIDEO",
            targetId = "vid_long_1",
            title = "Dhaka After Midnight: Cinematic 4K Neon Cityscape Tour"
        )
    )

    fun demoWatchHistory(): List<WatchHistoryEntry> = listOf(
        WatchHistoryEntry(
            id = "hist_1",
            videoId = "vid_long_1",
            title = "Dhaka After Midnight: Cinematic 4K Neon Cityscape Tour",
            authorName = "Tanisha Noor",
            watchedSec = 310L,
            thumbKey = "drawable:img_thumb_dhaka_night",
            timestampLabel = "Today"
        ),
        WatchHistoryEntry(
            id = "hist_2",
            videoId = "vid_short_1",
            title = "Neon Rain Freestyle — 60fps Beat Drop",
            authorName = "Rhythm Pulse",
            watchedSec = 28L,
            thumbKey = "drawable:img_short_neon_dance",
            timestampLabel = "Yesterday"
        )
    )

    fun demoActivityLog(): List<UserActivityEntry> = listOf(
        UserActivityEntry(id = "act_1", userId = DEMO_UID, action = "Published Video", detail = "Building a Pro Creator Desk & Dual-Monitor Video Studio"),
        UserActivityEntry(id = "act_2", userId = DEMO_UID, action = "Saved Post", detail = "Golden-hour street food walk in Dhanmondi!"),
        UserActivityEntry(id = "act_3", userId = DEMO_UID, action = "Followed Creator", detail = "Tanisha Noor (@tanishanoor)")
    )

    fun demoReports(): List<ModerationReport> = listOf(
        ModerationReport(
            id = "rep_demo_1",
            reporterId = "creator_tanisha",
            targetType = ReportTargetType.POST.wireValue,
            targetId = "post_3",
            reason = "Off-topic promotional comment reported for review",
            status = ReportStatus.OPEN.wireValue
        ),
        ModerationReport(
            id = "rep_demo_2",
            reporterId = "creator_foodie",
            targetType = ReportTargetType.VIDEO.wireValue,
            targetId = "vid_short_2",
            reason = "Duplicate audio watermark check",
            status = ReportStatus.RESOLVED.wireValue
        )
    )

    fun demoLiveSession(): LiveStreamSession = LiveStreamSession(
        id = "live_tanisha",
        hostId = "creator_tanisha",
        hostName = "Tanisha Noor",
        title = "Midnight Dhaka Bridge Shoot & Live Q&A",
        viewersCount = 1428,
        isLive = true,
        messages = listOf(
            LiveChatMessage("lc_1", "Rafi Studio", "Exposure looks super clean!"),
            LiveChatMessage("lc_2", "Dhaka Cravings", "Bring the camera near TSC next!"),
            LiveChatMessage("lc_3", "Rhythm Pulse", "Love the cyan reflections 🔥")
        )
    )
}
