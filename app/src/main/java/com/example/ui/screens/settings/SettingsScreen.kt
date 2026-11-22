package com.example.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.components.RoyalLuxuryCard
import com.example.ui.components.RoyalOutlinedButton
import com.example.ui.components.RoyalStatusBadge

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { Text("Clear Conversation History?") },
            text = {
                Text(
                    text = "This permanently deletes all saved conversation threads and messages from your Royal GPT Firestore workspace.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearDialog = false
                    },
                    modifier = Modifier.testTag("settings_confirm_clear_button")
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (!statusMessage.isNullOrBlank()) {
                RoyalLuxuryCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearStatusMessage) {
                            Text("OK")
                        }
                    }
                }
            }

            SettingsSwitchRow(
                icon = Icons.Outlined.DarkMode,
                title = "Dark Theme",
                subtitle = "Luxury Obsidian & Royal Blue dark interface (Default)",
                checked = prefs.darkThemeEnabled,
                onCheckedChange = viewModel::setDarkTheme,
                switchTestTag = "settings_dark_theme_switch"
            )

            SettingsSwitchRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = "Workspace activity alerts & session updates",
                checked = prefs.notificationsEnabled,
                onCheckedChange = viewModel::setNotifications,
                switchTestTag = "settings_notifications_switch"
            )

            SettingsSwitchRow(
                icon = Icons.Outlined.Security,
                title = "Privacy & History Retention",
                subtitle = "Persist encrypted conversations to your Firestore account",
                checked = prefs.saveHistoryEnabled,
                onCheckedChange = viewModel::setSaveHistory,
                switchTestTag = "settings_privacy_history_switch"
            )

            RoyalOutlinedButton(
                text = "Clear All Conversation History",
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_clear_history_button"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )

            RoyalLuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoyalCrestLogo(size = 44.dp, animated = false)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_about_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_about_version),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    RoyalStatusBadge(
                        text = "STAGE 1",
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Built natively with Kotlin, Jetpack Compose, Material 3, MVVM repositories, Navigation Compose, Firebase Auth, and Cloud Firestore zero-trust security rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTestTag: String
) {
    RoyalLuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(switchTestTag)
            )
        }
    }
}
