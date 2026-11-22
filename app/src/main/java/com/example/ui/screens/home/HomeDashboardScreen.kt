package com.example.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Conversation
import com.example.data.model.ConversationCategory
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.components.RoyalLoadingIndicator
import com.example.ui.components.RoyalLuxuryCard
import com.example.ui.components.RoyalPrimaryButton
import com.example.ui.components.RoyalStatusBadge
import java.util.Calendar

@Composable
fun HomeDashboardScreen(
    viewModel: HomeViewModel,
    fallbackUserName: String,
    isWideScreen: Boolean,
    onOpenChat: (conversationId: String?, category: ConversationCategory) -> Unit,
    onOpenTools: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    val displayName = remember(profileState, fallbackUserName) {
        val loadedName = (profileState as? UiState.Success)?.data?.displayName
        loadedName?.takeIf { it.isNotBlank() } ?: fallbackUserName.ifBlank { "Royal Member" }
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(modifier = Modifier.widthIn(max = 840.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoyalCrestLogo(size = 42.dp, animated = false)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.brand_wordmark),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            RoyalStatusBadge(
                                text = "CLOUD SYNC ACTIVE",
                                accentColor = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onOpenProfile,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("home_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                RoyalLuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderBrush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
                        )
                    )
                ) {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your private Royal GPT command center is synchronized and ready for your next prompt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    RoyalPrimaryButton(
                        text = "Start AI Conversation",
                        onClick = { onOpenChat(null, ConversationCategory.CHAT) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_primary_chat_cta")
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.widthIn(max = 840.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickShortcutCard(
                            title = "AI Chat",
                            subtitle = "Threaded dialogue",
                            icon = Icons.Outlined.ChatBubbleOutline,
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = { onOpenChat(null, ConversationCategory.CHAT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_ai_chat")
                        )
                        QuickShortcutCard(
                            title = "AI Image",
                            subtitle = "Visual studio",
                            icon = Icons.Outlined.Image,
                            accent = MaterialTheme.colorScheme.secondary,
                            onClick = { onOpenChat(null, ConversationCategory.IMAGE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_ai_image")
                        )
                        QuickShortcutCard(
                            title = "AI Voice",
                            subtitle = "Dictation mode",
                            icon = Icons.Outlined.GraphicEq,
                            accent = MaterialTheme.colorScheme.tertiary,
                            onClick = { onOpenChat(null, ConversationCategory.VOICE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_ai_voice")
                        )
                        QuickShortcutCard(
                            title = "AI Tools",
                            subtitle = "6 specialist suites",
                            icon = Icons.Outlined.AutoAwesome,
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = onOpenTools,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_ai_tools")
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickShortcutCard(
                                title = "AI Chat",
                                subtitle = "Threaded dialogue",
                                icon = Icons.Outlined.ChatBubbleOutline,
                                accent = MaterialTheme.colorScheme.primary,
                                onClick = { onOpenChat(null, ConversationCategory.CHAT) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shortcut_ai_chat")
                            )
                            QuickShortcutCard(
                                title = "AI Image",
                                subtitle = "Visual studio",
                                icon = Icons.Outlined.Image,
                                accent = MaterialTheme.colorScheme.secondary,
                                onClick = { onOpenChat(null, ConversationCategory.IMAGE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shortcut_ai_image")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickShortcutCard(
                                title = "AI Voice",
                                subtitle = "Dictation mode",
                                icon = Icons.Outlined.GraphicEq,
                                accent = MaterialTheme.colorScheme.tertiary,
                                onClick = { onOpenChat(null, ConversationCategory.VOICE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shortcut_ai_voice")
                            )
                            QuickShortcutCard(
                                title = "AI Tools",
                                subtitle = "6 specialist suites",
                                icon = Icons.Outlined.AutoAwesome,
                                accent = MaterialTheme.colorScheme.primary,
                                onClick = onOpenTools,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("shortcut_ai_tools")
                            )
                        }
                    }
                }
            }
        }

        if (!actionError.isNullOrBlank()) {
            item {
                RoyalLuxuryCard(
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = actionError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recent_conversations),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val count = (conversationsState as? UiState.Success)?.data?.size ?: 0
                Text(
                    text = "$count threads",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when (val state = conversationsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 840.dp)
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RoyalLoadingIndicator(label = "Synchronizing recent threads…")
                    }
                }
            }

            is UiState.Error -> {
                item {
                    RoyalLuxuryCard(
                        modifier = Modifier
                            .widthIn(max = 840.dp)
                            .fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    item {
                        RoyalLuxuryCard(
                            modifier = Modifier
                                .widthIn(max = 840.dp)
                                .fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No conversations saved yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Launch AI Chat or pick a specialist tool to persist your first thread in Firestore.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                RoyalPrimaryButton(
                                    text = "Start First Thread",
                                    onClick = { onOpenChat(null, ConversationCategory.CHAT) },
                                    modifier = Modifier.testTag("empty_start_chat_button")
                                )
                            }
                        }
                    }
                } else {
                    items(state.data, key = { it.id }) { conversation ->
                        ConversationRowCard(
                            conversation = conversation,
                            onClick = { onOpenChat(conversation.id, conversation.resolvedCategory) },
                            onDelete = { viewModel.deleteConversation(conversation.id) },
                            modifier = Modifier
                                .widthIn(max = 840.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoyalLuxuryCard(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accent.copy(alpha = 0.16f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConversationRowCard(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoyalLuxuryCard(
        onClick = onClick,
        modifier = modifier.testTag("conversation_card_${conversation.id}"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = conversation.resolvedCategory.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "${conversation.messageCount} msgs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (conversation.lastMessagePreview.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = conversation.lastMessagePreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_conversation_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete conversation",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
