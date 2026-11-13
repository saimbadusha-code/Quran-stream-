package com.example

import android.content.Context
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GuestLibraryDataStore
import java.io.File
import java.util.UUID
import com.example.data.model.AudioSourceProvider
import com.example.data.model.CollectionCategory
import com.example.data.model.DownloadPermission
import com.example.data.model.RepeatMode
import com.example.data.provider.TilawahAudioProvider
import com.example.data.remote.AlQuranCloudEditionDto
import com.example.data.remote.AlQuranCloudEditionsResponseDto
import com.example.data.remote.AudioFileDto
import com.example.data.remote.AuthenticatedSeedCatalog
import com.example.data.remote.ChapterRecitationResponseDto
import com.example.data.remote.ChapterReciterDto
import com.example.data.remote.ChapterRecitersResponseDto
import com.example.data.remote.ChaptersResponseDto
import com.example.data.remote.HttpStreamProbeResult
import com.example.data.remote.QuranApiService
import com.example.data.remote.QuranCatalogRepository
import com.example.data.remote.StreamProbeClient
import com.example.data.remote.TranslationsResponseDto
import com.example.data.remote.VerseDto
import com.example.data.remote.VerseTimestampDto
import com.example.data.remote.VerseTranslationDto
import com.example.data.remote.VersesResponseDto
import com.example.player.AmbientSoundEngine
import com.example.player.AudioBackendDriver
import com.example.player.QuranAudioEngine
import com.example.viewmodel.TilawahViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

private class FakeStreamProbeClient : StreamProbeClient {
    override suspend fun fetchSurahAudioEditions(): List<AlQuranCloudEditionDto> = listOf(
        AlQuranCloudEditionDto(identifier = "ar.alafasy", englishName = "Alafasy", bitrate = "128"),
        AlQuranCloudEditionDto(identifier = "ar.husary", englishName = "Husary", bitrate = "128"),
        AlQuranCloudEditionDto(identifier = "ar.abdulbasitmurattal", englishName = "Abdul Basit", bitrate = "192"),
        AlQuranCloudEditionDto(identifier = "ar.minshawi", englishName = "Minshawi", bitrate = "128"),
        AlQuranCloudEditionDto(identifier = "ar.abdurrahmaansudais", englishName = "As-Sudais", bitrate = "192"),
        AlQuranCloudEditionDto(identifier = "ar.mahermuaiqly", englishName = "Maher Al Muaiqly", bitrate = "128"),
        AlQuranCloudEditionDto(identifier = "ar.shaatree", englishName = "Abu Bakr Ash-Shaatree", bitrate = "128")
    )

    override suspend fun fetchVerseAudioEditions(): List<AlQuranCloudEditionDto> = fetchSurahAudioEditions()

    override suspend fun verifyAudioUrl(url: String): HttpStreamProbeResult =
        HttpStreamProbeResult(
            isReachable = url.startsWith("https://"),
            httpCode = if (url.startsWith("https://")) 200 else 0,
            contentType = "audio/mpeg",
            diagnosticMessage = "Verified by FakeStreamProbeClient"
        )
}

private class FakeQuranApiService : QuranApiService {
    override suspend fun getChapters(language: String): ChaptersResponseDto =
        ChaptersResponseDto(chapters = emptyList())

    override suspend fun getChapterReciters(language: String): ChapterRecitersResponseDto =
        ChapterRecitersResponseDto(
            reciters = listOf(
                ChapterReciterDto(id = 99, name = "Yasser Al-Dosari", arabicName = "ياسر الدوسري")
            )
        )

    override suspend fun getChapterRecitation(
        reciterId: Int,
        chapterNumber: Int,
        segments: Boolean
    ): ChapterRecitationResponseDto = ChapterRecitationResponseDto(
        audioFile = AudioFileDto(
            id = chapterNumber,
            chapterId = chapterNumber,
            format = "mp3",
            audioUrl = "https://download.quranicaudio.com/qdc/mishari_al_afasy/murattal/$chapterNumber.mp3",
            timestamps = listOf(
                VerseTimestampDto("1:1", 0L, 4500L),
                VerseTimestampDto("1:2", 4501L, 9000L)
            )
        )
    )

