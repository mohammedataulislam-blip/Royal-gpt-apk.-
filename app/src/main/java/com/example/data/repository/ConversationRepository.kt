package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.ConversationCategory
import com.example.data.model.MessageRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ConversationRepository(
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
            ?: throw IllegalStateException("User must be signed in before accessing conversations.")
    }

    fun observeConversations(userId: String): Flow<List<Conversation>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        val colRef = db.collection("users").document(uid).collection("conversations")
        emitAll(
            colRef
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Conversation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeMessages(userId: String, conversationId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        require(conversationId.isNotBlank()) { "Conversation ID cannot be blank." }
        val colRef = db.collection("users").document(uid)
            .collection("conversations").document(conversationId)
            .collection("messages")
        emitAll(
            colRef
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChatMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun createConversation(
        userId: String,
        title: String,
        category: ConversationCategory
    ): Result<String> {
        var path = "users/$userId/conversations"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user conversation creation." }
            val convRef = db.collection("users").document(uid)
                .collection("conversations").document()
            path = convRef.path
            val safeTitle = title.trim().ifBlank { "${category.label} Session" }.take(100)
            val model = Conversation(
                userId = uid,
                title = safeTitle,
                category = category.name,
                lastMessagePreview = "Session initialized",
                messageCount = 0L
            )
            convRef.set(model.toCreateMap()).await()
            Result.success(convRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        userId: String,
        conversationId: String?,
        titleHint: String,
        category: ConversationCategory,
        content: String,
        attachmentLabel: String?
    ): Result<String> {
        var path = "users/$userId/conversations"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user message write." }
            val cleanContent = content.trim()
            require(cleanContent.isNotEmpty() && cleanContent.length <= 4000) {
                "Message must be between 1 and 4000 characters."
            }

            val conversationsCol = db.collection("users").document(uid).collection("conversations")
            val convRef = if (conversationId.isNullOrBlank()) {
                val newRef = conversationsCol.document()
                val derivedTitle = titleHint.trim()
                    .ifBlank { cleanContent.take(44) }
                    .take(100)
                val newConv = Conversation(
                    userId = uid,
                    title = derivedTitle,
                    category = category.name,
                    lastMessagePreview = cleanContent.take(240),
                    messageCount = 0L
                )
                path = newRef.path
                newRef.set(newConv.toCreateMap()).await()
                newRef
            } else {
                conversationsCol.document(conversationId)
            }

            val msgRef = convRef.collection("messages").document()
            path = msgRef.path
            val msgModel = ChatMessage(
                userId = uid,
                conversationId = convRef.id,
                role = MessageRole.USER.name,
                content = cleanContent,
                attachmentLabel = attachmentLabel
            )
            msgRef.set(msgModel.toCreateMap()).await()

            path = convRef.path
            convRef.update(
                mapOf(
                    "lastMessagePreview" to cleanContent.take(240),
                    "messageCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(convRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(userId: String, conversationId: String): Result<Unit> {
        var path = "users/$userId/conversations/$conversationId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user delete." }
            require(conversationId.isNotBlank()) { "Conversation ID cannot be blank." }
            val convRef = db.collection("users").document(uid)
                .collection("conversations").document(conversationId)
            path = convRef.path
            val messagesSnapshot = convRef.collection("messages").get().await()
            val batch = db.batch()
            messagesSnapshot.documents.forEach { doc -> batch.delete(doc.reference) }
            batch.delete(convRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun clearAllConversations(userId: String): Result<Unit> {
        val path = "users/$userId/conversations"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user delete." }
            val convDocs = db.collection("users").document(uid)
                .collection("conversations").get().await()
            for (convDoc in convDocs.documents) {
                val messages = convDoc.reference.collection("messages").get().await()
                val batch = db.batch()
                messages.documents.forEach { msg -> batch.delete(msg.reference) }
                batch.delete(convDoc.reference)
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
