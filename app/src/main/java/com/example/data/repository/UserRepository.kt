package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(user: FirebaseUser): Result<Unit> {
        val path = "users/${user.uid}"
        return try {
            val uid = requireUserId()
            check(uid == user.uid) { "Signed-in user mismatch." }
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val initialProfile = UserProfile(
                    userId = uid,
                    displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "Royal Member",
                    email = user.email.orEmpty(),
                    photoUrl = user.photoUrl?.toString(),
                    emailVerified = user.isEmailVerified,
                    authProvider = "google",
                    darkThemeEnabled = true,
                    notificationsEnabled = true,
                    saveHistoryEnabled = true
                )
                docRef.set(initialProfile.toCreateMap()).await()
            } else {
                val updates = buildMap<String, Any> {
                    put("emailVerified", user.isEmailVerified)
                    user.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.take(500)?.let {
                        put("photoUrl", it)
                    }
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(userId: String, displayName: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user update." }
            val trimmed = displayName.trim()
            require(trimmed.length in 1..60) { "Display name must be between 1 and 60 characters." }
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "displayName" to trimmed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(
        userId: String,
        darkThemeEnabled: Boolean,
        notificationsEnabled: Boolean,
        saveHistoryEnabled: Boolean
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user update." }
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "darkThemeEnabled" to darkThemeEnabled,
                        "notificationsEnabled" to notificationsEnabled,
                        "saveHistoryEnabled" to saveHistoryEnabled,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
