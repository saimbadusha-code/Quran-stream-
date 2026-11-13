package com.example.data.model

import androidx.annotation.DrawableRes

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val isRtl: Boolean = false
) {
    EN("en", "English", false),
    AR("ar", "العربية", true),
    DE("de", "Deutsch", false),
    ES("es", "Español", false),
    FR("fr", "Français", false),
    ID("id", "Bahasa Indonesia", false),
    MS("ms", "Bahasa Melayu", false),
    RU("ru", "Русский", false);

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

enum class RepeatMode(val code: String) {
    OFF("off"),
    ONE("one"),
    ALL("all");

    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }

    companion object {
        fun fromCode(code: String): RepeatMode =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: OFF
    }
}

enum class AudioCategory(val id: String, val displayLabel: String) {
    QURAN_SURAH("quran_surah", "Quran Surah Recitation"),
    QURAN_VERSE("quran_verse", "Verse-Level Recitation"),
    SPOKEN_RUQYA("spoken_ruqya", "Ruqya Recitation"),
    SPOKEN_SUPPLICATION_ADHKAR("spoken_adhkar", "Morning/Evening Adhkar & Duas"),
    ENVIRONMENTAL_AMBIENCE("environmental_ambience", "Environmental Ambience")
}

enum class AudioSourceProvider(
    val code: String,
    val displayName: String,
    val shortBadge: String,
    val docsUrl: String
) {
    AL_QURAN_CLOUD(
        code = "al_quran_cloud",
        displayName = "Al Quran Cloud (Islamic Network CDN)",
        shortBadge = "Al Quran Cloud",
        docsUrl = "https://alquran.cloud/cdn"
    ),
    EVERY_AYAH(
        code = "every_ayah",
        displayName = "EveryAyah Recitation Library",
        shortBadge = "EveryAyah",
        docsUrl = "https://www.everyayah.com/recitations_pages.html"
    ),
    QURAN_FOUNDATION_API(
        code = "quran_foundation",
        displayName = "Quran.com v4 / QuranicAudio CDN",
        shortBadge = "Quran.com API",
        docsUrl = "https://api-docs.quran.com/docs/api-reference/"
    ),
    ON_DEVICE_SYNTHESIS(
        code = "on_device_dsp",
        displayName = "Tilawah First-Party Acoustic DSP",
        shortBadge = "On-Device DSP",
        docsUrl = "local://asset-attribution-register"
    ),
    DEMO_CATALOG_ONLY(
        code = "demo_catalog",
        displayName = "Demo Metadata Catalog",
        shortBadge = "Demo Only",
        docsUrl = "local://demo-mode"
    );

    companion object {
        fun fromCode(code: String): AudioSourceProvider =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AL_QURAN_CLOUD
    }
}

enum class DownloadPermission(val label: String) {
    PERSONAL_OFFLINE_PERMITTED("Personal offline use permitted (attribution required)"),
    STREAM_ONLY("Streaming only (offline download disabled)"),
    NOT_APPLICABLE("Synthesized or metadata-only entry")
}

data class LicenceMetadata(
    val licenceName: String,
    val licenceUrl: String?,
    val sourceProviderName: String,
    val sourceWebpage: String,
    val attributionText: String,
    val downloadPermission: DownloadPermission,
    val permissionConfirmed: Boolean,
    val usageNotes: String
)

enum class TrackAvailability(val statusLabel: String) {
    READY_TO_PLAY("Ready to play"),
    VERIFIED_STREAM("Verified stream"),
    LOADING("Loading"),
    REQUIRES_CONFIGURATION("Requires configuration"),
    UNAVAILABLE_DEMO("Demo metadata only")
}

enum class CollectionCategory(val id: String) {
    ALL("all"),
    FOCUS_WORK("focus_work"),
    SLEEP("sleep"),
    DUAA_RUQIA("duaa_ruqia"),
    EMOTIONAL("emotional"),
    RECITER("reciter"),
    FAVORITES("favorites")
}

enum class SoundCategory(val id: String, val label: String) {
    ALL("all", "All Sounds"),
    AMBIENT("ambient", "Ambient"),
    RAIN("rain", "Rain"),
    WATER("water", "Water"),
    WIND("wind", "Wind"),
    FOREST("forest", "Forest"),
    NIGHT("night", "Night Ambience")
}

