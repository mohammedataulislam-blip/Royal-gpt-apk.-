package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppSettingsPreferences
import com.example.data.repository.ConversationRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val currentUserId: String
) : ViewModel() {

    val preferences: StateFlow<AppSettingsPreferences> = settingsRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AppSettingsPreferences()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(enabled)
            val current = preferences.value
            userRepository.updatePreferences(
                userId = currentUserId,
                darkThemeEnabled = enabled,
                notificationsEnabled = current.notificationsEnabled,
                saveHistoryEnabled = current.saveHistoryEnabled
            )
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifications(enabled)
            val current = preferences.value
            userRepository.updatePreferences(
                userId = currentUserId,
                darkThemeEnabled = current.darkThemeEnabled,
                notificationsEnabled = enabled,
                saveHistoryEnabled = current.saveHistoryEnabled
            )
        }
    }

    fun setSaveHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSaveHistory(enabled)
            val current = preferences.value
            userRepository.updatePreferences(
                userId = currentUserId,
                darkThemeEnabled = current.darkThemeEnabled,
                notificationsEnabled = current.notificationsEnabled,
                saveHistoryEnabled = enabled
            )
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            conversationRepository.clearAllConversations(currentUserId)
                .onSuccess {
                    _statusMessage.value = "All cloud conversation threads cleared."
                }
                .onFailure { err ->
                    _statusMessage.value = err.localizedMessage ?: "Failed to clear history."
                }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
