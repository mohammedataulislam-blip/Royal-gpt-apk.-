package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,
    val authProvider: String = "google",
    val darkThemeEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val saveHistoryEnabled: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val safeName = displayName.trim().ifBlank { "Royal Member" }.take(60)
        val safeEmail = email.trim().take(120)
        val safePhoto = photoUrl?.trim()?.takeIf { it.isNotEmpty() }?.take(500)
        val safeProvider = if (authProvider == "email_pending") "email_pending" else "google"
        return buildMap {
            put("userId", userId)
            put("displayName", safeName)
            put("email", safeEmail)
            if (safePhoto != null) {
                put("photoUrl", safePhoto)
            }
            put("emailVerified", emailVerified)
            put("authProvider", safeProvider)
            put("darkThemeEnabled", darkThemeEnabled)
            put("notificationsEnabled", notificationsEnabled)
            put("saveHistoryEnabled", saveHistoryEnabled)
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
    }
}
