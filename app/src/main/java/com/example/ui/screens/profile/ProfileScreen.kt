package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.components.RoyalLuxuryCard
import com.example.ui.components.RoyalOutlinedButton
import com.example.ui.components.RoyalStatusBadge
import com.example.ui.screens.home.UiState

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    fallbackName: String,
    fallbackEmail: String,
    fallbackPhotoUrl: String?,
    fallbackVerified: Boolean,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val loadedProfile = (profileState as? UiState.Success)?.data
    val displayName = loadedProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: fallbackName.ifBlank { "Royal Member" }
    val email = loadedProfile?.email?.takeIf { it.isNotBlank() } ?: fallbackEmail
    val photoUrl = loadedProfile?.photoUrl ?: fallbackPhotoUrl
    val isVerified = loadedProfile?.emailVerified ?: fallbackVerified

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember(displayName) { mutableStateOf(displayName) }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = { Text("Update Display Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_edit_name_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDisplayName(editedName)
                        showEditNameDialog = false
                    },
                    modifier = Modifier.testTag("profile_save_name_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
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
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.nav_profile),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            RoyalLuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "User avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            RoyalCrestLogo(size = 48.dp, animated = false)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    editedName = displayName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("profile_edit_name_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit display name",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (email.isNotBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        RoyalStatusBadge(
                            text = if (isVerified) "VERIFIED IDENTITY" else "VERIFICATION READY",
                            accentColor = if (isVerified) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }

            if (!statusMessage.isNullOrBlank()) {
                RoyalLuxuryCard(
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        TextButton(onClick = viewModel::clearStatusMessage) {
                            Text("OK")
                        }
                    }
                }
            }

            ProfileMenuRow(
                icon = Icons.Outlined.ManageAccounts,
                title = "Account Settings",
                subtitle = "Edit display name & inspect verified Google provider",
                onClick = {
                    editedName = displayName
                    showEditNameDialog = true
                },
                modifier = Modifier.testTag("profile_account_settings_row")
            )

            ProfileMenuRow(
                icon = Icons.Outlined.Palette,
                title = "Appearance Settings",
                subtitle = "Toggle luxury Dark Mode & visual preferences",
                onClick = onOpenSettings,
                modifier = Modifier.testTag("profile_appearance_settings_row")
            )

            ProfileMenuRow(
                icon = Icons.Outlined.Lock,
                title = "Privacy Settings",
                subtitle = "Manage conversation history retention & security",
                onClick = onOpenSettings,
                modifier = Modifier.testTag("profile_privacy_settings_row")
            )

            RoyalLuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Your profile and conversation history are isolated per user UID under hardened Firestore security rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RoyalOutlinedButton(
                text = stringResource(R.string.action_logout),
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_logout_button"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoyalLuxuryCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
