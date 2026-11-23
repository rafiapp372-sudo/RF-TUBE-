package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localization.AppLanguage
import com.example.localization.LocalStrings
import com.example.model.ModerationReport
import com.example.model.PrivacyMode
import com.example.model.ReportStatus
import com.example.ui.components.RfTubeLogo
import com.example.ui.theme.MintPulse
import com.example.viewmodel.RfTubeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: RfTubeUiState,
    onToggleDarkMode: (Boolean) -> Unit,
    onChangeLanguage: (AppLanguage) -> Unit,
    onChangePrivacy: (PrivacyMode) -> Unit,
    onEnableTwoFactor: (String) -> Boolean,
    onDisableTwoFactor: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    BackHandler { onBack() }

    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.close)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance & Language
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.appearanceSection, style = MaterialTheme.typography.titleMedium)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.darkModeLabel, style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.darkMode,
                            onCheckedChange = onToggleDarkMode,
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }

                    Text(strings.languageLabel, style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = state.language == lang,
                                onClick = { onChangeLanguage(lang) },
                                label = { Text(lang.label) },
                                modifier = Modifier.testTag("settings_lang_${lang.code}")
                            )
                        }
                    }
                }
            }

            // Privacy
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.privacySection, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            PrivacyMode.PUBLIC to strings.privacyPublic,
                            PrivacyMode.FOLLOWERS to strings.privacyFollowers,
                            PrivacyMode.PRIVATE to strings.privacyPrivate
                        ).forEach { (mode, label) ->
                            FilterChip(
                                selected = state.privacyMode == mode,
                                onClick = { onChangePrivacy(mode) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Security & 2FA
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = MintPulse)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.securitySection, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(strings.twoFactorLabel, style = MaterialTheme.typography.bodyLarge)
                            Text(strings.twoFactorDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.twoFactorEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable) showTwoFactorDialog = true
                                else onDisableTwoFactor()
                            },
                            modifier = Modifier.testTag("settings_2fa_switch")
                        )
                    }
                }
            }

            // Account & Deletion
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.accountSection, style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().testTag("settings_logout_button")
                    ) {
                        Text(if (state.isDemoMode) strings.switchToGoogleBtn else strings.logoutBtn)
                    }
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("settings_delete_account_button")
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = strings.deleteAccountBtn)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.deleteAccountBtn)
                    }
                }
            }

            // About RF TUBE
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.aboutSection, style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RfTubeLogo(
                            size = 56.dp,
                            modifier = Modifier.testTag("settings_about_logo")
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.tagline,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = strings.aboutDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTwoFactorDialog) {
        var otpCode by remember { mutableStateOf("123456") }
        AlertDialog(
            onDismissRequest = { showTwoFactorDialog = false },
            title = { Text(strings.twoFactorLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.twoFactorCodePrompt, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (onEnableTwoFactor(otpCode)) {
                        showTwoFactorDialog = false
                        onShowMessage(strings.twoFactorEnabledMsg)
                    }
                }) {
                    Text(strings.verifyBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTwoFactorDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteAccountConfirmTitle) },
            text = { Text(strings.deleteAccountConfirmDesc) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.confirmDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    state: RfTubeUiState,
    onRemoveContent: (ModerationReport) -> Unit,
    onToggleBanUser: (targetUserId: String, reportId: String?) -> Unit,
    onDismissReport: (String) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    BackHandler { onBack() }

    var showOpenOnly by remember { mutableStateOf(true) }
    val filteredReports = remember(state.reports, showOpenOnly) {
        state.reports.filter {
            if (showOpenOnly) it.status == ReportStatus.OPEN.wireValue
            else it.status != ReportStatus.OPEN.wireValue
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.adminTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.close)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = showOpenOnly,
                    onClick = { showOpenOnly = true },
                    label = { Text(strings.openReports) }
                )
                FilterChip(
                    selected = !showOpenOnly,
                    onClick = { showOpenOnly = false },
                    label = { Text(strings.resolvedReports) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredReports.isEmpty()) {
                Text(strings.noReports, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredReports, key = { it.id }) { report ->
                        val targetUserId = when (report.targetType) {
                            "USER" -> report.targetId
                            "POST" -> state.posts.firstOrNull { it.id == report.targetId }?.userId
                            else -> state.videos.firstOrNull { it.id == report.targetId }?.userId
                        }
                        val targetChannel = state.channels.firstOrNull { it.userId == targetUserId }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            "${report.targetType} • ${report.targetId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(report.status, style = MaterialTheme.typography.labelSmall, color = MintPulse)
                                }

                                Text(report.reason, style = MaterialTheme.typography.bodyLarge)

                                if (report.status == ReportStatus.OPEN.wireValue) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { onRemoveContent(report) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(strings.removeContentBtn, maxLines = 1)
                                        }
                                        if (targetUserId != null) {
                                            OutlinedButton(
                                                onClick = { onToggleBanUser(targetUserId, report.id) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    if (targetChannel?.banned == true) strings.unbanUserBtn else strings.banUserBtn,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        TextButton(onClick = { onDismissReport(report.id) }) {
                                            Text(strings.dismissReportBtn)
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
