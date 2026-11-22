package com.example.ui.screens.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ConversationCategory
import com.example.ui.components.RoyalCrestLogo
import com.example.ui.components.RoyalLoadingIndicator
import com.example.ui.components.RoyalLuxuryCard
import com.example.ui.components.RoyalStatusBadge
import com.example.ui.screens.home.UiState
import com.example.ui.theme.RoyalBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: ChatViewModel
) {
    val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val attachedImageUri by viewModel.attachedImageUri.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()

    var showHistorySheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val transcript = matches?.firstOrNull().orEmpty()
            if (transcript.isNotBlank()) {
                viewModel.appendVoiceTranscript(transcript)
            }
        }
    }

    LaunchedEffect(messagesState) {
        val count = (messagesState as? UiState.Success)?.data?.size ?: 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chat_history_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = {
                            viewModel.startNewConversation(ConversationCategory.CHAT)
                            showHistorySheet = false
                        }
                    ) {
                        Icon(Icons.Outlined.AddComment, contentDescription = "New conversation")
                    }
                }

                when (val convState = conversationsState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Text(
                            text = convState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is UiState.Success -> {
                        if (convState.data.isEmpty()) {
                            Text(
                                text = "No saved threads yet. Send a message to create your first session.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(320.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(convState.data, key = { it.id }) { conversation ->
                                    RoyalLuxuryCard(
                                        onClick = {
                                            viewModel.selectConversation(conversation)
                                            showHistorySheet = false
                                        },
                                        contentPadding = PaddingValues(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = conversation.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${conversation.resolvedCategory.label} • ${conversation.messageCount} messages",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteConversation(conversation.id) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.DeleteOutline,
                                                    contentDescription = "Delete thread"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("ai_chat_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoyalCrestLogo(size = 36.dp, animated = false)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = activeCategory.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    RoyalStatusBadge(
                        text = "WORKSPACE PERSISTED",
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Chat history",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { viewModel.startNewConversation(ConversationCategory.CHAT) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_new_conversation_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddComment,
                        contentDescription = stringResource(R.string.chat_new_conversation),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (!statusBanner.isNullOrBlank()) {
            RoyalLuxuryCard(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statusBanner.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = viewModel::clearStatusBanner) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Dismiss notice",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Messages area
        Box(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 840.dp)
                .fillMaxWidth()
        ) {
            when (val msgState = messagesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        RoyalLoadingIndicator(label = "Loading conversation…")
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RoyalLuxuryCard {
                            Text(
                                text = msgState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    val messages = msgState.data
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RoyalCrestLogo(size = 76.dp, animated = true)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Royal GPT ${activeCategory.label}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Compose prompts, attach visual references, or dictate via voice. Messages are saved directly to your private Firestore workspace.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            RoyalLuxuryCard(
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Stage 1 Architecture: Messaging & Firestore persistence are active. Live AI inference connects in Stage 2.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                ChatMessageBubble(message = msg)
                            }
                            item {
                                RoyalLuxuryCard(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.chat_stage_notice),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Composer bar
        Surface(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (attachedImageUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, start = 4.dp, end = 4.dp)
                    ) {
                        AsyncImage(
                            model = attachedImageUri,
                            contentDescription = "Selected attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Text(
                            text = "Image attached",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.onImageSelected(null) }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove attachment",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_attach_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Attach image",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Royal GPT")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (_: Exception) {
                                viewModel.showInfoBanner(
                                    "Voice speech recognizer is unavailable on this device configuration."
                                )
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Voice input",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = viewModel::onInputChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.chat_input_placeholder),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input")
                    )

                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = (inputText.isNotBlank() || attachedImageUri != null) && !isSending,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(RoyalBlue, MaterialTheme.colorScheme.primary)
                                )
                            )
                            .testTag("chat_send_button")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp
            ),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            ),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (!message.attachmentLabel.isNullOrBlank()) {
                    RoyalStatusBadge(
                        text = "IMAGE ATTACHED",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
