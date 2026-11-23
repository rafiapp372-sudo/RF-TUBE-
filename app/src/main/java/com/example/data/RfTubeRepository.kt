package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ChannelProfile
import com.example.model.FeedPost
import com.example.model.ModerationReport
import com.example.model.ReportStatus
import com.example.model.StoryItem
import com.example.model.UserActivityEntry
import com.example.model.UserPlaylist
import com.example.model.UserPrivateSettings
import com.example.model.UserSavedRef
import com.example.model.VideoItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RfTubeRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.isEmailVerified && user.email == "rafiapp372@gmail.com"
    }

    private fun <T> Flow<T>.logAndRethrowFirestoreErrors(path: String): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser != null) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
    }

    fun observeChannels(): Flow<List<ChannelProfile>> = flow {
        requireUserId()
        emitAll(
            db.collection("channels")
                .whereEqualTo("isBanned", false)
                .snapshots()
                .map { snap -> snap.toObjects(ChannelProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors("channels")
        )
    }

    fun observePosts(): Flow<List<FeedPost>> = flow {
        requireUserId()
        emitAll(
            db.collection("posts")
                .whereEqualTo("isRemoved", false)
                .snapshots()
                .map { snap -> snap.toObjects(FeedPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors("posts")
        )
    }

    fun observeVideos(): Flow<List<VideoItem>> = flow {
        requireUserId()
        emitAll(
            db.collection("videos")
                .whereEqualTo("isRemoved", false)
                .snapshots()
                .map { snap -> snap.toObjects(VideoItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors("videos")
        )
    }

    fun observeStories(): Flow<List<StoryItem>> = flow {
        requireUserId()
        emitAll(
            db.collection("stories")
                .whereGreaterThan("expiresAtMs", 0L)
                .snapshots()
                .map { snap -> snap.toObjects(StoryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors("stories")
        )
    }

    fun observePrivateSettings(userId: String): Flow<UserPrivateSettings?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user private settings access." }
        val path = "users/$uid/private/settings"
        emitAll(
            db.collection("users").document(uid).collection("private").document("settings")
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserPrivateSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .logAndRethrowFirestoreErrors(path)
        )
    }

    fun observePlaylists(userId: String): Flow<List<UserPlaylist>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/playlists"
        emitAll(
            db.collection("users").document(uid).collection("playlists")
                .snapshots()
                .map { snap -> snap.toObjects(UserPlaylist::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors(path)
        )
    }

    fun observeSaved(userId: String): Flow<List<UserSavedRef>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/saved"
        emitAll(
            db.collection("users").document(uid).collection("saved")
                .snapshots()
                .map { snap -> snap.toObjects(UserSavedRef::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors(path)
        )
    }

    fun observeActivity(userId: String): Flow<List<UserActivityEntry>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/activity"
        emitAll(
            db.collection("users").document(uid).collection("activity")
                .snapshots()
                .map { snap -> snap.toObjects(UserActivityEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors(path)
        )
    }

    fun observeReports(): Flow<List<ModerationReport>> = flow {
        val uid = requireUserId()
        val query = if (isCurrentUserAdmin()) {
            db.collection("reports")
        } else {
            db.collection("reports").whereEqualTo("reporterId", uid)
        }
        emitAll(
            query.snapshots()
                .map { snap -> snap.toObjects(ModerationReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .logAndRethrowFirestoreErrors("reports")
        )
    }

    suspend fun upsertChannelProfile(profile: ChannelProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "channels/$uid"
        return try {
            val docRef = db.collection("channels").document(uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(profile.copy(userId = uid).toCreateMap()).await()
            } else {
                val updates = buildMap<String, Any> {
                    put("displayName", profile.displayName.take(60))
                    put("handle", profile.handle.take(35))
                    put("bio", profile.bio.take(300))
                    if (!profile.avatarKey.isNullOrBlank()) put("avatarKey", profile.avatarKey.take(300))
                    if (!profile.coverKey.isNullOrBlank()) put("coverKey", profile.coverKey.take(300))
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun savePrivateSettings(settings: UserPrivateSettings): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/private/settings"
        return try {
            val docRef = db.collection("users").document(uid).collection("private").document("settings")
            docRef.set(settings.copy(userId = uid).toWriteMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun createPost(post: FeedPost): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("posts").document()
        return try {
            docRef.set(post.copy(userId = uid).toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updatePostCaption(postId: String, caption: String): Result<Unit> {
        val path = "posts/$postId"
        return try {
            requireUserId()
            require(postId.isNotBlank())
            db.collection("posts").document(postId).update(
                mapOf(
                    "caption" to caption.take(1500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementPostLikes(postId: String, delta: Long): Result<Unit> {
        val path = "posts/$postId"
        return try {
            requireUserId()
            db.collection("posts").document(postId).update(
                mapOf(
                    "likesCount" to FieldValue.increment(delta),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        val path = "posts/$postId"
        return try {
            requireUserId()
            db.collection("posts").document(postId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun createVideo(video: VideoItem): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("videos").document()
        return try {
            docRef.set(video.copy(userId = uid).toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordVideoMetrics(videoId: String, viewsDelta: Long, watchTimeDelta: Long, likesDelta: Long): Result<Unit> {
        val path = "videos/$videoId"
        return try {
            requireUserId()
            val updates = buildMap<String, Any> {
                if (viewsDelta != 0L) put("viewsCount", FieldValue.increment(viewsDelta))
                if (watchTimeDelta != 0L) put("watchTimeSec", FieldValue.increment(watchTimeDelta))
                if (likesDelta != 0L) put("likesCount", FieldValue.increment(likesDelta))
                put("updatedAt", FieldValue.serverTimestamp())
            }
            db.collection("videos").document(videoId).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun createStory(story: StoryItem): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("stories").document()
        return try {
            docRef.set(story.copy(userId = uid).toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun savePlaylist(playlist: UserPlaylist): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val col = db.collection("users").document(uid).collection("playlists")
        val docRef = if (playlist.id.isBlank()) col.document() else col.document(playlist.id)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(playlist.copy(userId = uid).toCreateMap()).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to playlist.title.take(80),
                        "videoIds" to playlist.videoIds.take(50),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleSavedRef(ref: UserSavedRef, save: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = "${ref.itemType}_${ref.targetId}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val docRef = db.collection("users").document(uid).collection("saved").document(docId)
        return try {
            if (save) {
                docRef.set(ref.copy(userId = uid).toCreateMap()).await()
            } else {
                docRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun logActivity(action: String, detail: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("users").document(uid).collection("activity").document()
        return try {
            docRef.set(UserActivityEntry(userId = uid, action = action, detail = detail).toCreateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitReport(report: ModerationReport): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("reports").document()
        return try {
            docRef.set(report.copy(reporterId = uid).toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun resolveReport(reportId: String, status: ReportStatus): Result<Unit> {
        val path = "reports/$reportId"
        return try {
            requireUserId()
            db.collection("reports").document(reportId).update(
                mapOf(
                    "status" to status.wireValue,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccountData(userId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (uid != userId) return Result.failure(IllegalStateException("UID mismatch"))
        return try {
            db.collection("users").document(uid).collection("private").document("settings").delete().await()
            db.collection("channels").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "channels/$uid")
            Result.failure(e)
        }
    }
}
