package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserPreferences(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String? = null,
    val preferredLanguage: String = "en",
    val themeMode: String = "dark",
    val reducedMotion: Boolean = false,
    val arabicFontScale: Double = 1.0,
    val autoplayNext: Boolean = true,
    val defaultRepeatMode: String = "off",
    val ambientSoundId: String = "none",
    val ambientVolume: Double = 0.35,
    val preferredAudioSourceCode: String? = null,
    val favoriteReciterIds: List<String> = emptyList(),
    val favoriteTrackIds: List<String> = emptyList(),
    val savedCollectionIds: List<String> = emptyList(),
    val isDemoPremium: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(ownerUid: String): Map<String, Any> {
        return mapOf<String, Any?>(
            "userId" to ownerUid,
            "displayName" to displayName?.take(100),
            "preferredLanguage" to preferredLanguage,
            "themeMode" to themeMode,
            "reducedMotion" to reducedMotion,
            "arabicFontScale" to arabicFontScale.coerceIn(0.8, 1.8),
            "autoplayNext" to autoplayNext,
            "defaultRepeatMode" to defaultRepeatMode,
            "ambientSoundId" to ambientSoundId,
            "ambientVolume" to ambientVolume.coerceIn(0.0, 1.0),
            "preferredAudioSourceCode" to preferredAudioSourceCode?.take(32),
            "favoriteReciterIds" to favoriteReciterIds.take(100),
            "favoriteTrackIds" to favoriteTrackIds.take(200),
            "savedCollectionIds" to savedCollectionIds.take(100),
            "isDemoPremium" to isDemoPremium,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }

    fun toUpdateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "displayName" to displayName?.take(100),
            "preferredLanguage" to preferredLanguage,
            "themeMode" to themeMode,
            "reducedMotion" to reducedMotion,
            "arabicFontScale" to arabicFontScale.coerceIn(0.8, 1.8),
            "autoplayNext" to autoplayNext,
            "defaultRepeatMode" to defaultRepeatMode,
            "ambientSoundId" to ambientSoundId,
            "ambientVolume" to ambientVolume.coerceIn(0.0, 1.0),
            "preferredAudioSourceCode" to preferredAudioSourceCode?.take(32),
            "favoriteReciterIds" to favoriteReciterIds.take(100),
            "favoriteTrackIds" to favoriteTrackIds.take(200),
            "savedCollectionIds" to savedCollectionIds.take(100),
            "isDemoPremium" to isDemoPremium,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class FirestoreCustomPlaylist(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val trackIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreListeningEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val trackId: String = "",
    val surahNumber: Long = 1L,
    val surahName: String = "",
    val reciterId: String = "",
    val reciterName: String = "",
    val durationListenedSec: Long = 1L,
    val lastPositionMs: Long = 0L,
    val sessionId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
