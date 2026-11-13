package com.example.data.provider

import com.example.data.model.AmbientSoundscape
import com.example.data.model.AudioCategory
import com.example.data.model.AudioEditionDescriptor
import com.example.data.model.AudioSourceProvider
import com.example.data.model.AudioTrack
import com.example.data.model.LicenceMetadata
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.model.TrackAvailability
import com.example.data.model.defaultQuranStreamLicence
import com.example.data.remote.AuthenticatedSeedCatalog
import com.example.data.remote.QuranCatalogRepository
import com.example.data.remote.StreamProbeClient
import java.util.Locale

data class TrackValidationResult(
    val isPlayable: Boolean,
    val validatedUrl: String?,
    val httpStatusCode: Int,
    val provider: AudioSourceProvider,
    val diagnosticSummary: String
)

/**
 * Clean, provider-agnostic audio abstraction layer separating verified source resolution,
 * edition discovery, verse/surah URL construction, background soundscapes, spoken Adhkar/Ruqya
 * sessions, HTTP stream validation, and licence metadata from UI playback controls.
 */
interface AudioProvider {
    fun getReciters(): List<Reciter>
    fun getSurahs(): List<Surah>
    fun getAvailableEditions(): List<AudioEditionDescriptor>
    fun getSurahTracks(surahNumber: Int, reciterId: String): List<AudioTrack>
    fun getVerseAudio(surahNumber: Int, verseNumber: Int, reciterId: String): AudioTrack?
    fun getBackgroundSounds(): List<AmbientSoundscape>
    fun getBackgroundVoiceTracks(): List<AudioTrack>
    suspend fun validateTrack(track: AudioTrack): TrackValidationResult
    fun getTrackSource(track: AudioTrack): AudioSourceProvider
    fun getLicenceMetadata(track: AudioTrack): LicenceMetadata
}

