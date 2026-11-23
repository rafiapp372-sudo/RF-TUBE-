package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.QrCodeGenerator
import com.example.localization.LocalStrings
import com.example.model.ChannelProfile
import com.example.model.CommentItem
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.MintPulse
import com.example.ui.theme.SunsetEmber

fun formatCompactCount(count: Long): String = when {
    count >= 1_000_000L -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000L -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}

@Composable
fun MediaArt(
    mediaKey: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (mediaKey) {
        "drawable:img_cover_studio" -> R.drawable.img_cover_studio
        "drawable:img_thumb_dhaka_night" -> R.drawable.img_thumb_dhaka_night
        "drawable:img_feed_streetfood" -> R.drawable.img_feed_streetfood
        "drawable:img_short_neon_dance" -> R.drawable.img_short_neon_dance
        "drawable:rf_tube_icon" -> R.drawable.rf_tube_icon
        else -> null
    }

    when {
        drawableRes != null -> {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        !mediaKey.isNullOrBlank() && (mediaKey.startsWith("content://") || mediaKey.startsWith("file://") || mediaKey.startsWith("http")) -> {
            AsyncImage(
                model = Uri.parse(mediaKey),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> {
            Box(
                modifier = modifier.background(
                    Brush.linearGradient(listOf(CrimsonCoral, SunsetEmber, ElectricAmber))
                )
            )
        }
    }
}

@Composable
fun RfTubeLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.rf_tube_logo),
        contentDescription = "RF TUBE Logo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}

@Composable
fun AuthorAvatar(
    name: String,
    avatarKey: String?,
    size: Dp = 42.dp,
    hasStoryRing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = remember(name) {
        val hash = kotlin.math.abs(name.hashCode())
        when (hash % 4) {
            0 -> listOf(CrimsonCoral, SunsetEmber)
            1 -> listOf(MintPulse, Color(0xFF0088FF))
            2 -> listOf(ElectricAmber, CrimsonCoral)
            else -> listOf(Color(0xFF7C4DFF), CrimsonCoral)
        }
    }

    val ringModifier = if (hasStoryRing) {
        Modifier
            .size(size + 6.dp)
            .clip(CircleShape)
            .background(Brush.sweepGradient(listOf(CrimsonCoral, SunsetEmber, ElectricAmber, MintPulse, CrimsonCoral)))
            .padding(2.5.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(2.dp)
    } else {
        Modifier.size(size)
    }

    Box(
        modifier = modifier.then(ringModifier).clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarKey.isNullOrBlank() && (avatarKey.startsWith("content://") || avatarKey.startsWith("file://"))) {
            AsyncImage(
                model = Uri.parse(avatarKey),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(palette)),
                contentAlignment = Alignment.Center
            ) {
                val initials = name.trim().split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")
                    .ifBlank { "RF" }
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QrProfileDialog(
    channel: ChannelProfile,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val qrPayload = "rftube://channel/${channel.handle}"
    val matrix = remember(qrPayload) { QrCodeGenerator.generate(qrPayload) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = strings.qrProfile,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.qrProfile, style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                AuthorAvatar(name = channel.displayName, avatarKey = channel.avatarKey, size = 56.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(channel.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    channel.handle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .size(210.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val n = matrix.size
                        val cell = size.width / n.toFloat()
                        for (r in 0 until n) {
                            for (c in 0 until n) {
                                if (matrix[r][c]) {
                                    drawRect(
                                        color = Color(0xFF0B0D14),
                                        topLeft = Offset(c * cell, r * cell),
                                        size = Size(cell + 0.5f, cell + 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.qrScanNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CommentsDialog(
    targetTitle: String,
    comments: List<CommentItem>,
    onAddComment: (String) -> String?,
    onAddReply: (commentId: String, text: String) -> String?,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var inputText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<CommentItem?>(null) }
    var spamWarning by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 340.dp, max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.commentsTitle, style = MaterialTheme.typography.titleLarge)
                        Text(
                            targetTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AuthorAvatar(name = comment.authorName, avatarKey = null, size = 30.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        comment.authorName,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        comment.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(comment.text, style = MaterialTheme.typography.bodyMedium)

                                TextButton(
                                    onClick = { replyingTo = comment },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Reply,
                                        contentDescription = strings.reply,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.reply, style = MaterialTheme.typography.labelSmall)
                                }

                                comment.replies.forEach { reply ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp, top = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                "${reply.authorName} • ${reply.timeAgo}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(reply.text, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (spamWarning != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = spamWarning!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (replyingTo != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Text(
                            "${strings.replyingTo} ${replyingTo!!.authorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { replyingTo = null }) {
                            Text(strings.cancel, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it; spamWarning = null },
                        placeholder = { Text(strings.writeCommentHint) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val err = if (replyingTo != null) {
                                onAddReply(replyingTo!!.id, inputText)
                            } else {
                                onAddComment(inputText)
                            }
                            if (err != null) {
                                spamWarning = err
                            } else {
                                inputText = ""
                                replyingTo = null
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("send_comment_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.send,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDialog(
    targetLabel: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Flag, contentDescription = strings.report, tint = MaterialTheme.colorScheme.error) },
        title = { Text(strings.reportTitle) },
        text = {
            Column {
                Text(
                    targetLabel,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text(strings.reportReasonHint) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("report_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().length >= 3) {
                        onSubmit(reason.trim())
                        onDismiss()
                    }
                },
                enabled = reason.trim().length >= 3
            ) {
                Text(strings.submitReport)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun FollowersDialog(
    title: String,
    channels: List<ChannelProfile>,
    followedIds: Set<String>,
    currentUserId: String,
    onToggleFollow: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 460.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(channels.filter { it.userId != currentUserId }, key = { it.userId }) { ch ->
                        val isFollowing = ch.userId in followedIds
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuthorAvatar(name = ch.displayName, avatarKey = ch.avatarKey, size = 40.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ch.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${ch.handle} • ${formatCompactCount(ch.followersCount)} ${strings.followers}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isFollowing) {
                                OutlinedButton(onClick = { onToggleFollow(ch.userId) }) {
                                    Text(strings.following, style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(onClick = { onToggleFollow(ch.userId) }) {
                                    Text(strings.follow, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
