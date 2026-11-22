package com.example.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.screens.home.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> = userRepository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun updateDisplayName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed.length > 60) {
            _statusMessage.value = "Display name must be between 1 and 60 characters."
            return
        }
        viewModelScope.launch {
            userRepository.updateDisplayName(currentUserId, trimmed)
                .onSuccess { _statusMessage.value = "Profile name updated." }
                .onFailure { err ->
                    _statusMessage.value = err.localizedMessage ?: "Failed to update name."
                }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    private companion object {
        const val TAG = "ProfileViewModel"
    }
}
