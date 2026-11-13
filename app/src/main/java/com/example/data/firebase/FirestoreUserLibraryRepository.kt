package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.FirestoreCustomPlaylist
import com.example.data.model.FirestoreListeningEvent
import com.example.data.model.FirestoreUserPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreUserLibraryRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before writing to Firestore.")
    }

    fun observeUserPreferences(userId: String): Flow<FirestoreUserPreferences?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        FirestoreUserPreferences::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun saveUserPreferences(prefs: FirestoreUserPreferences): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                docRef.set(prefs.toCreateMap(uid)).await()
            } else {
                docRef.update(prefs.toUpdateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserPreferencesOnce(userId: String): Result<FirestoreUserPreferences?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            val obj = if (snap.exists()) {
                snap.toObject(
                    FirestoreUserPreferences::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
            Result.success(obj)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observePlaylists(userId: String): Flow<List<FirestoreCustomPlaylist>> {
        val path = "users/$userId/playlists"
        return db.collection("users").document(userId).collection("playlists")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FirestoreCustomPlaylist::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createPlaylist(
        name: String,
        description: String,
        initialTrackIds: List<String> = emptyList()
    ): Result<String> {
        val uid = requireUserId()
        val playlistId = "pl_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
        val payload = mapOf(
            "userId" to uid,
            "name" to name.trim().take(80).ifEmpty { "My Playlist" },
            "description" to description.trim().take(300),
            "trackIds" to initialTrackIds.take(200),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(playlistId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updatePlaylist(
        playlistId: String,
        name: String,
        description: String,
        trackIds: List<String>
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
        val updates = mapOf(
            "name" to name.trim().take(80).ifEmpty { "My Playlist" },
            "description" to description.trim().take(300),
            "trackIds" to trackIds.take(200),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeListeningEvents(userId: String): Flow<List<FirestoreListeningEvent>> {
        val path = "users/$userId/listening_events"
        return db.collection("users").document(userId).collection("listening_events")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FirestoreListeningEvent::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun recordListeningEvent(
        trackId: String,
        surahNumber: Int,
        surahName: String,
        reciterId: String,
        reciterName: String,
        durationListenedSec: Int,
        lastPositionMs: Long,
        sessionId: String
    ): Result<String> {
        val uid = requireUserId()
        val eventId = "ev_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("listening_events").document(eventId)
        val payload = mapOf(
            "userId" to uid,
            "trackId" to trackId.take(128),
            "surahNumber" to surahNumber.coerceIn(1, 114),
            "surahName" to surahName.take(100).ifEmpty { "Surah $surahNumber" },
            "reciterId" to reciterId.take(64),
            "reciterName" to reciterName.take(100).ifEmpty { "Reciter" },
            "durationListenedSec" to durationListenedSec.coerceIn(1, 86400),
            "lastPositionMs" to lastPositionMs.coerceIn(0L, 86400000L),
            "sessionId" to sessionId.take(128).ifEmpty { "session_default" },
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(eventId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
