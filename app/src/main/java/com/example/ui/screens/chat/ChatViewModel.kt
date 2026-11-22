package com.example.ui.screens.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.ConversationCategory
import com.example.data.repository.ConversationRepository
import com.example.ui.screens.home.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ConversationRepository,
    private val currentUserId: String,
    initialConversationId: String? = null,
    initialCategory: ConversationCategory = ConversationCategory.CHAT,
    initialPrompt: String = ""
) : ViewModel() {

    private val _activeConversationId = MutableStateFlow(initialConversationId)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeCategory = MutableStateFlow(initialCategory)
    val activeCategory: StateFlow<ConversationCategory> = _activeCategory.asStateFlow()

    private val _inputText = MutableStateFlow(initialPrompt)
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val conversationsState: StateFlow<UiState<List<Conversation>>> =
        repository.observeConversations(currentUserId)
            .map<List<Conversation>, UiState<List<Conversation>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing chat history", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load conversation history"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val messagesState: StateFlow<UiState<List<ChatMessage>>> =
        _activeConversationId.flatMapLatest { convId ->
            if (convId.isNullOrBlank()) {
                flowOf<UiState<List<ChatMessage>>>(UiState.Success(emptyList()))
            } else {
                repository.observeMessages(currentUserId, convId)
                    .map<List<ChatMessage>, UiState<List<ChatMessage>>> { UiState.Success(it) }
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        val rootError = firestoreError ?: error
                        Log.w(TAG, "Error observing messages", rootError)
                        emit(UiState.Error(rootError.message ?: "Failed to load messages"))
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun onInputChanged(value: String) {
        _inputText.value = value
    }

    fun appendVoiceTranscript(transcript: String) {
        val clean = transcript.trim()
        if (clean.isNotEmpty()) {
            val current = _inputText.value.trim()
            _inputText.value = if (current.isEmpty()) clean else "$current $clean"
        }
    }

    fun onImageSelected(uri: Uri?) {
        _attachedImageUri.value = uri
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showInfoBanner(message: String) {
        _statusBanner.value = message
    }

    fun startNewConversation(category: ConversationCategory = ConversationCategory.CHAT) {
        _activeConversationId.value = null
        _activeCategory.value = category
        _attachedImageUri.value = null
        _statusBanner.value = null
    }

    fun selectConversation(conversation: Conversation) {
        _activeConversationId.value = conversation.id
        _activeCategory.value = conversation.resolvedCategory
        _statusBanner.value = null
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            val result = repository.deleteConversation(currentUserId, conversationId)
            result.onSuccess {
                if (_activeConversationId.value == conversationId) {
                    _activeConversationId.value = null
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to delete conversation."
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val attachment = _attachedImageUri.value
        if (text.isEmpty() && attachment == null) return
        if (_isSending.value) return

        val finalContent = if (text.isNotEmpty()) text else "Attached image for visual review"
        val attachmentLabel = attachment?.lastPathSegment?.take(180) ?: attachment?.toString()?.take(180)

        viewModelScope.launch {
            _isSending.value = true
            _statusBanner.value = null
            val result = repository.sendMessage(
                userId = currentUserId,
                conversationId = _activeConversationId.value,
                titleHint = finalContent.take(44),
                category = _activeCategory.value,
                content = finalContent,
                attachmentLabel = attachmentLabel
            )
            _isSending.value = false
            result.onSuccess { newConvId ->
                _activeConversationId.value = newConvId
                _inputText.value = ""
                _attachedImageUri.value = null
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Unable to save message to Firestore."
            }
        }
    }

    private companion object {
        const val TAG = "ChatViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