data class AudioEditionDescriptor(
    val editionId: String,
    val reciterId: String,
    val displayName: String,
    val provider: AudioSourceProvider,
    val bitrateKbps: Int,
    val supportsFullSurah: Boolean,
    val supportsVerseByVerse: Boolean,
    val style: String = "Murattal"
)

data class Reciter(
    val id: String,
    val chapterReciterId: Int?,
    val displayName: String,
    val arabicName: String?,
    val country: String?,
    val recitationStyle: String?,
    val riwayah: String?,
    val isTajwidFeatured: Boolean,
    val isFeaturedHero: Boolean = false,
    val neutralDescription: String,
    val sourceProvenance: String,
    val avatarAccentHex: Long,
    val alQuranCloudEdition: String? = null,
    val alQuranCloudBitrate: Int? = null,
    val everyAyahFolder: String? = null,
    val everyAyahBitrate: Int? = null
) {
    val initials: String
        get() = displayName
            .split(" ")
            .filter { it.isNotBlank() && !it.startsWith("Al-", ignoreCase = true) }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifEmpty { displayName.take(2).uppercase() }
}

data class Surah(
    val id: String,
    val number: Int,
    val arabicName: String,
    val displayName: String,
    val transliteration: String,
    val englishMeaning: String,
    val revelationPlace: String?,
    val verseCount: Int,
    val sourceIdentifier: String
)

data class AudioTrack(
    val id: String,
    val surahNumber: Int,
    val surahName: String,
    val surahArabicName: String,
    val surahMeaning: String,
    val verseCount: Int,
    val reciterId: String,
    val reciterName: String,
    val chapterReciterId: Int?,
    val audioUrl: String?,
    val durationSec: Int?,
    val format: String = "mp3",
    val sourceMetadata: String,
    val availabilityStatus: TrackAvailability,
    val audioCategory: AudioCategory = AudioCategory.QURAN_SURAH,
    val specificVerseNumber: Int? = null,
    val sourceProvider: AudioSourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
    val bitrateKbps: Int? = null,
    val licenceMetadata: LicenceMetadata = defaultQuranStreamLicence(sourceProvider, reciterName)
)

fun defaultQuranStreamLicence(
    provider: AudioSourceProvider,
    reciterName: String
): LicenceMetadata = when (provider) {
    AudioSourceProvider.AL_QURAN_CLOUD -> LicenceMetadata(
        licenceName = "Islamic Network Open CDN Terms",
        licenceUrl = "https://alquran.cloud/cdn",
        sourceProviderName = provider.displayName,
        sourceWebpage = "https://alquran.cloud/cdn",
        attributionText = "Recitation by $reciterName via Al Quran Cloud (Islamic Network CDN)",
        downloadPermission = DownloadPermission.PERSONAL_OFFLINE_PERMITTED,
        permissionConfirmed = true,
        usageNotes = "Personal streaming and single-surah personal offline caching permitted with attribution preserved. Commercial resale prohibited."
    )

    AudioSourceProvider.EVERY_AYAH -> LicenceMetadata(
        licenceName = "EveryAyah Directory Terms (Per-Recording Rights)",
        licenceUrl = "https://www.everyayah.com/recitations_pages.html",
        sourceProviderName = provider.displayName,
        sourceWebpage = "https://www.everyayah.com/recitations_pages.html",
        attributionText = "Recitation by $reciterName via EveryAyah.com",
        downloadPermission = DownloadPermission.STREAM_ONLY,
        permissionConfirmed = false,
        usageNotes = "Streamed directly from EveryAyah. Offline redistribution disabled because commercial/caching licences vary by studio."
    )

    AudioSourceProvider.QURAN_FOUNDATION_API -> LicenceMetadata(
        licenceName = "Quran.com Public API v4 / QuranicAudio Terms",
        licenceUrl = "https://api-docs.quran.com/docs/api-reference/",
        sourceProviderName = provider.displayName,
        sourceWebpage = "https://quran.com",
        attributionText = "Recitation by $reciterName via Quran.com API v4 / QuranicAudio",
        downloadPermission = DownloadPermission.STREAM_ONLY,
        permissionConfirmed = false,
        usageNotes = "Streamed over HTTPS via Quran.com v4 / QuranicAudio QDC. Offline download disabled unless explicitly authorized."
    )

    AudioSourceProvider.ON_DEVICE_SYNTHESIS -> LicenceMetadata(
        licenceName = "Tilawah First-Party Procedural Audio Licence",
        licenceUrl = null,
        sourceProviderName = provider.displayName,
        sourceWebpage = "local://tilawah-dsp",
        attributionText = "Generated in real time by Tilawah Procedural Acoustic Engine",
        downloadPermission = DownloadPermission.NOT_APPLICABLE,
        permissionConfirmed = true,
        usageNotes = "100% original non-musical acoustic synthesis owned by the application."
    )

    AudioSourceProvider.DEMO_CATALOG_ONLY -> LicenceMetadata(
        licenceName = "Metadata Reference Only",
        licenceUrl = null,
        sourceProviderName = provider.displayName,
        sourceWebpage = "local://demo-catalog",
        attributionText = "Editorial catalog reference for $reciterName (audio stream not configured)",
        downloadPermission = DownloadPermission.NOT_APPLICABLE,
        permissionConfirmed = false,
        usageNotes = "No playable audio URL configured for this reciter in open public CDNs."
    )
}

