package com.example.ui.screens.tools

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AiToolItem
import com.example.data.model.ConversationCategory
import com.example.ui.components.RoyalLuxuryCard
import com.example.ui.components.RoyalPrimaryButton
import com.example.ui.components.RoyalStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiToolsScreen(
    isWideScreen: Boolean,
    onLaunchToolInChat: (category: ConversationCategory, initialPrompt: String) -> Unit
) {
    val viewModel: ToolsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val selectedTool = state.selectedTool
    if (selectedTool != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissToolSheet,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RoyalStatusBadge(
                    text = selectedTool.badge,
                    accentColor = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = selectedTool.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = selectedTool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = state.draftPrompt,
                    onValueChange = viewModel::onDraftPromptChanged,
                    label = { Text("Prompt Starter") },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_prompt_input")
                )
                Text(
                    text = "Stage 1 saves your tool session to Firestore. Automated AI generation activates in Stage 2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RoyalPrimaryButton(
                    text = "Open in AI Chat",
                    onClick = {
                        val prompt = state.draftPrompt
                        val category = selectedTool.category
                        viewModel.dismissToolSheet()
                        onLaunchToolInChat(category, prompt)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_launch_chat_button")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_tools_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isWideScreen) 3 else 2),
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "AI Tools Suite",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a specialized Royal GPT module to prepare your structured prompt workspace.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.tools, key = { it.id }) { tool ->
                AiToolCard(
                    tool = tool,
                    icon = iconForTool(tool),
                    onClick = { viewModel.selectTool(tool) },
                    modifier = Modifier.testTag("ai_tool_card_${tool.id}")
                )
            }
        }
    }
}

@Composable
private fun AiToolCard(
    tool: AiToolItem,
    icon: ImageVector,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tool.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = tool.badge,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = tool.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tool.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun iconForTool(tool: AiToolItem): ImageVector = when (tool.id) {
    "writing" -> Icons.Outlined.EditNote
    "coding" -> Icons.Outlined.Code
    "translation" -> Icons.Outlined.Translate
    "summarization" -> Icons.Outlined.AutoStories
    "learning" -> Icons.Outlined.School
    "vision" -> Icons.Outlined.ImageSearch
    else -> Icons.Outlined.EditNote
}
