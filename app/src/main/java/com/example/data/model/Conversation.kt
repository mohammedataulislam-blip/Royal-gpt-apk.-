package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

enum class ConversationCategory(val label: String) {
    CHAT("AI Chat"),
    IMAGE("AI Image"),
    VOICE("AI Voice"),
    WRITING("AI Writing"),
    CODING("AI Coding"),
    TRANSLATION("AI Translation"),
    SUMMARIZATION("AI Summarization"),
    LEARNING("AI Learning"),
    VISION("AI Image Understanding");

    companion object {
        fun fromRaw(raw: String?): ConversationCategory =
            entries.firstOrNull { it.name == raw } ?: CHAT
    }
}

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = ConversationCategory.CHAT.name,
    val lastMessagePreview: String = "",
    val messageCount: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val resolvedCategory: ConversationCategory
        get() = ConversationCategory.fromRaw(category)

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.trim().ifBlank { "New Royal Session" }.take(100),
        "category" to resolvedCategory.name,
        "lastMessagePreview" to lastMessagePreview.trim().take(240),
        "messageCount" to messageCount.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