    override suspend fun getVersesByChapter(
        chapterNumber: Int,
        translationId: Int,
        fields: String,
        perPage: Int,
        page: Int
    ): VersesResponseDto = VersesResponseDto(
        verses = listOf(
            VerseDto(
                id = 1,
                verseNumber = 1,
                verseKey = "$chapterNumber:1",
                textUthmani = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                translations = listOf(
                    VerseTranslationDto(
                        id = 101,
                        resourceId = translationId,
                        text = "In the name of Allah, the Entirely Merciful, the Especially Merciful."
                    )
                )
            )
        )
    )

    override suspend fun getTranslations(language: String): TranslationsResponseDto =
        TranslationsResponseDto(translations = emptyList())
}

private class DeterministicAudioDriver : AudioBackendDriver {
    var playing = false
    var posMs = 0L
    var durMs = 64_000L
    var currentVol = 0.9f

    override fun loadAndPlay(
        url: String,
        volume: Float,
        onPrepared: (durationMs: Long) -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        this.currentVol = volume
        this.posMs = 0L
        this.playing = true
        onPrepared(durMs)
    }

    override fun play() {
        playing = true
    }

    override fun pause() {
        playing = false
    }

    override fun seekTo(positionMs: Long) {
        posMs = positionMs
    }

    override fun setVolume(volume: Float) {
        this.currentVol = volume
    }

