package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreUserLibraryRepository
import com.example.data.local.GuestLibraryDataStore
import com.example.data.local.OfflineAudioCacheManager
import com.example.data.local.OfflineDownloadsUiState
import com.example.data.model.AppLanguage
import com.example.data.model.AudioSourceProvider
import com.example.data.model.AudioTrack
import com.example.data.model.CollectionCategory
import com.example.data.model.CuratedCollection
import com.example.data.model.CustomPlaylist
import com.example.data.model.DailyListeningPoint
import com.example.data.model.DownloadPermission
import com.example.data.model.FirestoreUserPreferences
import com.example.data.model.ListeningEventRecord
import com.example.data.model.ListeningInsightsSummary
import com.example.data.model.QuranVerse
import com.example.data.model.Reciter
import com.example.data.model.ReciterStatItem
import com.example.data.model.RepeatMode
import com.example.data.model.Surah
import com.example.data.model.TranslationEdition
import com.example.data.model.UserPreferencesState
import com.example.data.provider.AudioProvider
import com.example.data.provider.TilawahAudioProvider
import com.example.data.remote.AuthenticatedSeedCatalog
import com.example.data.remote.QuranCatalogRepository
import com.example.player.AmbientSoundEngine
import com.example.player.AmbientUiState
import com.example.player.AudioBackendDriver
import com.example.player.AndroidMediaPlayerDriver
import com.example.player.GlobalPlayerState
import com.example.player.QuranAudioEngine
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val isLoading: Boolean = false,
    val surahs: List<Surah> = AuthenticatedSeedCatalog.canonicalSurahs,
    val reciters: List<Reciter> = AuthenticatedSeedCatalog.reciters,
    val collections: List<CuratedCollection> = AuthenticatedSeedCatalog.curatedCollections,
    val translations: List<TranslationEdition> = AuthenticatedSeedCatalog.defaultTranslations,
    val catalogStatusBanner: String? = null
)

data class ScripturePanelState(
    val surahNumber: Int = 1,
    val verses: List<QuranVerse> = emptyList(),
    val isLoading: Boolean = false,
    val isOfflineFallback: Boolean = false,
    val selectedTranslation: TranslationEdition = AuthenticatedSeedCatalog.defaultTranslations.first(),
    val isCompactReadingMode: Boolean = false,
    val errorMessage: String? = null
)

data class SearchResultsState(
    val query: String = "",
    val selectedTab: String = "all",
    val matchingSurahs: List<Surah> = emptyList(),
    val matchingReciters: List<Reciter> = emptyList(),
    val matchingCollections: List<CuratedCollection> = emptyList(),
    val matchingSpokenSessions: List<AudioTrack> = emptyList(),
    val recentSearches: List<String> = emptyList()
)

