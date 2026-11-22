package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

enum class MessageRole {
    USER,
    SYSTEM_NOTICE;

    companion object {
        fun fromRaw(raw: String?): MessageRole =
            entries.firstOrNull { it.name == raw } ?: USER
    }
}

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val role: String = MessageRole.USER.name,
    val content: String = "",
    val attachmentLabel: String? = null,
    val createdAt: Timestamp? = null
) {
    val resolvedRole: MessageRole
        get() = MessageRole.fromRaw(role)

    fun toCreateMap(): Map<String, Any> {
        val safeAttachment = attachmentLabel?.trim()?.takeIf { it.isNotEmpty() }?.take(200)
        return buildMap {
            put("userId", userId)
            put("conversationId", conversationId)
            put("role", resolvedRole.name)
            put("content", content.trim().take(4000))
            if (safeAttachment != null) {
                put("attachmentLabel", safeAttachment)
            }
            put("createdAt", FieldValue.serverTimestamp())
        }
    }
}
