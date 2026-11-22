package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import com.example.data.model.AiToolItem
import com.example.data.model.RoyalAiToolsCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ToolsUiState(
    val tools: List<AiToolItem> = RoyalAiToolsCatalog.tools,
    val selectedTool: AiToolItem? = null,
    val draftPrompt: String = ""
)

class ToolsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    fun selectTool(tool: AiToolItem) {
        _uiState.update {
            it.copy(
                selectedTool = tool,
                draftPrompt = tool.promptTemplate
            )
        }
    }

    fun dismissToolSheet() {
        _uiState.update { it.copy(selectedTool = null) }
    }

    fun onDraftPromptChanged(value: String) {
        _uiState.update { it.copy(draftPrompt = value) }
    }
}