    override fun currentPositionMs(): Long = posMs
    override fun durationMs(): Long = durMs
    override fun isActuallyPlaying(): Boolean = playing
    override fun stopAndRelease() {
        playing = false
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createIsolatedGuestStore(context: Context): GuestLibraryDataStore {
        val tempFile = File(context.cacheDir, "test_guest_${UUID.randomUUID()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { tempFile }
        )
        return GuestLibraryDataStore(context, customDataStore = ds)
    }

    @Test
    fun `app name and canonical 114 surah catalog integrity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Tilawah", appName)

        val surahs = AuthenticatedSeedCatalog.canonicalSurahs
        assertEquals(114, surahs.size)
        assertEquals("Al-Fatihah", surahs.first().displayName)
        assertEquals("An-Nas", surahs.last().displayName)

        // Verify normalized search matching ("fatiha" matches "Al-Fatihah")
        val normFatiha = QuranCatalogRepository.normalizeForSearch("Al-Fatihah")
        val queryNorm = QuranCatalogRepository.normalizeForSearch("fatiha")
        assertTrue(normFatiha.contains(queryNorm))
    }

    @Test
    fun `moshi kotlin json adapter deserializes remote api dtos without ksp`() {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val chaptersJson = """
            {
              "chapters": [
                {
                  "id": 1,
                  "revelation_place": "makkah",
                  "name_simple": "Al-Fatihah",
                  "name_arabic": "الفاتحة",
                  "verses_count": 7,
                  "translated_name": { "name": "The Opener", "language_name": "english" }
                }
              ]
            }
        """.trimIndent()
        val chaptersDto = moshi.adapter(ChaptersResponseDto::class.java).fromJson(chaptersJson)
        assertNotNull(chaptersDto)
        assertEquals(1, chaptersDto?.chapters?.size)
        assertEquals("Al-Fatihah", chaptersDto?.chapters?.first()?.nameSimple)
        assertEquals("makkah", chaptersDto?.chapters?.first()?.revelationPlace)
        assertEquals(7, chaptersDto?.chapters?.first()?.versesCount)
        assertEquals("The Opener", chaptersDto?.chapters?.first()?.translatedName?.name)

        val editionsJson = """
            {
              "code": 200,
              "status": "OK",
              "data": [
                {
                  "identifier": "ar.alafasy",
                  "englishName": "Alafasy",
                  "format": "audio",
                  "bitrate": "128"
                }
              ]
            }
        """.trimIndent()
        val editionsDto = moshi.adapter(AlQuranCloudEditionsResponseDto::class.java).fromJson(editionsJson)
        assertNotNull(editionsDto)
        assertEquals("ar.alafasy", editionsDto?.data?.first()?.identifier)
        assertEquals("Alafasy", editionsDto?.data?.first()?.englishName)
        assertEquals("128", editionsDto?.data?.first()?.bitrate)
    }

    @Test
    fun `audio engine manages queue repeat shuffle seek and sleep timer`() {
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val engine = QuranAudioEngine(
            scope = scope,
            catalogRepository = repo,
            driver = driver,
            onVerifiedListeningProgress = { _, _, _, _ -> }
        )

        val t1 = repo.buildTrack("mishary_alafasy", 1)
        val t2 = repo.buildTrack("mishary_alafasy", 36)
        val t3 = repo.buildTrack("mishary_alafasy", 67)

        engine.playTrack(t1, newQueue = listOf(t1, t2, t3), shuffle = false)
        Thread.sleep(80L)

        assertEquals(3, engine.state.value.queue.size)
        assertEquals(1, engine.state.value.currentEntry?.track?.surahNumber)

        engine.cycleRepeatMode()
        assertEquals(RepeatMode.ALL, engine.state.value.repeatMode)
        engine.cycleRepeatMode()
        assertEquals(RepeatMode.ONE, engine.state.value.repeatMode)

        engine.seekTo(5000L)
        assertEquals(5000L, engine.state.value.positionMs)

        engine.startSleepTimer(15)
        assertNotNull(engine.state.value.sleepTimerRemainingSec)
        engine.cancelSleepTimer()
        assertEquals(null, engine.state.value.sleepTimerRemainingSec)

        engine.release()
    }

    @Test
    fun `audio provider validates al quran cloud everyayah spoken sessions and offline licence rules`() {
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val provider = TilawahAudioProvider(repo)

        assertEquals(1, TilawahAudioProvider.computeGlobalAyahNumber(1, 1))
        assertEquals(262, TilawahAudioProvider.computeGlobalAyahNumber(2, 255))
        assertEquals(6236, TilawahAudioProvider.computeGlobalAyahNumber(114, 6))

        val surahTracks = provider.getSurahTracks(1, "mishary_alafasy")
        val alQuranTrack = surahTracks.first { it.sourceProvider == AudioSourceProvider.AL_QURAN_CLOUD }
        assertEquals(
            "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/1.mp3",
            alQuranTrack.audioUrl
        )
        val alQuranLicence = provider.getLicenceMetadata(alQuranTrack)
        assertTrue(alQuranLicence.permissionConfirmed)
        assertEquals(DownloadPermission.PERSONAL_OFFLINE_PERMITTED, alQuranLicence.downloadPermission)

        val kursiDosari = provider.getVerseAudio(2, 255, "yasser_dosari")
        assertNotNull(kursiDosari)
        assertEquals(
            "https://everyayah.com/data/Yasser_Ad-Dussary_128kbps/002255.mp3",
            kursiDosari?.audioUrl
        )
        val everyAyahLicence = provider.getLicenceMetadata(kursiDosari!!)
        assertFalse(everyAyahLicence.permissionConfirmed)
        assertEquals(DownloadPermission.STREAM_ONLY, everyAyahLicence.downloadPermission)

        assertEquals(8, provider.getBackgroundSounds().size)
        assertEquals(9, provider.getBackgroundVoiceTracks().size)

        // Verify all core Popular Collections have unique cover artwork drawables
        val coreCollections = AuthenticatedSeedCatalog.curatedCollections.filter {
            it.category != CollectionCategory.RECITER
        }
        assertEquals(
            coreCollections.size,
            coreCollections.map { it.coverResId }.distinct().size
        )

        val ambientEngine = AmbientSoundEngine(enableAudioHardware = false)
        assertFalse(ambientEngine.state.value.isPlaying)
        assertFalse(ambientEngine.state.value.isEnabled)
        assertEquals("painted_dream", ambientEngine.state.value.selectedTrackId)
        ambientEngine.toggleSoundscape("rain")
        assertTrue(ambientEngine.state.value.isPlaying)
        assertTrue(ambientEngine.state.value.isEnabled)
        assertEquals("rain", ambientEngine.state.value.activeSoundId)
        ambientEngine.stopAll()
        assertFalse(ambientEngine.state.value.isPlaying)
        assertFalse(ambientEngine.state.value.isEnabled)
        assertEquals("none", ambientEngine.state.value.activeSoundId)
        ambientEngine.release()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w390dp-h844dp")
    fun `mobile layout renders quranify home hierarchy standby mini player bottom nav now playing read sheet and background music`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = createIsolatedGuestStore(context),
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )

        composeRule.setContent {
            TilawahRootApp(
                viewModelOverride = vm,
                initialGuestBypass = false
            )
        }

        // 1. Starts on Auth Welcome Gate with zero autoplay on launch
        assertFalse(vm.playerState.value.isPlaying)
        assertFalse(vm.ambientState.value.isPlaying)
        assertFalse(vm.ambientState.value.isEnabled)
        composeRule.onNodeWithTag("auth_welcome_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("google_sign_in_button").assertIsDisplayed()
        composeRule.onNodeWithTag("continue_guest_button").performClick()
        composeRule.waitForIdle()

        // 2. Verify Mobile Top Header + Your Reciter + Popular Collections + Best for Tajwid + Standby Mini-Player
        composeRule.onNodeWithTag("mobile_top_header").assertIsDisplayed()
        composeRule.onNodeWithTag("top_search_shortcut").assertIsDisplayed()
        composeRule.onNodeWithTag("top_language_button").assertIsDisplayed()
        composeRule.onNodeWithTag("your_reciter_hero_card").assertIsDisplayed()
        composeRule.onNodeWithTag("hero_play_button").assertIsDisplayed()
        composeRule.onNodeWithTag("hero_view_reciter_button").assertIsDisplayed()
        composeRule.onNodeWithTag("popular_collections_row").assertIsDisplayed()

        // Standby mini-player is visible above 5-item bottom nav without auto-playing
        assertFalse(vm.playerState.value.isPlaying)
        composeRule.onNodeWithTag("mobile_mini_player").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_bottom_nav").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_home").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_collections").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_reciters").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_search").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_nav_more").assertIsDisplayed()

        composeRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("tajwid_reciters_row"))
        composeRule.onNodeWithTag("tajwid_reciters_row").assertIsDisplayed()

        composeRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("spoken_islamic_audio_section"))
        composeRule.onNodeWithTag("spoken_islamic_audio_section").assertIsDisplayed()

        // 3. Play Your Reciter (Al-Fatihah by Yasser Al-Dosari), advance position, and open Now Playing without restarting
        composeRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("your_reciter_hero_card"))
        composeRule.onNodeWithTag("hero_play_button").performClick()
        Thread.sleep(100L)
        ShadowLooper.idleMainLooper()
        composeRule.waitForIdle()
        assertTrue(vm.playerState.value.isPlaying)
        driver.posMs = 45_000L
        ShadowLooper.idleMainLooper(600, TimeUnit.MILLISECONDS)
        composeRule.waitForIdle()
        assertEquals(45_000L, vm.playerState.value.positionMs)

        // Open Full-Screen Now Playing from Mini-Player and verify position is preserved (no restart)
        composeRule.onNodeWithTag("mobile_mini_player").performClick()
        composeRule.waitForIdle()
        assertEquals(45_000L, vm.playerState.value.positionMs)
        assertTrue(vm.playerState.value.isPlaying)

        composeRule.onNodeWithTag("now_playing_fullscreen").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_artwork").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_surah_title").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_reciter_name").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_footer_play_pause_button").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_favorite_button").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_queue_button").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_read_button").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_bg_sounds_button").assertIsDisplayed()
        composeRule.onNodeWithTag("background_music_panel").assertIsDisplayed()

        // 4. Test independent Background Music panel inside Now Playing
        val quranVolBefore = vm.playerState.value.volume
        composeRule.onNodeWithTag("bg_music_play_pause_button").performClick()
        composeRule.waitForIdle()
        assertTrue(vm.ambientState.value.isPlaying)
        assertTrue(vm.ambientState.value.isEnabled)
        assertEquals("painted_dream", vm.ambientState.value.activeSoundId)

        // Adjust background music volume independently — Quran volume must remain untouched
        vm.updateAmbientVolume(0.68f)
        composeRule.waitForIdle()
        assertEquals(0.68f, vm.ambientState.value.volume, 0.01f)
        assertEquals(quranVolBefore, vm.playerState.value.volume, 0.01f)

        // Switch background track — Quran recitation track and position must remain untouched
        vm.selectBackgroundMusicTrack("rain")
        composeRule.waitForIdle()
        assertEquals("rain", vm.ambientState.value.activeSoundId)
        assertEquals(1, vm.playerState.value.currentEntry?.track?.surahNumber)
        assertTrue(vm.playerState.value.isPlaying)

        // Stop Music stops only background soundscape while Quran recitation keeps playing
        composeRule.onNodeWithTag("bg_music_stop_button").performClick()
        composeRule.waitForIdle()
        assertFalse(vm.ambientState.value.isPlaying)
        assertFalse(vm.ambientState.value.isEnabled)
        assertTrue(vm.playerState.value.isPlaying)

        // 5. Open synchronized Quran Reading & Translations sheet from "Read" action
        composeRule.onNodeWithTag("now_playing_read_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("translation_selector_button").assertIsDisplayed()
        composeRule.onNodeWithTag("play_verse_audio_1").assertIsDisplayed()
        composeRule.onNodeWithTag("close_read_sheet_button").performClick()
        composeRule.waitForIdle()

        // Minimize Now Playing back to Home
        composeRule.onNodeWithTag("minimize_now_playing_button").performClick()
        composeRule.waitForIdle()

        // 6. Navigate across Collections, Reciters, Search, More -> Library & Settings while Quran keeps playing
        composeRule.onNodeWithTag("bottom_nav_collections").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("collections_overview_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_mini_player").assertIsDisplayed()
        assertTrue(vm.playerState.value.isPlaying)

        composeRule.onNodeWithTag("bottom_nav_reciters").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reciters_overview_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_mini_player").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_search").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("search_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_mini_player").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_more").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("more_item_library").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("library_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("library_tab_offline").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_more").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_screen")
            .performScrollToNode(hasTestTag("more_item_settings"))
        composeRule.onNodeWithTag("more_item_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        assertTrue(vm.playerState.value.isPlaying)

        vm.audioEngine.release()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1280dp-h800dp")
    fun `desktop layout renders sidebar split hero reciter browser persistent player and ambience studio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = createIsolatedGuestStore(context),
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )

        composeRule.setContent {
            TilawahRootApp(
                viewModelOverride = vm,
                initialGuestBypass = true
            )
        }
        composeRule.waitForIdle()

        // 1. Desktop Sidebar + Top Search Pill + Persistent Desktop Player Bar
        composeRule.onNodeWithTag("desktop_sidebar").assertIsDisplayed()
        composeRule.onNodeWithTag("sidebar_nav_home").assertIsDisplayed()
        composeRule.onNodeWithTag("sidebar_nav_collections").assertIsDisplayed()
        composeRule.onNodeWithTag("sidebar_nav_reciters").assertIsDisplayed()
        composeRule.onNodeWithTag("sidebar_nav_library").assertIsDisplayed()
        composeRule.onNodeWithTag("top_search_shortcut").assertIsDisplayed()
        composeRule.onNodeWithTag("desktop_player_bar").assertIsDisplayed()

        // 2. Interactive 'Your Reciter' switcher & quick surah playback on desktop split hero
        composeRule.onNodeWithTag("your_reciter_hero_card").assertIsDisplayed()
        composeRule.onNodeWithTag("hero_switcher_mishary_alafasy").performClick()
        composeRule.waitForIdle()
        assertEquals("mishary_alafasy", vm.selectedHeroReciterId.value)