class TilawahViewModel(
    private val databaseId: String,
    private val guestStore: GuestLibraryDataStore,
    private val catalogRepository: QuranCatalogRepository = QuranCatalogRepository(),
    private val offlineCacheManager: OfflineAudioCacheManager? = null,
    audioDriver: AudioBackendDriver = AndroidMediaPlayerDriver(),
    enableAmbientHardware: Boolean = true
) : ViewModel() {

    val audioProvider: AudioProvider = TilawahAudioProvider(catalogRepository)

    private var activeAuthenticatedUid: String? = null
    private var firestoreRepository: FirestoreUserLibraryRepository? = null
    private var cloudSyncJob: Job? = null
    private var hasInitializedDefaultRepeat: Boolean = false
    private var lastAppliedDefaultRepeatMode: RepeatMode = RepeatMode.OFF
    private var lastAppliedVerseTimings: Map<String, Pair<Long, Long>> = emptyMap()
    private var manuallyPausedBgMusic: Boolean = false
    private val inFlightLocalPrefWrites = java.util.concurrent.atomic.AtomicInteger(0)
    private val pendingOptimisticPlaylists = java.util.concurrent.ConcurrentHashMap<String, CustomPlaylist>()
    private val pendingDeletedPlaylistIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    private val _catalogState = MutableStateFlow(CatalogUiState())
    val catalogState: StateFlow<CatalogUiState> = _catalogState.asStateFlow()

    private val _preferences = MutableStateFlow(UserPreferencesState())
    val preferences: StateFlow<UserPreferencesState> = _preferences.asStateFlow()

    private val _playlists = MutableStateFlow<List<CustomPlaylist>>(emptyList())
    val playlists: StateFlow<List<CustomPlaylist>> = _playlists.asStateFlow()

    private val _listeningEvents = MutableStateFlow<List<ListeningEventRecord>>(emptyList())
    val listeningEvents: StateFlow<List<ListeningEventRecord>> = _listeningEvents.asStateFlow()

    private val _authBannerDismissed = MutableStateFlow(false)
    val authBannerDismissed: StateFlow<Boolean> = _authBannerDismissed.asStateFlow()

    private val _syncErrorMessage = MutableStateFlow<String?>(null)
    val syncErrorMessage: StateFlow<String?> = _syncErrorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _searchTab = MutableStateFlow("all")
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())

    private val _collectionFilter = MutableStateFlow(CollectionCategory.ALL)
    val collectionFilter: StateFlow<CollectionCategory> = _collectionFilter.asStateFlow()

    private val _collectionSearchQuery = MutableStateFlow("")
    val collectionSearchQuery: StateFlow<String> = _collectionSearchQuery.asStateFlow()

    private val _reciterSearchQuery = MutableStateFlow("")
    val reciterSearchQuery: StateFlow<String> = _reciterSearchQuery.asStateFlow()

    private val _reciterCountryFilter = MutableStateFlow("All")
    val reciterCountryFilter: StateFlow<String> = _reciterCountryFilter.asStateFlow()

    private val _reciterSortMode = MutableStateFlow("Featured")
    val reciterSortMode: StateFlow<String> = _reciterSortMode.asStateFlow()

    private val _spokenFilter = MutableStateFlow("all")
    val spokenFilter: StateFlow<String> = _spokenFilter.asStateFlow()

    private val _selectedHeroReciterId = MutableStateFlow("yasser_dosari")
    val selectedHeroReciterId: StateFlow<String> = _selectedHeroReciterId.asStateFlow()

    val spokenSessions: List<AudioTrack> = audioProvider.getBackgroundVoiceTracks()

    private val _fallbackOfflineState = MutableStateFlow(OfflineDownloadsUiState())
    val offlineState: StateFlow<OfflineDownloadsUiState> =
        offlineCacheManager?.state ?: _fallbackOfflineState.asStateFlow()

    private val _scriptureState = MutableStateFlow(ScripturePanelState())
    val scriptureState: StateFlow<ScripturePanelState> = _scriptureState.asStateFlow()

    val ambientEngine = AmbientSoundEngine(enableAudioHardware = enableAmbientHardware)
    val ambientState: StateFlow<AmbientUiState> = ambientEngine.state

    val audioEngine = QuranAudioEngine(
        scope = viewModelScope,
        catalogRepository = catalogRepository,
        driver = audioDriver,
        offlineCacheManager = offlineCacheManager,
        onVerifiedListeningProgress = { track, seconds, lastPosMs, sessionId ->
            recordVerifiedListeningEvent(track, seconds, lastPosMs, sessionId)
        }
    )
    val playerState: StateFlow<GlobalPlayerState> = audioEngine.state

    val insightsSummary: StateFlow<ListeningInsightsSummary> = _listeningEvents
        .combine(_catalogState) { events, _ -> computeInsights(events) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ListeningInsightsSummary()
        )

    val searchState: StateFlow<SearchResultsState> = combine(
        _searchQuery,
        _searchTab,
        _catalogState,
        _recentSearches
    ) { query, tab, catalog, recents ->
        val rawQuery = query.trim()
        if (rawQuery.isEmpty()) {
            SearchResultsState(
                query = query,
                selectedTab = tab,
                recentSearches = recents
            )
        } else {
            val surahMatches = catalog.surahs.filter { s ->
                QuranCatalogRepository.matchesSearchQuery(s.displayName, rawQuery) ||
                    QuranCatalogRepository.matchesSearchQuery(s.englishMeaning, rawQuery) ||
                    s.arabicName.contains(rawQuery) ||
                    s.number.toString() == rawQuery
            }
            val reciterMatches = catalog.reciters.filter { r ->
                QuranCatalogRepository.matchesSearchQuery(r.displayName, rawQuery) ||
                    (r.arabicName?.contains(rawQuery) == true) ||
                    (r.country?.let { QuranCatalogRepository.matchesSearchQuery(it, rawQuery) } == true)
            }
            val colMatches = catalog.collections.filter { c ->
                QuranCatalogRepository.matchesSearchQuery(c.title, rawQuery) ||
                    QuranCatalogRepository.matchesSearchQuery(c.description, rawQuery)
            }
            val spokenMatches = spokenSessions.filter { sp ->
                QuranCatalogRepository.matchesSearchQuery(sp.surahName, rawQuery) ||
                    QuranCatalogRepository.matchesSearchQuery(sp.surahMeaning, rawQuery) ||
                    QuranCatalogRepository.matchesSearchQuery(sp.reciterName, rawQuery) ||
                    sp.surahArabicName.contains(rawQuery)
            }
            SearchResultsState(
                query = query,
                selectedTab = tab,
                matchingSurahs = surahMatches,
                matchingReciters = reciterMatches,
                matchingCollections = colMatches,
                matchingSpokenSessions = spokenMatches,
                recentSearches = recents
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchResultsState()
    )

    init {
        audioEngine.seedStandbyTrack(catalogRepository.buildTrack("yasser_dosari", 1))
        observeLocalGuestFlows()
        refreshCatalog()
        observeActiveTrackForScripture()
    }

    private fun observeLocalGuestFlows() {
        viewModelScope.launch {
            guestStore.recentSearchesFlow
                .catch { e -> Log.w(TAG, "Guest recent searches flow error", e) }
                .collect { _recentSearches.value = it }
        }
        viewModelScope.launch {
            guestStore.authBannerDismissedFlow
                .catch { e -> Log.w(TAG, "Guest banner flow error", e) }
                .collect { _authBannerDismissed.value = it }
        }
        viewModelScope.launch {
            guestStore.preferencesFlow
                .catch { e -> Log.w(TAG, "Guest preferences flow error", e) }
                .collect { localPrefs ->
                    if (activeAuthenticatedUid == null && inFlightLocalPrefWrites.get() == 0) {
                        applyPreferencesState(localPrefs)
                    }
                }
        }
        viewModelScope.launch {
            guestStore.playlistsFlow
                .catch { e -> Log.w(TAG, "Guest playlists flow error", e) }
                .collect { localPlaylists ->
                    if (activeAuthenticatedUid == null) {
                        val merged = buildList {
                            pendingOptimisticPlaylists.values
                                .sortedByDescending { it.updatedAtEpochMs }
                                .forEach { opt ->
                                    if (opt.id !in pendingDeletedPlaylistIds && localPlaylists.none { it.id == opt.id }) {
                                        add(opt)
                                    }
                                }
                            localPlaylists.forEach { diskPl ->
                                if (diskPl.id !in pendingDeletedPlaylistIds) {
                                    add(pendingOptimisticPlaylists[diskPl.id] ?: diskPl)
                                }
                            }
                        }
                        _playlists.value = merged
                    }
                }
        }
        viewModelScope.launch {
            guestStore.listeningEventsFlow
                .catch { e -> Log.w(TAG, "Guest listening events flow error", e) }
                .collect { localEvents ->
                    if (activeAuthenticatedUid == null) {
                        _listeningEvents.value = localEvents
                    }
                }
        }
    }

    fun onAuthenticatedUserChanged(uid: String?) {
        if (activeAuthenticatedUid == uid) return
        cloudSyncJob?.cancel()
        cloudSyncJob = null
        activeAuthenticatedUid = uid

        if (uid == null) {
            firestoreRepository = null
            return
        }

        // Instantiate Firestore ONLY when authenticated with a valid Google UID
        val db = FirebaseFirestore.getInstance(databaseId)
        val repo = FirestoreUserLibraryRepository(db)
        firestoreRepository = repo

        cloudSyncJob = viewModelScope.launch {
            launch {
                repo.observeUserPreferences(uid)
                    .catch { e ->
                        Log.w(TAG, "Error observing cloud preferences", e)
                        _syncErrorMessage.value = e.localizedMessage ?: "Cloud preference sync paused"
                    }
                    .collect { cloudPrefs ->
                        if (cloudPrefs != null) {
                            val mapped = UserPreferencesState(
                                preferredLanguage = AppLanguage.fromCode(cloudPrefs.preferredLanguage),
                                darkTheme = cloudPrefs.themeMode != "light",
                                reducedMotion = cloudPrefs.reducedMotion,
                                arabicFontScale = cloudPrefs.arabicFontScale.toFloat().coerceIn(0.8f, 1.8f),
                                autoplayNext = cloudPrefs.autoplayNext,
                                defaultRepeatMode = RepeatMode.fromCode(cloudPrefs.defaultRepeatMode),
                                ambientSoundId = cloudPrefs.ambientSoundId,
                                ambientVolume = cloudPrefs.ambientVolume.toFloat().coerceIn(0f, 1f),
                                preferredAudioSourceCode = cloudPrefs.preferredAudioSourceCode ?: _preferences.value.preferredAudioSourceCode,
                                favoriteReciterIds = cloudPrefs.favoriteReciterIds.toSet(),
                                favoriteTrackIds = cloudPrefs.favoriteTrackIds.toSet(),
                                savedCollectionIds = cloudPrefs.savedCollectionIds.toSet(),
                                isDemoPremium = cloudPrefs.isDemoPremium
                            )
                            applyPreferencesState(mapped)
                        } else {
                            // Initialize cloud document from current local state on first login
                            persistPreferences(_preferences.value)
                        }
                    }
            }

            launch {
                repo.observePlaylists(uid)
                    .catch { e ->
                        Log.w(TAG, "Error observing cloud playlists", e)
                        _syncErrorMessage.value = e.localizedMessage ?: "Failed to load cloud playlists"
                    }
                    .collect { list ->
                        _playlists.value = list.map { dto ->
                            CustomPlaylist(
                                id = dto.id,
                                ownerId = dto.userId,
                                name = dto.name,
                                description = dto.description,
                                trackIds = dto.trackIds,
                                createdAtEpochMs = dto.createdAt?.toDate()?.time ?: 0L,
                                updatedAtEpochMs = dto.updatedAt?.toDate()?.time ?: 0L
                            )
                        }.sortedByDescending { it.updatedAtEpochMs }
                    }
            }

            launch {
                repo.observeListeningEvents(uid)
                    .catch { e ->
                        Log.w(TAG, "Error observing cloud listening events", e)
                    }
                    .collect { list ->
                        _listeningEvents.value = list.map { dto ->
                            ListeningEventRecord(
                                id = dto.id,
                                trackId = dto.trackId,
                                surahNumber = dto.surahNumber.toInt(),
                                surahName = dto.surahName,
                                reciterId = dto.reciterId,
                                reciterName = dto.reciterName,
                                durationListenedSec = dto.durationListenedSec.toInt(),
                                lastPositionMs = dto.lastPositionMs,
                                sessionId = dto.sessionId,
                                timestampEpochMs = dto.createdAt?.toDate()?.time ?: 0L
                            )
                        }.sortedByDescending { it.timestampEpochMs }
                    }
            }
        }
    }

    private fun applyPreferencesState(state: UserPreferencesState) {
        _preferences.value = state
        catalogRepository.setPreferredProvider(AudioSourceProvider.fromCode(state.preferredAudioSourceCode))
        audioEngine.setAutoplayNext(state.autoplayNext)
        if (!hasInitializedDefaultRepeat || state.defaultRepeatMode != lastAppliedDefaultRepeatMode) {
            hasInitializedDefaultRepeat = true
            lastAppliedDefaultRepeatMode = state.defaultRepeatMode
            audioEngine.setRepeatMode(state.defaultRepeatMode)
        }
        if (state.ambientSoundId != "none" && state.ambientSoundId.isNotBlank() && !ambientState.value.isPlaying) {
            ambientEngine.selectBackgroundTrack(state.ambientSoundId)
        }
        ambientEngine.setVolume(state.ambientVolume)
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _catalogState.update { it.copy(isLoading = true) }
            val result = catalogRepository.refreshCatalogFromApi(_preferences.value.preferredLanguage.code)
            result.fold(
                onSuccess = { (surahs, reciters) ->
                    _catalogState.update {
                        it.copy(
                            isLoading = false,
                            surahs = surahs,
                            reciters = reciters,
                            catalogStatusBanner = null
                        )
                    }
                },
                onFailure = {
                    _catalogState.update {
                        it.copy(
                            isLoading = false,
                            surahs = catalogRepository.getInitialSurahs(),
                            reciters = catalogRepository.getInitialReciters(),
                            catalogStatusBanner = "Using verified offline catalog metadata. Connect to the internet for full Quran.com live catalog."
                        )
                    }
                }
            )
        }
    }

    private fun observeActiveTrackForScripture() {
        viewModelScope.launch {
            playerState.collect { st ->
                val activeSurah = st.currentEntry?.track?.surahNumber ?: return@collect
                val currentScripture = _scriptureState.value
                if (activeSurah != currentScripture.surahNumber ||
                    (currentScripture.verses.isEmpty() && !currentScripture.isLoading && currentScripture.errorMessage == null)
                ) {
                    lastAppliedVerseTimings = st.verseTimings
                    loadScriptureForSurah(activeSurah, currentScripture.selectedTranslation, st.verseTimings)
                } else if (st.verseTimings != lastAppliedVerseTimings && currentScripture.verses.isNotEmpty()) {
                    lastAppliedVerseTimings = st.verseTimings
                    _scriptureState.update { panel ->
                        panel.copy(
                            verses = panel.verses.map { v ->
                                val timing = st.verseTimings[v.verseKey]
                                v.copy(
                                    timestampFromMs = timing?.first,
                                    timestampToMs = timing?.second
                                )
                            }
                        )
                    }
                }
                if (st.isPlaying && ambientState.value.isEnabled && !ambientState.value.isPlaying && !manuallyPausedBgMusic) {
                    ambientEngine.selectSoundscape(ambientState.value.selectedTrackId)
                }
            }
        }
    }

    fun loadScriptureForSurah(
        surahNumber: Int = _scriptureState.value.surahNumber,
        edition: TranslationEdition = _scriptureState.value.selectedTranslation,
        timings: Map<String, Pair<Long, Long>> = playerState.value.verseTimings
    ) {
        lastAppliedVerseTimings = timings
        viewModelScope.launch {
            _scriptureState.update {
                it.copy(
                    surahNumber = surahNumber,
                    selectedTranslation = edition,
                    isLoading = true,
                    errorMessage = null
                )
            }
            val result = catalogRepository.getChapterVerses(surahNumber, edition, timings)
            val latestTimings = if (playerState.value.currentEntry?.track?.surahNumber == surahNumber &&
                playerState.value.verseTimings.isNotEmpty()
            ) {
                playerState.value.verseTimings
            } else {
                timings
            }
            lastAppliedVerseTimings = latestTimings
            val mergedVerses = if (latestTimings.isNotEmpty()) {
                result.verses.map { v ->
                    val t = latestTimings[v.verseKey]
                    if (t != null) v.copy(timestampFromMs = t.first, timestampToMs = t.second) else v
                }
            } else {
                result.verses
            }
            _scriptureState.update {
                it.copy(
                    surahNumber = surahNumber,
                    verses = mergedVerses,
                    isLoading = false,
                    isOfflineFallback = result.isOfflineFallback,
                    errorMessage = result.errorMessage
                )
            }
        }
    }

    fun selectTranslationEdition(edition: TranslationEdition) {
        loadScriptureForSurah(_scriptureState.value.surahNumber, edition)
    }

    fun toggleCompactReadingMode() {
        _scriptureState.update { it.copy(isCompactReadingMode = !it.isCompactReadingMode) }
    }

    // --- Playback Actions ---

    fun selectHeroReciter(reciterId: String) {
        _selectedHeroReciterId.value = reciterId
    }

    fun playSurahWithReciter(reciterId: String, surahNumber: Int) {
        val track = catalogRepository.buildTrack(reciterId, surahNumber)
        audioEngine.playOrToggleTrack(track)
    }

    fun playReciterFeaturedSet(reciterId: String) {
        val quickSurahs = listOf(1, 36, 55, 67, 112, 113, 114)
        val queue = quickSurahs.map { catalogRepository.buildTrack(reciterId, it) }
        if (queue.isNotEmpty()) {
            audioEngine.playTrack(queue.first(), newQueue = queue, shuffle = false)
        }
    }

    fun toggleHeroReciterPlayback(reciterId: String) {
        val st = playerState.value
        val activeReciterId = st.currentEntry?.track?.reciterId
        if (activeReciterId == reciterId && (st.durationMs > 0L || st.isPlaying)) {
            audioEngine.togglePlayPause()
        } else {
            playReciterFeaturedSet(reciterId)
        }
    }

    fun playCollection(collection: CuratedCollection, shuffle: Boolean = false) {
        val tracks = collection.trackIds.mapNotNull { catalogRepository.resolveTrackById(it) }
        if (tracks.isEmpty()) return
        val first = if (shuffle) tracks.random() else tracks.first()
        audioEngine.playTrack(first, newQueue = tracks, shuffle = shuffle)
    }

    fun addCollectionToQueue(collection: CuratedCollection) {
        val tracks = collection.trackIds.mapNotNull { catalogRepository.resolveTrackById(it) }
        audioEngine.addTracksToQueue(tracks)
    }

    fun playCustomPlaylist(playlist: CustomPlaylist, shuffle: Boolean = false) {
        val tracks = playlist.trackIds.mapNotNull { catalogRepository.resolveTrackById(it) }
        if (tracks.isEmpty()) return
        val first = if (shuffle) tracks.random() else tracks.first()
        audioEngine.playTrack(first, newQueue = tracks, shuffle = shuffle)
    }

    fun resolveTracks(trackIds: List<String>): List<AudioTrack> =
        trackIds.mapNotNull { catalogRepository.resolveTrackById(it) }

    fun buildTrackForReciter(reciterId: String, surahNumber: Int): AudioTrack =
        catalogRepository.buildTrack(reciterId, surahNumber)

    fun playVerseAudio(surahNumber: Int, verseNumber: Int, reciterId: String? = null) {
        val targetReciterId = reciterId
            ?: playerState.value.currentEntry?.track?.reciterId
            ?: "mishary_alafasy"
        val verseTrack = audioProvider.getVerseAudio(surahNumber, verseNumber, targetReciterId)
            ?: audioProvider.getVerseAudio(surahNumber, verseNumber, "mishary_alafasy")
            ?: return
        audioEngine.playTrack(verseTrack)
    }

    fun playSpokenSession(track: AudioTrack, sessionQueue: List<AudioTrack>? = null) {
        audioEngine.playOrToggleTrack(track, newQueue = sessionQueue ?: spokenSessions, shuffle = false)
    }

    fun setSpokenFilter(filterId: String) {
        _spokenFilter.value = filterId
    }

    // --- Offline Downloads (Licence-Gated) ---

    fun isOfflineDownloadPermitted(track: AudioTrack): Boolean {
        val lic = audioProvider.getLicenceMetadata(track)
        return lic.downloadPermission == DownloadPermission.PERSONAL_OFFLINE_PERMITTED &&
            lic.permissionConfirmed &&
            !track.audioUrl.isNullOrBlank()
    }

    fun downloadTrackForPersonalOfflineUse(track: AudioTrack) {
        val mgr = offlineCacheManager ?: return
        viewModelScope.launch {
            mgr.downloadTrackForPersonalOfflineUse(track)
        }
    }

    fun removeOfflineTrack(trackId: String) {
        val mgr = offlineCacheManager ?: return
        viewModelScope.launch {
            mgr.removeOfflineTrack(trackId)
        }
    }

    fun clearOfflineDownloadError() {
        offlineCacheManager?.clearDownloadError()
    }

    // --- Favorites & Preferences ---

    fun toggleFavoriteTrack(trackId: String) {
        val current = _preferences.value
        val nextSet = if (trackId in current.favoriteTrackIds) {
            current.favoriteTrackIds - trackId
        } else {
            current.favoriteTrackIds + trackId
        }
        persistPreferences(current.copy(favoriteTrackIds = nextSet))
    }

    fun toggleFavoriteReciter(reciterId: String) {
        val current = _preferences.value
        val nextSet = if (reciterId in current.favoriteReciterIds) {
            current.favoriteReciterIds - reciterId
        } else {
            current.favoriteReciterIds + reciterId
        }
        persistPreferences(current.copy(favoriteReciterIds = nextSet))
    }

    fun toggleSavedCollection(collectionId: String) {
        val current = _preferences.value
        val nextSet = if (collectionId in current.savedCollectionIds) {
            current.savedCollectionIds - collectionId
        } else {
            current.savedCollectionIds + collectionId
        }
        persistPreferences(current.copy(savedCollectionIds = nextSet))
    }

    fun setLanguage(language: AppLanguage) {
        persistPreferences(_preferences.value.copy(preferredLanguage = language))
    }

    fun setDarkTheme(dark: Boolean) {
        persistPreferences(_preferences.value.copy(darkTheme = dark))
    }

    fun setReducedMotion(reduced: Boolean) {
        persistPreferences(_preferences.value.copy(reducedMotion = reduced))
    }

    fun setArabicFontScale(scale: Float) {
        persistPreferences(_preferences.value.copy(arabicFontScale = scale.coerceIn(0.8f, 1.8f)))
    }

    fun setAutoplayNext(enabled: Boolean) {
        audioEngine.setAutoplayNext(enabled)
        persistPreferences(_preferences.value.copy(autoplayNext = enabled))
    }

    fun setDefaultRepeatMode(mode: RepeatMode) {
        lastAppliedDefaultRepeatMode = mode
        audioEngine.setRepeatMode(mode)
        persistPreferences(_preferences.value.copy(defaultRepeatMode = mode))
    }

    fun setPreferredAudioSource(provider: AudioSourceProvider) {
        catalogRepository.setPreferredProvider(provider)
        persistPreferences(_preferences.value.copy(preferredAudioSourceCode = provider.code))
    }

    fun toggleAmbientSoundscape(soundId: String) {
        manuallyPausedBgMusic = false
        val nextId = if (soundId == "none") {
            ambientEngine.stopAll()
            "none"
        } else {
            ambientEngine.toggleSoundscape(soundId)
        }
        persistPreferences(_preferences.value.copy(ambientSoundId = nextId))
    }

    fun toggleBackgroundMusicPlayPause() {
        if (ambientState.value.isPlaying) {
            manuallyPausedBgMusic = true
            ambientEngine.pausePlayback()
        } else {
            manuallyPausedBgMusic = false
            ambientEngine.togglePlayPause()
            persistPreferences(_preferences.value.copy(ambientSoundId = ambientState.value.selectedTrackId))
        }
    }

    fun setBackgroundMusicEnabled(enabled: Boolean) {
        manuallyPausedBgMusic = false
        ambientEngine.setEnabled(enabled, autoPlayNow = enabled)
        val persistedId = if (enabled) ambientState.value.selectedTrackId else "none"
        persistPreferences(_preferences.value.copy(ambientSoundId = persistedId))
    }

    fun selectBackgroundMusicTrack(soundId: String) {
        ambientEngine.selectBackgroundTrack(soundId)
        if (ambientState.value.isEnabled) {
            persistPreferences(_preferences.value.copy(ambientSoundId = soundId))
        }
    }

    fun previewAmbientSoundscape(soundId: String) {
        ambientEngine.previewSoundscape(soundId)
    }

    fun updateAmbientVolume(volume: Float) {
        ambientEngine.setVolume(volume)
        persistPreferences(_preferences.value.copy(ambientVolume = volume.coerceIn(0f, 1f)))
    }

    fun stopAllAmbience() {
        manuallyPausedBgMusic = false
        ambientEngine.stopAll()
        persistPreferences(_preferences.value.copy(ambientSoundId = "none"))
    }

    fun setDemoPremiumEnabled(enabled: Boolean) {
        persistPreferences(_preferences.value.copy(isDemoPremium = enabled))
    }

    fun dismissAuthBanner() {
        _authBannerDismissed.value = true
        viewModelScope.launch {
            runCatching { guestStore.setAuthBannerDismissed(true) }
        }
    }

    private fun persistPreferences(updated: UserPreferencesState) {
        _preferences.value = updated
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        inFlightLocalPrefWrites.incrementAndGet()
        viewModelScope.launch {
            try {
                runCatching { guestStore.savePreferences(updated) }
            } finally {
                inFlightLocalPrefWrites.decrementAndGet()
            }
            if (uid != null && repo != null) {
                val fsModel = FirestoreUserPreferences(
                    userId = uid,
                    preferredLanguage = updated.preferredLanguage.code,
                    themeMode = if (updated.darkTheme) "dark" else "light",
                    reducedMotion = updated.reducedMotion,
                    arabicFontScale = updated.arabicFontScale.toDouble(),
                    autoplayNext = updated.autoplayNext,
                    defaultRepeatMode = updated.defaultRepeatMode.code,
                    ambientSoundId = updated.ambientSoundId,
                    ambientVolume = updated.ambientVolume.toDouble(),
                    preferredAudioSourceCode = updated.preferredAudioSourceCode,
                    favoriteReciterIds = updated.favoriteReciterIds.toList(),
                    favoriteTrackIds = updated.favoriteTrackIds.toList(),
                    savedCollectionIds = updated.savedCollectionIds.toList(),
                    isDemoPremium = updated.isDemoPremium
                )
                val res = repo.saveUserPreferences(fsModel)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    // --- Custom Playlists ---

    fun createCustomPlaylist(name: String, description: String, initialTrackIds: List<String> = emptyList()) {
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        if (uid == null || repo == null) {
            val now = System.currentTimeMillis()
            val localId = "local_pl_${java.util.UUID.randomUUID().toString().replace("-", "").take(12)}"
            val optimistic = CustomPlaylist(
                id = localId,
                ownerId = "local_guest",
                name = name.trim().take(80).ifEmpty { "My Playlist" },
                description = description.trim().take(300),
                trackIds = initialTrackIds.take(200),
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
            pendingDeletedPlaylistIds.remove(localId)
            pendingOptimisticPlaylists[localId] = optimistic
            _playlists.update { current -> listOf(optimistic) + current.filterNot { it.id == localId } }
            viewModelScope.launch {
                try {
                    runCatching { guestStore.createPlaylist(name, description, initialTrackIds, fixedPlaylistId = localId) }
                } finally {
                    pendingOptimisticPlaylists.remove(localId)
                }
            }
        } else {
            viewModelScope.launch {
                val res = repo.createPlaylist(name, description, initialTrackIds)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    fun updateCustomPlaylistMetadata(playlist: CustomPlaylist, newName: String, newDescription: String) {
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        if (uid == null || repo == null) {
            val now = System.currentTimeMillis()
            val updatedPl = playlist.copy(
                name = newName.trim().take(80).ifEmpty { "My Playlist" },
                description = newDescription.trim().take(300),
                updatedAtEpochMs = now
            )
            pendingOptimisticPlaylists[playlist.id] = updatedPl
            _playlists.update { list ->
                list.map { pl -> if (pl.id == playlist.id) updatedPl else pl }
            }
            viewModelScope.launch {
                try {
                    runCatching { guestStore.updatePlaylist(playlist.id, newName, newDescription, playlist.trackIds) }
                } finally {
                    pendingOptimisticPlaylists.remove(playlist.id)
                }
            }
        } else {
            viewModelScope.launch {
                val res = repo.updatePlaylist(playlist.id, newName, newDescription, playlist.trackIds)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    fun addTrackToPlaylist(playlist: CustomPlaylist, trackId: String) {
        if (trackId in playlist.trackIds) return
        val updatedTracks = (playlist.trackIds + trackId).take(200)
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        if (uid == null || repo == null) {
            val now = System.currentTimeMillis()
            val updatedPl = playlist.copy(trackIds = updatedTracks, updatedAtEpochMs = now)
            pendingOptimisticPlaylists[playlist.id] = updatedPl
            _playlists.update { list ->
                list.map { pl -> if (pl.id == playlist.id) updatedPl else pl }
            }
            viewModelScope.launch {
                try {
                    runCatching { guestStore.updatePlaylist(playlist.id, playlist.name, playlist.description, updatedTracks) }
                } finally {
                    pendingOptimisticPlaylists.remove(playlist.id)
                }
            }
        } else {
            viewModelScope.launch {
                val res = repo.updatePlaylist(playlist.id, playlist.name, playlist.description, updatedTracks)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    fun removeTrackFromPlaylist(playlist: CustomPlaylist, trackId: String) {
        val updatedTracks = playlist.trackIds.filterNot { it == trackId }
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        if (uid == null || repo == null) {
            val now = System.currentTimeMillis()
            val updatedPl = playlist.copy(trackIds = updatedTracks, updatedAtEpochMs = now)
            pendingOptimisticPlaylists[playlist.id] = updatedPl
            _playlists.update { list ->
                list.map { pl -> if (pl.id == playlist.id) updatedPl else pl }
            }
            viewModelScope.launch {
                try {
                    runCatching { guestStore.updatePlaylist(playlist.id, playlist.name, playlist.description, updatedTracks) }
                } finally {
                    pendingOptimisticPlaylists.remove(playlist.id)
                }
            }
        } else {
            viewModelScope.launch {
                val res = repo.updatePlaylist(playlist.id, playlist.name, playlist.description, updatedTracks)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    fun reorderPlaylistTrack(playlist: CustomPlaylist, fromIndex: Int, delta: Int) {
        val mutable = playlist.trackIds.toMutableList()
        if (fromIndex !in mutable.indices) return
        val target = (fromIndex + delta).coerceIn(0, mutable.lastIndex)
        if (fromIndex == target) return
        val moved = mutable.removeAt(fromIndex)
        mutable.add(target, moved)

        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        if (uid == null || repo == null) {
            val now = System.currentTimeMillis()
            val updatedPl = playlist.copy(trackIds = mutable, updatedAtEpochMs = now)
            pendingOptimisticPlaylists[playlist.id] = updatedPl
            _playlists.update { list ->
                list.map { pl -> if (pl.id == playlist.id) updatedPl else pl }
            }
            viewModelScope.launch {
                try {
                    runCatching { guestStore.updatePlaylist(playlist.id, playlist.name, playlist.description, mutable) }
                } finally {
                    pendingOptimisticPlaylists.remove(playlist.id)
                }
            }
        } else {
            viewModelScope.launch {
                val res = repo.updatePlaylist(playlist.id, playlist.name, playlist.description, mutable)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    fun deleteCustomPlaylist(playlistId: String) {
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        if (uid == null || repo == null) {
            pendingOptimisticPlaylists.remove(playlistId)
            pendingDeletedPlaylistIds.add(playlistId)
            _playlists.update { list -> list.filterNot { it.id == playlistId } }
            viewModelScope.launch {
                try {
                    runCatching { guestStore.deletePlaylist(playlistId) }
                } finally {
                    pendingDeletedPlaylistIds.remove(playlistId)
                }
            }
        } else {
            viewModelScope.launch {
                val res = repo.deletePlaylist(playlistId)
                if (res.isFailure) {
                    _syncErrorMessage.value = res.exceptionOrNull()?.localizedMessage
                }
            }
        }
    }

    // --- Search & Filters ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun commitRecentSearch(query: String) {
        viewModelScope.launch {
            runCatching { guestStore.addRecentSearch(query) }
        }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        viewModelScope.launch {
            runCatching { guestStore.clearRecentSearches() }
        }
    }

    fun setSearchTab(tab: String) {
        _searchTab.value = tab
    }

    fun setCollectionFilter(category: CollectionCategory) {
        _collectionFilter.value = category
    }

    fun setCollectionSearchQuery(query: String) {
        _collectionSearchQuery.value = query
    }

    fun setReciterSearchQuery(query: String) {
        _reciterSearchQuery.value = query
    }

    fun setReciterCountryFilter(country: String) {
        _reciterCountryFilter.value = country
    }

    fun setReciterSortMode(mode: String) {
        _reciterSortMode.value = mode
    }

    private fun recordVerifiedListeningEvent(
        track: AudioTrack,
        secondsPlayed: Int,
        lastPositionMs: Long,
        sessionId: String
    ) {
        val uid = activeAuthenticatedUid
        val repo = firestoreRepository
        viewModelScope.launch {
            if (uid != null && repo != null) {
                repo.recordListeningEvent(
                    trackId = track.id,
                    surahNumber = track.surahNumber,
                    surahName = track.surahName,
                    reciterId = track.reciterId,
                    reciterName = track.reciterName,
                    durationListenedSec = secondsPlayed,
                    lastPositionMs = lastPositionMs,
                    sessionId = sessionId
                )
            } else {
                guestStore.recordListeningEvent(
                    trackId = track.id,
                    surahNumber = track.surahNumber,
                    surahName = track.surahName,
                    reciterId = track.reciterId,
                    reciterName = track.reciterName,
                    durationListenedSec = secondsPlayed,
                    lastPositionMs = lastPositionMs,
                    sessionId = sessionId
                )
            }
        }
    }

    private fun computeInsights(events: List<ListeningEventRecord>): ListeningInsightsSummary {
        if (events.isEmpty()) return ListeningInsightsSummary()

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfTodayMs = cal.timeInMillis
        val sevenDaysAgoMs = startOfTodayMs - 6L * 24L * 3600L * 1000L

        val todaySec = events
            .filter { it.timestampEpochMs >= startOfTodayMs }
            .sumOf { it.durationListenedSec }

        val weekEvents = events.filter { it.timestampEpochMs >= sevenDaysAgoMs }
        val weekSec = weekEvents.sumOf { it.durationListenedSec }
        val distinctSessions = events.map { it.sessionId }.distinct().size

        val dayFmt = SimpleDateFormat("EEE", Locale.US)
        val trend = (6 downTo 0).map { daysBack ->
            val dayStart = startOfTodayMs - daysBack * 24L * 3600L * 1000L
            val dayEnd = dayStart + 24L * 3600L * 1000L
            val secs = events
                .filter { it.timestampEpochMs in dayStart until dayEnd }
                .sumOf { it.durationListenedSec }
            DailyListeningPoint(
                dayLabel = dayFmt.format(Date(dayStart)),
                secondsListened = secs
            )
        }

        val reciterStats = events
            .groupBy { it.reciterId to it.reciterName }
            .map { (pair, recs) ->
                ReciterStatItem(
                    reciterId = pair.first,
                    reciterName = pair.second,
                    totalSeconds = recs.sumOf { it.durationListenedSec },
                    sessionCount = recs.size
                )
            }
            .sortedByDescending { it.totalSeconds }
            .take(5)

        return ListeningInsightsSummary(
            todaySeconds = todaySec,
            weekSeconds = weekSec,
            distinctSessions = distinctSessions,
            dailyTrend = trend,
            frequentReciters = reciterStats,
            recentEvents = events.take(25)
        )
    }

    override fun onCleared() {
        super.onCleared()
        cloudSyncJob?.cancel()
        audioEngine.release()
        ambientEngine.release()
    }

    private companion object {
        const val TAG = "TilawahVM"
    }
}
