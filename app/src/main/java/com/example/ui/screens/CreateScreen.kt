package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.localization.LocalStrings
import com.example.model.CreateType
import com.example.model.VideoKind
import com.example.ui.components.MediaArt
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MintPulse
import com.example.viewmodel.RfTubeUiState

@Composable
fun CreateScreen(
    state: RfTubeUiState,
    initialType: CreateType = CreateType.POST,
    onCreatePost: (caption: String, mediaUri: String?) -> String?,
    onCreateVideo: (title: String, desc: String, tags: String, kind: VideoKind, thumbUri: String?, videoUri: String?) -> String?,
    onCreateStory: (caption: String, mediaUri: String?) -> String?,
    onStartLive: (title: String) -> Unit,
    onEndLive: () -> Unit,
    onSendLiveChat: (String) -> Unit,
    onPublishedNavigate: (CreateType) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val strings = LocalStrings.current
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var caption by remember { mutableStateOf("") }
    var videoTitle by remember { mutableStateOf("") }
    var videoDesc by remember { mutableStateOf("") }
    var tagsRaw by remember { mutableStateOf("") }
    var liveTitle by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pickedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var spamError by remember { mutableStateOf<String?>(null) }
    var showLiveRoom by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) pickedImageUri = uri
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) pickedVideoUri = uri
    }

    val tabs = listOf(
        CreateType.POST to strings.createPostTab,
        CreateType.LONG_VIDEO to strings.createLongVideoTab,
        CreateType.SHORT_VIDEO to strings.createShortVideoTab,
        CreateType.STORY to strings.createStoryTab,
        CreateType.LIVE to strings.goLiveTab
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            spamError = null
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("create_tab_${type.name}")
                    )
                }
            }

            if (spamError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = strings.spamBlockedTitle, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(strings.spamBlockedTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                            Text(spamError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedType) {
                        CreateType.POST -> {
                            OutlinedTextField(
                                value = caption,
                                onValueChange = { caption = it; spamError = null },
                                placeholder = { Text(strings.captionHint) },
                                minLines = 4,
                                modifier = Modifier.fillMaxWidth().testTag("create_post_caption")
                            )
                            OutlinedButton(
                                onClick = {
                                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = strings.pickPhotoBtn)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (pickedImageUri != null) strings.mediaSelected else strings.pickPhotoBtn)
                            }
                            if (pickedImageUri != null) {
                                MediaArt(
                                    mediaKey = pickedImageUri.toString(),
                                    contentDescription = strings.mediaSelected,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }
                            Button(
                                onClick = {
                                    val err = onCreatePost(caption, pickedImageUri?.toString())
                                    if (err != null) spamError = err
                                    else {
                                        caption = ""
                                        pickedImageUri = null
                                        onShowMessage(strings.publishedSuccess)
                                        onPublishedNavigate(CreateType.POST)
                                    }
                                },
                                enabled = caption.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publish_button")
                            ) {
                                Text(strings.publishBtn)
                            }
                        }

                        CreateType.LONG_VIDEO, CreateType.SHORT_VIDEO -> {
                            val isShort = selectedType == CreateType.SHORT_VIDEO
                            OutlinedTextField(
                                value = videoTitle,
                                onValueChange = { videoTitle = it; spamError = null },
                                placeholder = { Text(strings.videoTitleHint) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("create_video_title")
                            )
                            OutlinedTextField(
                                value = videoDesc,
                                onValueChange = { videoDesc = it; spamError = null },
                                placeholder = { Text(strings.videoDescHint) },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tagsRaw,
                                onValueChange = { tagsRaw = it },
                                placeholder = { Text(strings.tagsHint) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = strings.pickThumbBtn, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (pickedImageUri != null) "✓" else strings.pickThumbBtn, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.VideoFile, contentDescription = strings.pickVideoBtn, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (pickedVideoUri != null) "✓" else strings.pickVideoBtn, maxLines = 1)
                                }
                            }
                            if (pickedImageUri != null || pickedVideoUri != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MintPulse, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.mediaSelected, style = MaterialTheme.typography.bodySmall, color = MintPulse)
                                }
                            }
                            Button(
                                onClick = {
                                    val err = onCreateVideo(
                                        videoTitle,
                                        videoDesc,
                                        tagsRaw,
                                        if (isShort) VideoKind.SHORT else VideoKind.LONG,
                                        pickedImageUri?.toString(),
                                        pickedVideoUri?.toString()
                                    )
                                    if (err != null) spamError = err
                                    else {
                                        videoTitle = ""
                                        videoDesc = ""
                                        tagsRaw = ""
                                        pickedImageUri = null
                                        pickedVideoUri = null
                                        onShowMessage(strings.publishedSuccess)
                                        onPublishedNavigate(selectedType)
                                    }
                                },
                                enabled = videoTitle.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publish_video_button")
                            ) {
                                Text(strings.publishBtn)
                            }
                        }

                        CreateType.STORY -> {
                            Text(
                                strings.storyExpiresNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MintPulse
                            )
                            OutlinedTextField(
                                value = caption,
                                onValueChange = { caption = it; spamError = null },
                                placeholder = { Text(strings.captionHint) },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedButton(
                                onClick = {
                                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = strings.pickPhotoBtn)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (pickedImageUri != null) strings.mediaSelected else strings.pickPhotoBtn)
                            }
                            Button(
                                onClick = {
                                    val err = onCreateStory(caption, pickedImageUri?.toString())
                                    if (err != null) spamError = err
                                    else {
                                        caption = ""
                                        pickedImageUri = null
                                        onShowMessage(strings.publishedSuccess)
                                        onPublishedNavigate(CreateType.STORY)
                                    }
                                },
                                enabled = caption.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text(strings.publishBtn)
                            }
                        }

                        CreateType.LIVE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                MediaArt("drawable:img_cover_studio", strings.goLiveTab, Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x66000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LiveTv, contentDescription = strings.goLiveTab, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                            OutlinedTextField(
                                value = liveTitle,
                                onValueChange = { liveTitle = it },
                                placeholder = { Text(strings.videoTitleHint) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    onStartLive(liveTitle)
                                    showLiveRoom = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("start_live_button")
                            ) {
                                Text(strings.startLiveBtn)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLiveRoom) {
        LiveStreamRoomDialog(
            session = state.liveSession,
            isHost = true,
            onSendChat = onSendLiveChat,
            onEndLive = {
                onEndLive()
                showLiveRoom = false
            },
            onDismiss = { showLiveRoom = false }
        )
    }
}