class TilawahAudioProvider(
    private val catalogRepository: QuranCatalogRepository,
    private val streamProbeClient: StreamProbeClient = catalogRepository.probeClient
) : AudioProvider {

    override fun getReciters(): List<Reciter> = catalogRepository.getInitialReciters()

    override fun getSurahs(): List<Surah> = catalogRepository.getInitialSurahs()

    override fun getAvailableEditions(): List<AudioEditionDescriptor> {
        val reciters = getReciters()
        val list = mutableListOf<AudioEditionDescriptor>()

        reciters.forEach { reciter ->
            val aqcEdition = reciter.alQuranCloudEdition
            val aqcBitrate = reciter.alQuranCloudBitrate
            if (!aqcEdition.isNullOrBlank() && aqcBitrate != null) {
                list.add(
                    AudioEditionDescriptor(
                        editionId = aqcEdition,
                        reciterId = reciter.id,
                        displayName = "${reciter.displayName} ($aqcEdition • ${aqcBitrate}kbps)",
                        provider = AudioSourceProvider.AL_QURAN_CLOUD,
                        bitrateKbps = aqcBitrate,
                        supportsFullSurah = true,
                        supportsVerseByVerse = true,
                        style = reciter.recitationStyle ?: "Murattal"
                    )
                )
            }

            val eaFolder = reciter.everyAyahFolder
            val eaBitrate = reciter.everyAyahBitrate
            if (!eaFolder.isNullOrBlank() && eaBitrate != null) {
                list.add(
                    AudioEditionDescriptor(
                        editionId = eaFolder,
                        reciterId = reciter.id,
                        displayName = "${reciter.displayName} ($eaFolder • ${eaBitrate}kbps)",
                        provider = AudioSourceProvider.EVERY_AYAH,
                        bitrateKbps = eaBitrate,
                        supportsFullSurah = false,
                        supportsVerseByVerse = true,
                        style = reciter.recitationStyle ?: "Murattal"
                    )
                )
            }

            if (reciter.chapterReciterId != null || AuthenticatedSeedCatalog.hasVerifiedQuranicAudioSurahStream(reciter.id)) {
                list.add(
                    AudioEditionDescriptor(
                        editionId = "qdc_${reciter.chapterReciterId ?: reciter.id}",
                        reciterId = reciter.id,
                        displayName = "${reciter.displayName} (Quran.com / QuranicAudio)",
                        provider = AudioSourceProvider.QURAN_FOUNDATION_API,
                        bitrateKbps = 128,
                        supportsFullSurah = true,
                        supportsVerseByVerse = false,
                        style = reciter.recitationStyle ?: "Murattal"
                    )
                )
            }
        }
        return list
    }

    override fun getSurahTracks(surahNumber: Int, reciterId: String): List<AudioTrack> {
        if (surahNumber !in 1..114) return emptyList()
        val reciter = getReciters().firstOrNull { it.id == reciterId } ?: return emptyList()
        val surah = getSurahs().firstOrNull { it.number == surahNumber } ?: return emptyList()
        val candidates = mutableListOf<AudioTrack>()

        // Source A: Al Quran Cloud complete surah recording (validated edition & bitrate)
        val aqcEdition = reciter.alQuranCloudEdition
        val aqcBitrate = reciter.alQuranCloudBitrate
        if (!aqcEdition.isNullOrBlank() && aqcBitrate != null) {
            val url = buildAlQuranCloudSurahUrl(aqcBitrate, aqcEdition, surah.number)
            candidates.add(
                AudioTrack(
                    id = "${reciter.id}_${surah.number}",
                    surahNumber = surah.number,
                    surahName = surah.displayName,
                    surahArabicName = surah.arabicName,
                    surahMeaning = surah.englishMeaning,
                    verseCount = surah.verseCount,
                    reciterId = reciter.id,
                    reciterName = reciter.displayName,
                    chapterReciterId = reciter.chapterReciterId,
                    audioUrl = url,
                    durationSec = null,
                    sourceMetadata = "Al Quran Cloud ($aqcEdition • ${aqcBitrate}kbps)",
                    availabilityStatus = TrackAvailability.VERIFIED_STREAM,
                    audioCategory = AudioCategory.QURAN_SURAH,
                    sourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
                    bitrateKbps = aqcBitrate,
                    licenceMetadata = defaultQuranStreamLicence(
                        AudioSourceProvider.AL_QURAN_CLOUD,
                        reciter.displayName
                    )
                )
            )
        }

        // Source C: Quran.com API v4 / QuranicAudio complete surah recording
        val qdcUrl = AuthenticatedSeedCatalog.buildQuranicAudioDirectUrl(reciter.id, surah.number)
        if (qdcUrl != null || reciter.chapterReciterId != null) {
            candidates.add(
                AudioTrack(
                    id = "${reciter.id}_${surah.number}",
                    surahNumber = surah.number,
                    surahName = surah.displayName,
                    surahArabicName = surah.arabicName,
                    surahMeaning = surah.englishMeaning,
                    verseCount = surah.verseCount,
                    reciterId = reciter.id,
                    reciterName = reciter.displayName,
                    chapterReciterId = reciter.chapterReciterId,
                    audioUrl = qdcUrl,
                    durationSec = null,
                    sourceMetadata = "Quran.com API v4 / QuranicAudio",
                    availabilityStatus = TrackAvailability.VERIFIED_STREAM,
                    audioCategory = AudioCategory.QURAN_SURAH,
                    sourceProvider = AudioSourceProvider.QURAN_FOUNDATION_API,
                    bitrateKbps = 128,
                    licenceMetadata = defaultQuranStreamLicence(
                        AudioSourceProvider.QURAN_FOUNDATION_API,
                        reciter.displayName
                    )
                )
            )
        }

        return candidates
    }

    override fun getVerseAudio(
        surahNumber: Int,
        verseNumber: Int,
        reciterId: String
    ): AudioTrack? {
        val surah = getSurahs().firstOrNull { it.number == surahNumber } ?: return null
        if (verseNumber !in 1..surah.verseCount) return null
        val reciter = getReciters().firstOrNull { it.id == reciterId }
            ?: getReciters().firstOrNull { it.id == "mishary_alafasy" }
            ?: return null

        val preferred = catalogRepository.preferredAudioSource

        // Option 1: EveryAyah verse-level MP3 (SSSAAA.mp3) if preferred or if reciter is EveryAyah-only (e.g., Yasser Al-Dosari, Saad Al-Ghamdi)
        val eaFolder = reciter.everyAyahFolder
        val eaBitrate = reciter.everyAyahBitrate ?: 128
        if (!eaFolder.isNullOrBlank() && (preferred == AudioSourceProvider.EVERY_AYAH || reciter.alQuranCloudEdition.isNullOrBlank())) {
            val fileCode = String.format(Locale.US, "%03d%03d.mp3", surahNumber, verseNumber)
            val url = "https://everyayah.com/data/$eaFolder/$fileCode"
            return AudioTrack(
                id = "verse_${reciter.id}_${surahNumber}_$verseNumber",
                surahNumber = surah.number,
                surahName = "${surah.displayName} ($surahNumber:$verseNumber)",
                surahArabicName = "${surah.arabicName} (${verseNumber})",
                surahMeaning = "Verse $verseNumber of ${surah.verseCount} • ${surah.englishMeaning}",
                verseCount = 1,
                reciterId = reciter.id,
                reciterName = reciter.displayName,
                chapterReciterId = reciter.chapterReciterId,
                audioUrl = url,
                durationSec = null,
                sourceMetadata = "EveryAyah ($eaFolder/$fileCode)",
                availabilityStatus = TrackAvailability.VERIFIED_STREAM,
                audioCategory = AudioCategory.QURAN_VERSE,
                specificVerseNumber = verseNumber,
                sourceProvider = AudioSourceProvider.EVERY_AYAH,
                bitrateKbps = eaBitrate,
                licenceMetadata = defaultQuranStreamLicence(
                    AudioSourceProvider.EVERY_AYAH,
                    reciter.displayName
                )
            )
        }

        // Option 2: Al Quran Cloud global-ayah MP3 (/quran/audio/{bitrate}/{edition}/{globalAyahNumber}.mp3)
        val globalAyah = computeGlobalAyahNumber(surahNumber, verseNumber)
        val aqcEdition = reciter.alQuranCloudEdition
        val aqcBitrate = reciter.alQuranCloudBitrate ?: 128
        if (globalAyah != null && !aqcEdition.isNullOrBlank()) {
            val url = buildAlQuranCloudVerseUrl(aqcBitrate, aqcEdition, globalAyah)
            return AudioTrack(
                id = "verse_${reciter.id}_${surahNumber}_$verseNumber",
                surahNumber = surah.number,
                surahName = "${surah.displayName} ($surahNumber:$verseNumber)",
                surahArabicName = "${surah.arabicName} (${verseNumber})",
                surahMeaning = "Verse $verseNumber (Global Ayah #$globalAyah)",
                verseCount = 1,
                reciterId = reciter.id,
                reciterName = reciter.displayName,
                chapterReciterId = reciter.chapterReciterId,
                audioUrl = url,
                durationSec = null,
                sourceMetadata = "Al Quran Cloud ($aqcEdition • Ayah #$globalAyah)",
                availabilityStatus = TrackAvailability.VERIFIED_STREAM,
                audioCategory = AudioCategory.QURAN_VERSE,
                specificVerseNumber = verseNumber,
                sourceProvider = AudioSourceProvider.AL_QURAN_CLOUD,
                bitrateKbps = aqcBitrate,
                licenceMetadata = defaultQuranStreamLicence(
                    AudioSourceProvider.AL_QURAN_CLOUD,
                    reciter.displayName
                )
            )
        }

        // Fallback to EveryAyah if available
        if (!eaFolder.isNullOrBlank()) {
            val fileCode = String.format(Locale.US, "%03d%03d.mp3", surahNumber, verseNumber)
            val url = "https://everyayah.com/data/$eaFolder/$fileCode"
            return AudioTrack(
                id = "verse_${reciter.id}_${surahNumber}_$verseNumber",
                surahNumber = surah.number,
                surahName = "${surah.displayName} ($surahNumber:$verseNumber)",
                surahArabicName = "${surah.arabicName} (${verseNumber})",
                surahMeaning = "Verse $verseNumber of ${surah.verseCount}",
                verseCount = 1,
                reciterId = reciter.id,
                reciterName = reciter.displayName,
                chapterReciterId = reciter.chapterReciterId,
                audioUrl = url,
                durationSec = null,
                sourceMetadata = "EveryAyah ($eaFolder/$fileCode)",
                availabilityStatus = TrackAvailability.VERIFIED_STREAM,
                audioCategory = AudioCategory.QURAN_VERSE,
                specificVerseNumber = verseNumber,
                sourceProvider = AudioSourceProvider.EVERY_AYAH,
                bitrateKbps = eaBitrate,
                licenceMetadata = defaultQuranStreamLicence(
                    AudioSourceProvider.EVERY_AYAH,
                    reciter.displayName
                )
            )
        }

        return null
    }

    override fun getBackgroundSounds(): List<AmbientSoundscape> =
        AuthenticatedSeedCatalog.ambientSoundscapes.filter { it.id != "none" }

    override fun getBackgroundVoiceTracks(): List<AudioTrack> =
        AuthenticatedSeedCatalog.spokenIslamicSessions

    override suspend fun validateTrack(track: AudioTrack): TrackValidationResult {
        val url = track.audioUrl
        if (url.isNullOrBlank()) {
            return TrackValidationResult(
                isPlayable = false,
                validatedUrl = null,
                httpStatusCode = 0,
                provider = track.sourceProvider,
                diagnosticSummary = "No audio stream URL configured for ${track.reciterName} (${track.surahName})"
            )
        }
        val probe = streamProbeClient.verifyAudioUrl(url)
        return TrackValidationResult(
            isPlayable = probe.isReachable,
            validatedUrl = if (probe.isReachable) url else null,
            httpStatusCode = probe.httpCode,
            provider = track.sourceProvider,
            diagnosticSummary = "${track.sourceProvider.shortBadge}: ${probe.diagnosticMessage}"
        )
    }

    override fun getTrackSource(track: AudioTrack): AudioSourceProvider = track.sourceProvider

    override fun getLicenceMetadata(track: AudioTrack): LicenceMetadata = track.licenceMetadata

    companion object {
        fun buildAlQuranCloudSurahUrl(bitrate: Int, edition: String, surahNumber: Int): String =
            "https://cdn.islamic.network/quran/audio-surah/$bitrate/$edition/$surahNumber.mp3"

        fun buildAlQuranCloudVerseUrl(bitrate: Int, edition: String, globalAyahNumber: Int): String =
            "https://cdn.islamic.network/quran/audio/$bitrate/$edition/$globalAyahNumber.mp3"

        /**
         * Computes canonical 1-indexed global ayah number (1..6236) across all 114 surahs.
         * Example: Surah Al-Fatihah 1:1 -> 1; Ayat Al-Kursi 2:255 -> 262; An-Nas 114:6 -> 6236.
         */
        fun computeGlobalAyahNumber(surahNumber: Int, verseNumber: Int): Int? {
            val surahs = AuthenticatedSeedCatalog.canonicalSurahs
            val target = surahs.firstOrNull { it.number == surahNumber } ?: return null
            if (verseNumber !in 1..target.verseCount) return null
            val precedingVerses = surahs
                .filter { it.number < surahNumber }
                .sumOf { it.verseCount }
            return precedingVerses + verseNumber
        }
    }
}
