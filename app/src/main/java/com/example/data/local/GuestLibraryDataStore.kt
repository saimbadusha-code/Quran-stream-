package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.AudioSourceProvider
import com.example.data.model.CustomPlaylist
import com.example.data.model.ListeningEventRecord
import com.example.data.model.RepeatMode
import com.example.data.model.UserPreferencesState
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.tilawahGuestDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tilawah_guest_store"
)

class GuestLibraryDataStore(
    appContext: Context,
    customDataStore: DataStore<Preferences>? = null
) {
    private val store: DataStore<Preferences> =
        customDataStore ?: appContext.applicationContext.tilawahGuestDataStore

    private val safeDataFlow: Flow<Preferences> = store.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    private object Keys {
        val LANGUAGE = stringPreferencesKey("preferred_language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val ARABIC_SCALE = floatPreferencesKey("arabic_font_scale")
        val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
        val DEFAULT_REPEAT = stringPreferencesKey("default_repeat")
        val AMBIENT_ID = stringPreferencesKey("ambient_sound_id")
        val AMBIENT_VOL = floatPreferencesKey("ambient_volume")
        val PREFERRED_AUDIO_SOURCE = stringPreferencesKey("preferred_audio_source")
        val FAV_RECITERS = stringSetPreferencesKey("fav_reciter_ids")
        val FAV_TRACKS = stringSetPreferencesKey("fav_track_ids")
        val SAVED_COLS = stringSetPreferencesKey("saved_collection_ids")
        val DEMO_PREMIUM = booleanPreferencesKey("is_demo_premium")
        val PLAYLISTS_JSON = stringPreferencesKey("guest_playlists_json")
        val EVENTS_JSON = stringPreferencesKey("guest_listening_events_json")
        val RECENT_SEARCHES_JSON = stringPreferencesKey("recent_searches_json")
        val GUEST_DISMISSED_AUTH_BANNER = booleanPreferencesKey("guest_dismissed_auth_banner")
    }

    val preferencesFlow: Flow<UserPreferencesState> = safeDataFlow.map { prefs ->
        UserPreferencesState(
            preferredLanguage = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: "en"),
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            arabicFontScale = (prefs[Keys.ARABIC_SCALE] ?: 1.0f).coerceIn(0.8f, 1.8f),
            autoplayNext = prefs[Keys.AUTOPLAY_NEXT] ?: true,
            defaultRepeatMode = RepeatMode.fromCode(prefs[Keys.DEFAULT_REPEAT] ?: "off"),
            ambientSoundId = prefs[Keys.AMBIENT_ID] ?: "none",
            ambientVolume = (prefs[Keys.AMBIENT_VOL] ?: 0.35f).coerceIn(0f, 1f),
            preferredAudioSourceCode = prefs[Keys.PREFERRED_AUDIO_SOURCE] ?: AudioSourceProvider.AL_QURAN_CLOUD.code,
            favoriteReciterIds = prefs[Keys.FAV_RECITERS] ?: emptySet(),
            favoriteTrackIds = prefs[Keys.FAV_TRACKS] ?: emptySet(),
            savedCollectionIds = prefs[Keys.SAVED_COLS] ?: emptySet(),
            isDemoPremium = prefs[Keys.DEMO_PREMIUM] ?: false
        )
    }.distinctUntilChanged()

    val playlistsFlow: Flow<List<CustomPlaylist>> = safeDataFlow.map { prefs ->
        parsePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty())
    }.distinctUntilChanged()

    val listeningEventsFlow: Flow<List<ListeningEventRecord>> = safeDataFlow.map { prefs ->
        parseEvents(prefs[Keys.EVENTS_JSON].orEmpty())
    }.distinctUntilChanged()

    val recentSearchesFlow: Flow<List<String>> = safeDataFlow.map { prefs ->
        parseStringList(prefs[Keys.RECENT_SEARCHES_JSON].orEmpty())
    }.distinctUntilChanged()

    val authBannerDismissedFlow: Flow<Boolean> = safeDataFlow.map { prefs ->
        prefs[Keys.GUEST_DISMISSED_AUTH_BANNER] ?: false
    }.distinctUntilChanged()

    suspend fun savePreferences(state: UserPreferencesState) {
        store.edit { prefs ->
            prefs[Keys.LANGUAGE] = state.preferredLanguage.code
            prefs[Keys.DARK_THEME] = state.darkTheme
            prefs[Keys.REDUCED_MOTION] = state.reducedMotion
            prefs[Keys.ARABIC_SCALE] = state.arabicFontScale.coerceIn(0.8f, 1.8f)
            prefs[Keys.AUTOPLAY_NEXT] = state.autoplayNext
            prefs[Keys.DEFAULT_REPEAT] = state.defaultRepeatMode.code
            prefs[Keys.AMBIENT_ID] = state.ambientSoundId
            prefs[Keys.AMBIENT_VOL] = state.ambientVolume.coerceIn(0f, 1f)
            prefs[Keys.PREFERRED_AUDIO_SOURCE] = state.preferredAudioSourceCode
            prefs[Keys.FAV_RECITERS] = state.favoriteReciterIds
            prefs[Keys.FAV_TRACKS] = state.favoriteTrackIds
            prefs[Keys.SAVED_COLS] = state.savedCollectionIds
            prefs[Keys.DEMO_PREMIUM] = state.isDemoPremium
        }
    }

    suspend fun setAuthBannerDismissed(dismissed: Boolean) {
        store.edit { prefs ->
            prefs[Keys.GUEST_DISMISSED_AUTH_BANNER] = dismissed
        }
    }

    suspend fun addRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        store.edit { prefs ->
            val current = parseStringList(prefs[Keys.RECENT_SEARCHES_JSON].orEmpty()).toMutableList()
            current.removeAll { it.equals(clean, ignoreCase = true) }
            current.add(0, clean)
            prefs[Keys.RECENT_SEARCHES_JSON] = serializeStringList(current.take(8))
        }
    }

    suspend fun clearRecentSearches() {
        store.edit { prefs ->
            prefs[Keys.RECENT_SEARCHES_JSON] = "[]"
        }
    }

    suspend fun createPlaylist(
        name: String,
        description: String,
        initialTrackIds: List<String> = emptyList(),
        fixedPlaylistId: String? = null
    ): CustomPlaylist {
        val id = fixedPlaylistId ?: "local_pl_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val now = System.currentTimeMillis()
        val created = CustomPlaylist(
            id = id,
            ownerId = "local_guest",
            name = name.trim().take(80).ifEmpty { "My Playlist" },
            description = description.trim().take(300),
            trackIds = initialTrackIds.take(200),
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        store.edit { prefs ->
            val current = parsePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty())
                .filterNot { it.id == id }
                .toMutableList()
            current.add(0, created)
            prefs[Keys.PLAYLISTS_JSON] = serializePlaylists(current)
        }
        return created
    }

    suspend fun updatePlaylist(
        playlistId: String,
        name: String,
        description: String,
        trackIds: List<String>
    ) {
        val now = System.currentTimeMillis()
        store.edit { prefs ->
            val updated = parsePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty()).map { pl ->
                if (pl.id == playlistId) {
                    pl.copy(
                        name = name.trim().take(80).ifEmpty { "My Playlist" },
                        description = description.trim().take(300),
                        trackIds = trackIds.take(200),
                        updatedAtEpochMs = now
                    )
                } else {
                    pl
                }
            }
            prefs[Keys.PLAYLISTS_JSON] = serializePlaylists(updated)
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        store.edit { prefs ->
            val filtered = parsePlaylists(prefs[Keys.PLAYLISTS_JSON].orEmpty())
                .filterNot { it.id == playlistId }
            prefs[Keys.PLAYLISTS_JSON] = serializePlaylists(filtered)
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
    ) {
        val now = System.currentTimeMillis()
        val id = "local_ev_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        store.edit { prefs ->
            val current = parseEvents(prefs[Keys.EVENTS_JSON].orEmpty()).toMutableList()
            current.add(
                0,
                ListeningEventRecord(
                    id = id,
                    trackId = trackId,
                    surahNumber = surahNumber,
                    surahName = surahName,
                    reciterId = reciterId,
                    reciterName = reciterName,
                    durationListenedSec = durationListenedSec.coerceAtLeast(1),
                    lastPositionMs = lastPositionMs.coerceAtLeast(0L),
                    sessionId = sessionId,
                    timestampEpochMs = now
                )
            )
            prefs[Keys.EVENTS_JSON] = serializeEvents(current.take(250))
        }
    }

    private fun parsePlaylists(raw: String): List<CustomPlaylist> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val tracksArr = obj.optJSONArray("trackIds") ?: JSONArray()
                    val tracks = buildList {
                        for (j in 0 until tracksArr.length()) {
                            val t = tracksArr.optString(j)
                            if (t.isNotBlank()) add(t)
                        }
                    }
                    add(
                        CustomPlaylist(
                            id = obj.optString("id"),
                            ownerId = obj.optString("ownerId", "local_guest"),
                            name = obj.optString("name", "Playlist"),
                            description = obj.optString("description", ""),
                            trackIds = tracks,
                            createdAtEpochMs = obj.optLong("createdAtEpochMs", 0L),
                            updatedAtEpochMs = obj.optLong("updatedAtEpochMs", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializePlaylists(list: List<CustomPlaylist>): String {
        val arr = JSONArray()
        list.forEach { pl ->
            arr.put(
                JSONObject().apply {
                    put("id", pl.id)
                    put("ownerId", pl.ownerId)
                    put("name", pl.name)
                    put("description", pl.description)
                    put("trackIds", JSONArray(pl.trackIds))
                    put("createdAtEpochMs", pl.createdAtEpochMs)
                    put("updatedAtEpochMs", pl.updatedAtEpochMs)
                }
            )
        }
        return arr.toString()
    }

    private fun parseEvents(raw: String): List<ListeningEventRecord> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        ListeningEventRecord(
                            id = obj.optString("id"),
                            trackId = obj.optString("trackId"),
                            surahNumber = obj.optInt("surahNumber", 1),
                            surahName = obj.optString("surahName", "Al-Fatihah"),
                            reciterId = obj.optString("reciterId", "7"),
                            reciterName = obj.optString("reciterName", "Mishary Rashid Alafasy"),
                            durationListenedSec = obj.optInt("durationListenedSec", 1),
                            lastPositionMs = obj.optLong("lastPositionMs", 0L),
                            sessionId = obj.optString("sessionId", "local_session"),
                            timestampEpochMs = obj.optLong("timestampEpochMs", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeEvents(list: List<ListeningEventRecord>): String {
        val arr = JSONArray()
        list.forEach { ev ->
            arr.put(
                JSONObject().apply {
                    put("id", ev.id)
                    put("trackId", ev.trackId)
                    put("surahNumber", ev.surahNumber)
                    put("surahName", ev.surahName)
                    put("reciterId", ev.reciterId)
                    put("reciterName", ev.reciterName)
                    put("durationListenedSec", ev.durationListenedSec)
                    put("lastPositionMs", ev.lastPositionMs)
                    put("sessionId", ev.sessionId)
                    put("timestampEpochMs", ev.timestampEpochMs)
                }
            )
        }
        return arr.toString()
    }

    private fun parseStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i)
                    if (s.isNotBlank()) add(s)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeStringList(list: List<String>): String = JSONArray(list).toString()
}