        composeRule.onNodeWithTag("hero_quick_surah_1").performClick()
        Thread.sleep(80L)
        composeRule.waitForIdle()
        assertEquals(1, vm.playerState.value.currentEntry?.track?.surahNumber)

        // 3. Open Background Sounds Studio from persistent desktop player bar
        composeRule.onNodeWithTag("player_ambience_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("background_sounds_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("ambience_volume_slider").assertIsDisplayed()
        composeRule.onNodeWithTag("ambience_option_rain").assertIsDisplayed()
        composeRule.onNodeWithTag("ambience_toggle_rain").performClick()
        composeRule.waitForIdle()
        assertTrue(vm.ambientState.value.isPlaying)
        assertEquals("rain", vm.ambientState.value.activeSoundId)
        composeRule.onNodeWithTag("stop_all_ambience_button").performClick()
        composeRule.waitForIdle()
        assertFalse(vm.ambientState.value.isPlaying)

        // 4. Close Ambience Studio modal & verify Reciters browser + Surah index on desktop width
        composeRule.onNodeWithTag("ambience_done_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("sidebar_nav_reciters").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reciters_overview_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("reciter_card_yasser_dosari").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reciter_detail_screen").assertIsDisplayed()
    }

    @Test
    fun `audit hero reciter stream progress toggle verse timing sync search and guest persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val guestStore = createIsolatedGuestStore(context)
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = guestStore,
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )
        fun pump(ms: Long = 100L) {
            Thread.sleep(ms)
            ShadowLooper.idleMainLooper()
        }
        pump(120L)

        // 1. Verified QuranicAudio 3-digit padded direct URLs for Dosari, Ghamdi, and Muaiqly
        assertEquals(
            "https://download.quranicaudio.com/quran/yasser_ad-dussary/001.mp3",
            AuthenticatedSeedCatalog.buildQuranicAudioDirectUrl("yasser_dosari", 1)
        )
        assertEquals(
            "https://download.quranicaudio.com/quran/sa3d_al-ghaamidi/complete/036.mp3",
            AuthenticatedSeedCatalog.buildQuranicAudioDirectUrl("saad_al_ghamdi", 36)
        )
        assertEquals(
            "https://download.quranicaudio.com/quran/maher_256/114.mp3",
            AuthenticatedSeedCatalog.buildQuranicAudioDirectUrl("maher_muaiqly", 114)
        )

        // 2. Play default hero reciter (Yasser Al-Dosari), advance real media progress, and toggle pause on second tap
        vm.playSurahWithReciter("yasser_dosari", 1)
        pump(150L)
        assertTrue(vm.playerState.value.isPlaying)
        assertEquals("yasser_dosari", vm.playerState.value.currentEntry?.track?.reciterId)

        driver.posMs = 12_400L
        ShadowLooper.idleMainLooper(600, TimeUnit.MILLISECONDS)
        assertEquals(12_400L, vm.playerState.value.positionMs)

        // Tapping same active surah row toggles pause without resetting position to 0
        vm.playSurahWithReciter("yasser_dosari", 1)
        ShadowLooper.idleMainLooper()
        assertFalse(vm.playerState.value.isPlaying)
        assertEquals(12_400L, vm.playerState.value.positionMs)

        // Tapping again resumes from 12_400L
        vm.playSurahWithReciter("yasser_dosari", 1)
        ShadowLooper.idleMainLooper()
        assertTrue(vm.playerState.value.isPlaying)
        assertEquals(12_400L, vm.playerState.value.positionMs)

        // 3. Verse timings from resolved recitation sync into scriptureState.verses
        pump(120L)
        val syncedFirstVerse = vm.scriptureState.value.verses.firstOrNull()
        assertNotNull(syncedFirstVerse)
        assertEquals(0L, syncedFirstVerse?.timestampFromMs)
        assertEquals(4500L, syncedFirstVerse?.timestampToMs)

        // Single-ayah playback sets activeVerseKey immediately
        vm.playVerseAudio(2, 255)
        pump(150L)
        assertEquals("2:255", vm.playerState.value.activeVerseKey)

        // 4. Synchronous search normalization ("Yasin" -> Ya-Sin, "Duha" -> Ad-Duhaa, "Kursi" -> spoken session)
        vm.updateSearchQuery("Yasin")
        ShadowLooper.idleMainLooper()
        assertEquals("Yasin", vm.searchState.value.query)
        assertTrue(vm.searchState.value.matchingSurahs.any { it.number == 36 })

        vm.updateSearchQuery("Duha")
        ShadowLooper.idleMainLooper()
        assertTrue(vm.searchState.value.matchingSurahs.any { it.number == 93 })

        vm.updateSearchQuery("Kursi")
        ShadowLooper.idleMainLooper()
        assertTrue(vm.searchState.value.matchingSpokenSessions.isNotEmpty())
        vm.updateSearchQuery("")
        ShadowLooper.idleMainLooper()

        // 5. Guest favorites & playlist persistence
        val targetTrackId = "mishary_alafasy:1"
        val wasFav = targetTrackId in vm.preferences.value.favoriteTrackIds
        vm.toggleFavoriteTrack(targetTrackId)
        pump(120L)
        assertEquals(!wasFav, targetTrackId in vm.preferences.value.favoriteTrackIds)

        vm.createCustomPlaylist("Audit Night Tilawah", "Quiet contemplation", listOf(targetTrackId))
        pump(150L)
        val created = vm.playlists.value.firstOrNull { it.name == "Audit Night Tilawah" }
        assertNotNull(created)
        assertTrue(targetTrackId in (created?.trackIds ?: emptyList()))

        vm.audioEngine.release()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp")
    fun `mobile audit verifies non overlapping mini player dual favorite repeat controls 48dp targets and back stack`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = createIsolatedGuestStore(context),
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )

        val lifecycleOwner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this).apply { currentState = Lifecycle.State.RESUMED }
            override val lifecycle: Lifecycle get() = registry
        }
        val backDispatcher = OnBackPressedDispatcher()
        val backOwner = object : OnBackPressedDispatcherOwner, LifecycleOwner by lifecycleOwner {
            override val onBackPressedDispatcher: OnBackPressedDispatcher = backDispatcher
        }

        composeRule.setContent {
            CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides backOwner) {
                TilawahRootApp(
                    viewModelOverride = vm,
                    initialGuestBypass = true
                )
            }
        }
        composeRule.waitForIdle()

        val track = repo.buildTrack("yasser_dosari", 1)
        vm.audioEngine.playTrack(track)
        Thread.sleep(100L)
        composeRule.waitForIdle()

        // 1. Verify mobile mini-player sits cleanly above mobile bottom navigation without overlap
        val miniBounds = composeRule.onNodeWithTag("mobile_mini_player").fetchSemanticsNode().boundsInRoot
        val navBounds = composeRule.onNodeWithTag("mobile_bottom_nav").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected mini-player bottom (${miniBounds.bottom}) <= bottom nav top (${navBounds.top})",
            miniBounds.bottom <= navBounds.top + 1f
        )

        // 2. Open Now Playing overlay and verify BOTH Favorite and Repeat controls coexist with >= 48dp bounds
        composeRule.onNodeWithTag("mobile_mini_player").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("now_playing_favorite_button")
            .assertIsDisplayed()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("mobile_footer_repeat_button")
            .assertIsDisplayed()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        composeRule.onNodeWithTag("mobile_footer_repeat_button").performClick()
        composeRule.waitForIdle()
        assertEquals(RepeatMode.ALL, vm.playerState.value.repeatMode)

        // 3. System Back presses minimize Now Playing first, then pop route back stack to Home
        composeRule.runOnIdle { backDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("your_reciter_hero_card").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_collections").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("collections_overview_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_more").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_screen")
            .performScrollToNode(hasTestTag("more_item_settings"))
        composeRule.onNodeWithTag("more_item_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()

        composeRule.runOnIdle { backDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_screen").assertIsDisplayed()

        composeRule.runOnIdle { backDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("collections_overview_screen").assertIsDisplayed()

        composeRule.runOnIdle { backDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("your_reciter_hero_card").assertIsDisplayed()

        vm.audioEngine.release()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h640dp")
    fun `small 320dp mobile viewport renders home mini player and now playing without overlap`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = createIsolatedGuestStore(context),
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )

        composeRule.setContent {
            TilawahRootApp(
                viewModelOverride = vm,
                initialGuestBypass = true
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("your_reciter_hero_card").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_mini_player").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_bottom_nav").assertIsDisplayed()

        val miniBounds = composeRule.onNodeWithTag("mobile_mini_player").fetchSemanticsNode().boundsInRoot
        val navBounds = composeRule.onNodeWithTag("mobile_bottom_nav").fetchSemanticsNode().boundsInRoot
        assertTrue(miniBounds.bottom <= navBounds.top + 1f)

        composeRule.onNodeWithTag("mini_player_expand_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("now_playing_fullscreen").assertIsDisplayed()
        composeRule.onNodeWithTag("now_playing_artwork").assertIsDisplayed()
        composeRule.onNodeWithTag("mobile_footer_play_pause_button").assertIsDisplayed()

        vm.audioEngine.release()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w580dp-h360dp-land")
    fun `landscape mobile viewport scrolls now playing and background music panel cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = createIsolatedGuestStore(context),
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )

        composeRule.setContent {
            TilawahRootApp(
                viewModelOverride = vm,
                initialGuestBypass = true
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("mobile_mini_player").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("now_playing_fullscreen").assertIsDisplayed()
        composeRule.onNodeWithTag("minimize_now_playing_button").assertIsDisplayed()

        vm.audioEngine.release()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w700dp-h900dp")
    fun `tablet layout renders navigation rail with insights and premium routes cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranCatalogRepository(
            apiService = FakeQuranApiService(),
            probeClient = FakeStreamProbeClient()
        )
        val driver = DeterministicAudioDriver()
        val vm = TilawahViewModel(
            databaseId = "(default)",
            guestStore = createIsolatedGuestStore(context),
            catalogRepository = repo,
            audioDriver = driver,
            enableAmbientHardware = false
        )

        composeRule.setContent {
            TilawahRootApp(
                viewModelOverride = vm,
                initialGuestBypass = true
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tablet_navigation_rail").assertIsDisplayed()
        composeRule.onNodeWithTag("desktop_player_bar").assertIsDisplayed()

        composeRule.onNodeWithTag("rail_nav_insights").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("insights_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("rail_nav_premium").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("premium_screen").assertIsDisplayed()

        vm.audioEngine.release()
    }
}