data class CuratedCollection(
    val id: String,
    val title: String,
    val description: String,
    val category: CollectionCategory,
    @param:DrawableRes val coverResId: Int,
    val trackIds: List<String>,
    val provenance: String,
    val associatedReciterId: String? = null
)

data class QuranVerse(
    val id: Int,
    val verseKey: String,
    val verseNumber: Int,
    val textUthmani: String,
    val translationText: String?,
    val translationAttribution: String?,
    val timestampFromMs: Long? = null,
    val timestampToMs: Long? = null
)

data class TranslationEdition(
    val id: Int,
    val name: String,
    val authorName: String,
    val languageCode: String,
    val languageName: String
)

data class AmbientSoundscape(
    val id: String,
    val nameKey: String,
    val defaultName: String,
    val subtitle: String,
    val category: SoundCategory = SoundCategory.ALL,
    @param:DrawableRes val artworkResId: Int = 0,
    val creator: String = "Tilawah Acoustic DSP",
    val licenceMetadata: LicenceMetadata = defaultQuranStreamLicence(
        AudioSourceProvider.ON_DEVICE_SYNTHESIS,
        "Tilawah Acoustic DSP"
    )
)

data class SpokenContentFilter(
    val id: String,
    val title: String,
    val subtitle: String
)

data class OfflineSavedTrackInfo(
    val trackId: String,
    val surahNumber: Int,
    val surahName: String,
    val reciterId: String,
    val reciterName: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val providerCode: String,
    val attributionText: String,
    val licenceName: String,
    val savedAtEpochMs: Long
)

data class DiagnosticLogEntry(
    val timestampEpochMs: Long,
    val source: String,
    val summary: String
)

data class CustomPlaylist(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String,
    val trackIds: List<String>,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

data class ListeningEventRecord(
    val id: String,
    val trackId: String,
    val surahNumber: Int,
    val surahName: String,
    val reciterId: String,
    val reciterName: String,
    val durationListenedSec: Int,
    val lastPositionMs: Long,
    val sessionId: String,
    val timestampEpochMs: Long
)

data class UserPreferencesState(
    val preferredLanguage: AppLanguage = AppLanguage.EN,
    val darkTheme: Boolean = true,
    val reducedMotion: Boolean = false,
    val arabicFontScale: Float = 1.0f,
    val autoplayNext: Boolean = true,
    val defaultRepeatMode: RepeatMode = RepeatMode.OFF,
    val ambientSoundId: String = "none",
    val ambientVolume: Float = 0.35f,
    val preferredAudioSourceCode: String = AudioSourceProvider.AL_QURAN_CLOUD.code,
    val favoriteReciterIds: Set<String> = emptySet(),
    val favoriteTrackIds: Set<String> = emptySet(),
    val savedCollectionIds: Set<String> = emptySet(),
    val isDemoPremium: Boolean = false
)

data class DailyListeningPoint(
    val dayLabel: String,
    val secondsListened: Int
)

data class ReciterStatItem(
    val reciterId: String,
    val reciterName: String,
    val totalSeconds: Int,
    val sessionCount: Int
)

data class ListeningInsightsSummary(
    val todaySeconds: Int = 0,
    val weekSeconds: Int = 0,
    val distinctSessions: Int = 0,
    val dailyTrend: List<DailyListeningPoint> = emptyList(),
    val frequentReciters: List<ReciterStatItem> = emptyList(),
    val recentEvents: List<ListeningEventRecord> = emptyList()
)
