package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.attemptAutoSignIn
import com.example.auth.signOutUser
import com.example.data.DemoData
import com.example.data.RfTubeRepository
import com.example.localization.AppLanguage
import com.example.localization.LocalStrings
import com.example.model.ChannelProfile
import com.example.model.CreateType
import com.example.model.MainTab
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.model.VideoSubTab
import com.example.ui.components.RfTubeLogo
import com.example.ui.components.RfTubeSplashScreen
import com.example.ui.components.WelcomePosterModal
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.ChannelScreen
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.RfTubeTheme
import com.example.viewmodel.RfTubeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RfTubeRootApp()
        }
    }
}

@Composable
private fun RfTubeRootApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isDemoMode by rememberSaveable { mutableStateOf(false) }
    var authLanguage by rememberSaveable { mutableStateOf(AppLanguage.EN) }
    var showSplash by rememberSaveable { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isDemoMode = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null && !isDemoMode) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = { /* Stay on AuthScreen */ },
                scope = scope
            )
        }
    }

    if (showSplash) {
        RfTubeTheme(darkTheme = true, language = authLanguage) {
            RfTubeSplashScreen(onFinished = { showSplash = false })
        }
        return
    }

    val user = currentUser
    if (user == null && !isDemoMode) {
        RfTubeTheme(darkTheme = true, language = authLanguage) {
            AuthScreen(
                language = authLanguage,
                onLanguageChange = { authLanguage = it },
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onEnterDemoMode = { isDemoMode = true }
            )
        }
    } else {
        val sessionKey = user?.uid ?: DemoData.DEMO_UID
        key(sessionKey, isDemoMode) {
            val viewModel: RfTubeViewModel = viewModel(
                key = "${sessionKey}_$isDemoMode",
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val repo = if (!isDemoMode && user != null) {
                            val databaseId = app.getString(R.string.firestore_database_id)
                            val db = FirebaseFirestore.getInstance(databaseId)
                            RfTubeRepository(db)
                        } else null

                        RfTubeViewModel(
                            repository = repo,
                            userId = sessionKey,
                            userEmail = user?.email,
                            userDisplayName = user?.displayName,
                            isEmailVerified = user?.isEmailVerified ?: false,
                            isDemoMode = isDemoMode
                        )
                    }
                }
            )
            MainRfTubeExperience(
                viewModel = viewModel,
                onSignOut = {
                    if (isDemoMode) {
                        isDemoMode = false
                    } else {
                        signOutUser(credentialManager, scope) {
                            currentUser = null
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MainRfTubeExperience(
    viewModel: RfTubeViewModel,
    onSignOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RfTubeTheme(darkTheme = state.darkMode, language = state.language) {
        val strings = LocalStrings.current
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var currentTab by rememberSaveable { mutableStateOf(MainTab.FEEDS) }
        var videoSubTab by rememberSaveable { mutableStateOf(VideoSubTab.LONG) }
        var createInitialType by rememberSaveable { mutableStateOf(CreateType.POST) }
        var selectedLongVideo by remember { mutableStateOf<VideoItem?>(null) }
        var inspectedChannel by remember { mutableStateOf<ChannelProfile?>(null) }
        var showSettings by rememberSaveable { mutableStateOf(false) }
        var showAdminPanel by rememberSaveable { mutableStateOf(false) }
        var showWelcomePoster by rememberSaveable { mutableStateOf(true) }

        fun showToast(msg: String) {
            scope.launch { snackbarHostState.showSnackbar(msg) }
        }

        if (showWelcomePoster) {
            BackHandler { showWelcomePoster = false }
        } else if (currentTab != MainTab.FEEDS && !showSettings && !showAdminPanel && inspectedChannel == null) {
            BackHandler { currentTab = MainTab.FEEDS }
        }

        if (showSettings) {
            SettingsScreen(
                state = state,
                onToggleDarkMode = viewModel::setDarkMode,
                onChangeLanguage = viewModel::setLanguage,
                onChangePrivacy = viewModel::setPrivacyMode,
                onEnableTwoFactor = viewModel::enableTwoFactor,
                onDisableTwoFactor = viewModel::disableTwoFactor,
                onSignOut = {
                    showSettings = false
                    onSignOut()
                },
                onDeleteAccount = {
                    viewModel.deleteAccount {
                        showSettings = false
                        onSignOut()
                    }
                },
                onShowMessage = ::showToast,
                onBack = { showSettings = false }
            )
            return@RfTubeTheme
        }

        if (showAdminPanel) {
            AdminPanelScreen(
                state = state,
                onRemoveContent = viewModel::adminRemoveContent,
                onToggleBanUser = viewModel::adminToggleBanUser,
                onDismissReport = viewModel::adminDismissReport,
                onBack = { showAdminPanel = false }
            )
            return@RfTubeTheme
        }

        val unreadCount = remember(state.notifications) { state.notifications.count { !it.isRead } }

        val navItems = listOf(
            NavItemMeta(MainTab.FEEDS, strings.tabFeeds, Icons.Filled.DynamicFeed, Icons.Outlined.DynamicFeed, "nav_feeds"),
            NavItemMeta(MainTab.VIDEOS, strings.tabVideos, Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle, "nav_videos"),
            NavItemMeta(MainTab.CREATE, strings.tabCreate, Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline, "nav_create"),
            NavItemMeta(MainTab.NOTIFICATIONS, strings.tabNotifications, Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_notifications"),
            NavItemMeta(MainTab.CHANNEL, strings.tabChannel, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "nav_channel")
        )

        val hideTopBar = currentTab == MainTab.VIDEOS && videoSubTab == VideoSubTab.SHORT

        Box(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 720.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (!hideTopBar) {
                        RfTubeTopBar(
                            appName = strings.appName,
                            isDemoMode = state.isDemoMode,
                            demoLabel = strings.demoModeBanner
                        )
                    }
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(windowInsets = WindowInsets.navigationBars) {
                            navItems.forEach { item ->
                                val selected = currentTab == item.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        inspectedChannel = null
                                        currentTab = item.tab
                                    },
                                    icon = {
                                        if (item.tab == MainTab.NOTIFICATIONS && unreadCount > 0) {
                                            BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        }
                                    },
                                    label = { Text(item.label, maxLines = 1) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail(modifier = Modifier.fillMaxHeight()) {
                            Spacer(modifier = Modifier.weight(0.2f))
                            navItems.forEach { item ->
                                val selected = currentTab == item.tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        inspectedChannel = null
                                        currentTab = item.tab
                                    },
                                    icon = {
                                        if (item.tab == MainTab.NOTIFICATIONS && unreadCount > 0) {
                                            BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        }
                                    },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (currentTab) {
                            MainTab.FEEDS -> FeedScreen(
                                state = state,
                                onToggleLikePost = viewModel::toggleLikePost,
                                onToggleSave = viewModel::toggleSaveItem,
                                onToggleFollow = viewModel::toggleFollow,
                                onAddComment = viewModel::addComment,
                                onAddReply = viewModel::addReply,
                                onEditPost = viewModel::editPost,
                                onDeletePost = viewModel::deletePost,
                                onReport = viewModel::submitReport,
                                onSendLiveChat = viewModel::sendLiveChat,
                                onEndLive = viewModel::endLiveBroadcast,
                                onOpenCreateStory = {
                                    createInitialType = CreateType.STORY
                                    currentTab = MainTab.CREATE
                                },
                                onOpenVideo = { vid ->
                                    currentTab = MainTab.VIDEOS
                                    if (vid.videoKind == VideoKind.SHORT.wireValue) {
                                        videoSubTab = VideoSubTab.SHORT
                                    } else {
                                        videoSubTab = VideoSubTab.LONG
                                        selectedLongVideo = vid
                                    }
                                },
                                onOpenChannel = { ch ->
                                    inspectedChannel = ch
                                    currentTab = MainTab.CHANNEL
                                },
                                onShowMessage = ::showToast
                            )

                            MainTab.VIDEOS -> VideosScreen(
                                state = state,
                                subTab = videoSubTab,
                                onSubTabChange = { videoSubTab = it },
                                selectedVideo = selectedLongVideo,
                                onSelectVideo = { selectedLongVideo = it },
                                recommendedShorts = viewModel.recommendedShorts(),
                                onToggleLikeVideo = viewModel::toggleLikeVideo,
                                onToggleSave = viewModel::toggleSaveItem,
                                onToggleFollow = viewModel::toggleFollow,
                                onAddComment = viewModel::addComment,
                                onAddReply = viewModel::addReply,
                                onRecordWatch = viewModel::recordVideoWatch,
                                onClearHistory = viewModel::clearWatchHistory,
                                onCreatePlaylist = viewModel::createPlaylist,
                                onAddToPlaylist = viewModel::addVideoToPlaylist,
                                onReport = viewModel::submitReport,
                                onShowMessage = ::showToast
                            )

                            MainTab.CREATE -> CreateScreen(
                                state = state,
                                initialType = createInitialType,
                                onCreatePost = viewModel::createPost,
                                onCreateVideo = viewModel::createVideo,
                                onCreateStory = viewModel::createStory,
                                onStartLive = viewModel::startLiveBroadcast,
                                onEndLive = viewModel::endLiveBroadcast,
                                onSendLiveChat = viewModel::sendLiveChat,
                                onPublishedNavigate = { type ->
                                    when (type) {
                                        CreateType.POST, CreateType.STORY -> currentTab = MainTab.FEEDS
                                        CreateType.LONG_VIDEO -> {
                                            videoSubTab = VideoSubTab.LONG
                                            currentTab = MainTab.VIDEOS
                                        }
                                        CreateType.SHORT_VIDEO -> {
                                            videoSubTab = VideoSubTab.SHORT
                                            currentTab = MainTab.VIDEOS
                                        }
                                        CreateType.LIVE -> currentTab = MainTab.FEEDS
                                    }
                                },
                                onShowMessage = ::showToast
                            )

                            MainTab.NOTIFICATIONS -> NotificationsScreen(
                                state = state,
                                onMarkRead = viewModel::markNotificationRead,
                                onMarkAllRead = viewModel::markAllNotificationsRead
                            )

                            MainTab.CHANNEL -> ChannelScreen(
                                state = state,
                                inspectedChannel = inspectedChannel,
                                onClearInspectedChannel = { inspectedChannel = null },
                                onUpdateProfile = viewModel::updateProfile,
                                onToggleFollow = viewModel::toggleFollow,
                                onOpenVideo = { vid ->
                                    currentTab = MainTab.VIDEOS
                                    if (vid.videoKind == VideoKind.SHORT.wireValue) {
                                        videoSubTab = VideoSubTab.SHORT
                                    } else {
                                        videoSubTab = VideoSubTab.LONG
                                        selectedLongVideo = vid
                                    }
                                },
                                onCreatePlaylist = viewModel::createPlaylist,
                                onClearHistory = viewModel::clearWatchHistory,
                                onOpenSettings = { showSettings = true },
                                onOpenAdminPanel = { showAdminPanel = true }
                            )
                        }
                    }
                }
            }
            }

            WelcomePosterModal(
                visible = showWelcomePoster,
                onDismiss = { showWelcomePoster = false }
            )
        }
    }
}

@Composable
private fun RfTubeTopBar(
    appName: String,
    isDemoMode: Boolean,
    demoLabel: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RfTubeLogo(
                    size = 38.dp,
                    modifier = Modifier.testTag("topbar_app_logo")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isDemoMode) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = demoLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private data class NavItemMeta(
    val tab: MainTab,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
