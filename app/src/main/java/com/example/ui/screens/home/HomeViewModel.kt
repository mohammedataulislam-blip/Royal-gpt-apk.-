package com.example.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Conversation
import com.example.data.model.UserProfile
import com.example.data.repository.ConversationRepository
import com.example.data.repository.UserRepository
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

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class HomeViewModel(
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val currentUserId: String
) : ViewModel() {

    val userProfileState: StateFlow<UiState<UserProfile?>> = userRepository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing profile", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load user profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val conversationsState: StateFlow<UiState<List<Conversation>>> =
        conversationRepository.observeConversations(currentUserId)
            .map<List<Conversation>, UiState<List<Conversation>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing conversations", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load conversations"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            val result = conversationRepository.deleteConversation(currentUserId, conversationId)
            result.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not delete conversation."
            }
        }
    }

    fun clearError() {
        _actionError.value = null
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
