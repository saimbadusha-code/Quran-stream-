package com.example.data.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FirestoreUserPreferences
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UserLibraryRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObservePreferences_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreUserLibraryRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserPreferences(
                FirestoreUserPreferences(
                    userId = aliceUid,
                    preferredLanguage = "ar",
                    ambientSoundId = "ocean",
                    preferredAudioSourceCode = "everyayah",
                    favoriteReciterIds = listOf("7"),
                    favoriteTrackIds = listOf("7_1", "7_36"),
                    savedCollectionIds = listOf("col_focus")
                )
            )
        }
        assertTrue("Expected saveUserPreferences to succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserPreferences(aliceUid).first { it != null && it.preferredLanguage == "ar" }
        }
        assertNotNull(emitted)
        assertEquals("ocean", emitted?.ambientSoundId)
        assertEquals("everyayah", emitted?.preferredAudioSourceCode)
        assertEquals(listOf("7_1", "7_36"), emitted?.favoriteTrackIds)
    }

    @Test
    fun createUpdateObservePlaylist_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreUserLibraryRepository(firestore)

        val playlistId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createPlaylist(
                name = "Night Calm",
                description = "Surah Al-Mulk and Al-Baqarah closing",
                initialTrackIds = listOf("7_67")
            ).getOrThrow()
        }
        assertTrue(playlistId.isNotEmpty())

        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updatePlaylist(
                playlistId = playlistId,
                name = "Night Calm Updated",
                description = "Updated description",
                trackIds = listOf("7_67", "7_1")
            )
        }
        assertTrue(updateRes.isSuccess)

        val playlists = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePlaylists(aliceUid).first { list ->
                list.any { it.id == playlistId && it.name == "Night Calm Updated" }
            }
        }
        val match = playlists.first { it.id == playlistId }
        assertEquals(listOf("7_67", "7_1"), match.trackIds)
    }

    @Test
    fun recordAndObserveListeningEvent_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreUserLibraryRepository(firestore)

        val eventId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordListeningEvent(
                trackId = "7_1",
                surahNumber = 1,
                surahName = "Al-Fatihah",
                reciterId = "7",
                reciterName = "Mishary Rashid Alafasy",
                durationListenedSec = 42,
                lastPositionMs = 42000L,
                sessionId = "sess_abc"
            ).getOrThrow()
        }

        val events = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeListeningEvents(aliceUid).first { list -> list.any { it.id == eventId } }
        }
        assertTrue(events.any { it.id == eventId && it.durationListenedSec == 42L })
    }

    @Test
    fun getUserPreferences_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreUserLibraryRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveUserPreferences(FirestoreUserPreferences(userId = aliceUid)).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreUserLibraryRepository(firestore)
        val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUserPreferencesOnce(aliceUid)
        }
        assertTrue("Expected cross-user read to fail", crossResult.isFailure)
        val fsEx = crossResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observePlaylists_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = FirestoreUserLibraryRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePlaylists(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED when observing playlists while signed out")
        } catch (e: Exception) {
            val fsEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@tilawah.test"
        const val BOB_EMAIL = "bob@tilawah.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3500L
    }
}
