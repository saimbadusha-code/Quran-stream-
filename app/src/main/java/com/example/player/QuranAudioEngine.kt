package com.example.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.local.OfflineAudioCacheManager
import com.example.data.model.AudioTrack
import com.example.data.model.DiagnosticLogEntry
import com.example.data.model.RepeatMode
import com.example.data.model.TrackAvailability
import com.example.data.remote.QuranCatalogRepository
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class QueueEntry(
    val queueItemId: String = UUID.randomUUID().toString(),
    val track: AudioTrack
)

data class GlobalPlayerState(
    val currentEntry: QueueEntry? = null,
    val queue: List<QueueEntry> = emptyList(),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 0.9f,
    val isMuted: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val autoplayNext: Boolean = true,
    val sleepTimerRemainingSec: Int? = null,
    val activeVerseKey: String? = null,
    val repeatVerseBoundsMs: Pair<Long, Long>? = null,
    val repeatVerseKey: String? = null,
    val verseTimings: Map<String, Pair<Long, Long>> = emptyMap(),
    val playbackError: String? = null,
    val streamSourceLabel: String? = null,
    val alternativeVerifiedTracks: List<AudioTrack> = emptyList(),
    val diagnosticEvents: List<DiagnosticLogEntry> = emptyList()
)

interface AudioBackendDriver {
    fun loadAndPlay(
        url: String,
        volume: Float,
        onPrepared: (durationMs: Long) -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    )
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setVolume(volume: Float)
    fun currentPositionMs(): Long
    fun durationMs(): Long
    fun isActuallyPlaying(): Boolean
    fun stopAndRelease()
}

class AndroidMediaPlayerDriver : AudioBackendDriver {
    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false

    override fun loadAndPlay(
        url: String,
        volume: Float,
        onPrepared: (durationMs: Long) -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        stopAndRelease()
        prepared = false
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(url)
                setVolume(volume, volume)
                setOnPreparedListener { player ->
                    prepared = true
                    val dur = player.duration.toLong().coerceAtLeast(0L)
                    player.start()
                    onPrepared(dur)
                }
                setOnCompletionListener {
                    if (prepared) {
                        onCompleted()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    prepared = false
                    onError("Audio stream error (code $what/$extra). Check network connection or try a verified alternative recording.")
                    true
                }
            }
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Failed to initialize audio stream.")
        }
    }

    override fun play() {
        try {
            if (prepared) mediaPlayer?.start()
        } catch (_: Exception) {
        }
    }

    override fun pause() {
        try {
            if (prepared && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {
        }
    }

    override fun seekTo(positionMs: Long) {
        try {
            if (prepared) {
                mediaPlayer?.seekTo(positionMs.coerceAtLeast(0L).toInt())
            }
        } catch (_: Exception) {
        }
    }

    override fun setVolume(volume: Float) {
        try {
            val v = volume.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(v, v)
        } catch (_: Exception) {
        }
    }

    override fun currentPositionMs(): Long {
        return try {
            if (prepared) mediaPlayer?.currentPosition?.toLong()?.coerceAtLeast(0L) ?: 0L else 0L
        } catch (_: Exception) {
            0L
        }
    }

    override fun durationMs(): Long {
        return try {
            if (prepared) mediaPlayer?.duration?.toLong()?.coerceAtLeast(0L) ?: 0L else 0L
        } catch (_: Exception) {
            0L
        }
    }

    override fun isActuallyPlaying(): Boolean {
        return try {
            prepared && mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }

    override fun stopAndRelease() {
        prepared = false
        val mp = mediaPlayer
        mediaPlayer = null
        if (mp != null) {
            try {
                mp.reset()
                mp.release()
            } catch (_: Exception) {
            }
        }
    }
}

class QuranAudioEngine(
    private val scope: CoroutineScope,
    private val catalogRepository: QuranCatalogRepository,
    private val driver: AudioBackendDriver = AndroidMediaPlayerDriver(),
    private val offlineCacheManager: OfflineAudioCacheManager? = null,
    private val onVerifiedListeningProgress: (track: AudioTrack, secondsPlayed: Int, lastPositionMs: Long, sessionId: String) -> Unit
) {
    private val _state = MutableStateFlow(GlobalPlayerState())
    val state: StateFlow<GlobalPlayerState> = _state.asStateFlow()

    private var originalUnshuffledQueue: List<QueueEntry> = emptyList()
    private var progressPollingJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var loadStreamJob: Job? = null

    private var activeListenAccumulatedSec: Int = 0
    private var currentSessionId: String = "sess_${UUID.randomUUID().toString().take(8)}"

    /**
     * Populates a paused standby track in the player queue on launch without starting network streaming
     * or audio playback, so the persistent Mini-Player is ready immediately on user tap.
     */
    fun seedStandbyTrack(track: AudioTrack) {
        if (_state.value.currentEntry != null || _state.value.queue.isNotEmpty()) return
        val entry = QueueEntry(track = track)
        originalUnshuffledQueue = listOf(entry)
        _state.update {
            it.copy(
                currentEntry = entry,
                queue = listOf(entry),
                isPlaying = false,
                isBuffering = false,
                positionMs = 0L,
                durationMs = 0L
            )
        }
    }

    /**
     * Starts playback of any AudioTrack (Surah, Verse, Ruqya, or Adhkar/Dua).
     * Automatically replaces any prior voice stream so spoken content and Quran recitation never overlap,
     * while AmbientSoundEngine runs independently on its separate acoustic channel.
     */
    fun playTrack(track: AudioTrack, newQueue: List<AudioTrack>? = null, shuffle: Boolean = false) {
        flushListeningSessionIfEligible()

        val baseTracks = newQueue?.ifEmpty { listOf(track) } ?: listOf(track)
        val entries = baseTracks.map { t -> QueueEntry(track = t) }
        originalUnshuffledQueue = entries

        val selectedEntry = entries.firstOrNull { it.track.id == track.id } ?: entries.first()
        val orderedQueue = if (shuffle && entries.size > 1) {
            val rest = entries.filterNot { it.queueItemId == selectedEntry.queueItemId }.shuffled()
            listOf(selectedEntry) + rest
        } else {
            entries
        }

        _state.update {
            it.copy(
                queue = orderedQueue,
                shuffleEnabled = if (newQueue != null) shuffle else it.shuffleEnabled
            )
        }
        startEntry(selectedEntry)
    }

    fun playOrToggleTrack(track: AudioTrack, newQueue: List<AudioTrack>? = null, shuffle: Boolean = false) {
        val current = _state.value
        if (current.currentEntry?.track?.id == track.id && current.playbackError == null && !current.isBuffering) {
            togglePlayPause()
        } else {
            playTrack(track, newQueue, shuffle)
        }
    }

    fun playQueueEntry(queueItemId: String) {
        val current = _state.value
        if (current.currentEntry?.queueItemId == queueItemId && current.playbackError == null && !current.isBuffering) {
            togglePlayPause()
            return
        }
        val target = current.queue.firstOrNull { it.queueItemId == queueItemId } ?: return
        flushListeningSessionIfEligible()
        startEntry(target)
    }

    fun togglePlayPause() {
        val current = _state.value
        val entry = current.currentEntry ?: return
        if (current.isBuffering) return

        if (current.isPlaying) {
            driver.pause()
            flushListeningSessionIfEligible()
            _state.update { it.copy(isPlaying = false) }
        } else {
            if (current.playbackError != null || current.durationMs == 0L) {
                startEntry(entry)
            } else {
                driver.play()
                _state.update { it.copy(isPlaying = true, playbackError = null) }
                ensureProgressLoop()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val dur = _state.value.durationMs
        val clamped = if (dur > 0L) positionMs.coerceIn(0L, dur) else positionMs.coerceAtLeast(0L)
        driver.seekTo(clamped)
        _state.update { st ->
            st.copy(
                positionMs = clamped,
                activeVerseKey = resolveActiveVerse(clamped, st.verseTimings)
            )
        }
    }

    fun seekToVerse(verseKey: String) {
        val bounds = _state.value.verseTimings[verseKey] ?: return
        seekTo(bounds.first)
    }

    fun toggleVerseRepeat(verseKey: String) {
        val current = _state.value
        if (current.repeatVerseKey == verseKey) {
            _state.update { it.copy(repeatVerseKey = null, repeatVerseBoundsMs = null) }
        } else {
            val bounds = current.verseTimings[verseKey]
            if (bounds != null) {
                _state.update { it.copy(repeatVerseKey = verseKey, repeatVerseBoundsMs = bounds) }
                seekTo(bounds.first)
            }
        }
    }

    fun skipNext(fromAutoCompletion: Boolean = false) {
        val st = _state.value
        val queue = st.queue
        val current = st.currentEntry ?: return
        if (queue.isEmpty()) return

        if (fromAutoCompletion && st.repeatMode == RepeatMode.ONE) {
            flushListeningSessionIfEligible()
            driver.seekTo(0L)
            driver.play()
            _state.update { it.copy(positionMs = 0L, isPlaying = true) }
            ensureProgressLoop()
            return
        }

        val currentIdx = queue.indexOfFirst { it.queueItemId == current.queueItemId }
        val nextIdx = currentIdx + 1
        if (nextIdx in queue.indices) {
            flushListeningSessionIfEligible()
            startEntry(queue[nextIdx])
        } else if (st.repeatMode == RepeatMode.ALL && queue.isNotEmpty()) {
            flushListeningSessionIfEligible()
            startEntry(queue.first())
        } else {
            flushListeningSessionIfEligible()
            driver.pause()
            driver.seekTo(0L)
            _state.update { it.copy(isPlaying = false, positionMs = 0L) }
        }
    }

    fun skipPrevious() {
        val st = _state.value
        if (st.positionMs > 4000L) {
            seekTo(0L)
            return
        }
        val queue = st.queue
        val current = st.currentEntry ?: return
        val currentIdx = queue.indexOfFirst { it.queueItemId == current.queueItemId }
        if (currentIdx > 0) {
            flushListeningSessionIfEligible()
            startEntry(queue[currentIdx - 1])
        } else if (st.repeatMode == RepeatMode.ALL && queue.size > 1) {
            flushListeningSessionIfEligible()
            startEntry(queue.last())
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val st = _state.value
        val newShuffle = !st.shuffleEnabled
        val current = st.currentEntry
        if (st.queue.size <= 1) {
            _state.update { it.copy(shuffleEnabled = newShuffle) }
            return
        }

        val updatedQueue = if (newShuffle) {
            originalUnshuffledQueue = st.queue
            if (current != null) {
                val others = st.queue.filterNot { it.queueItemId == current.queueItemId }.shuffled()
                listOf(current) + others
            } else {
                st.queue.shuffled()
            }
        } else {
            if (originalUnshuffledQueue.isNotEmpty()) {
                val currentIds = st.queue.map { it.queueItemId }.toSet()
                val restored = originalUnshuffledQueue.filter { it.queueItemId in currentIds }
                val newlyAdded = st.queue.filter { entry -> restored.none { it.queueItemId == entry.queueItemId } }
                restored + newlyAdded
            } else {
                st.queue
            }
        }
        _state.update { it.copy(shuffleEnabled = newShuffle, queue = updatedQueue) }
    }

    fun cycleRepeatMode() {
        _state.update { it.copy(repeatMode = it.repeatMode.next()) }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _state.update { it.copy(repeatMode = mode) }
    }

    fun setAutoplayNext(enabled: Boolean) {
        _state.update { it.copy(autoplayNext = enabled) }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val muted = clamped == 0f
        driver.setVolume(if (muted) 0f else clamped)
        _state.update { it.copy(volume = clamped, isMuted = muted) }
    }

    fun toggleMute() {
        val st = _state.value
        val nextMuted = !st.isMuted
        val effectiveVol = if (nextMuted) 0f else st.volume.coerceAtLeast(0.15f)
        driver.setVolume(effectiveVol)
        _state.update {
            it.copy(
                isMuted = nextMuted,
                volume = if (!nextMuted && it.volume == 0f) 0.5f else it.volume
            )
        }
    }

    fun addTrackToQueue(track: AudioTrack) {
        val entry = QueueEntry(track = track)
        originalUnshuffledQueue = originalUnshuffledQueue + entry
        _state.update { st -> st.copy(queue = st.queue + entry) }
        if (_state.value.currentEntry == null) {
            startEntry(entry)
        }
    }

    fun addTracksToQueue(tracks: List<AudioTrack>) {
        if (tracks.isEmpty()) return
        val entries = tracks.map { QueueEntry(track = it) }
        originalUnshuffledQueue = originalUnshuffledQueue + entries
        val hadCurrent = _state.value.currentEntry != null
        _state.update { it.copy(queue = it.queue + entries) }
        if (!hadCurrent) {
            startEntry(entries.first())
        }
    }

    fun removeQueueItem(queueItemId: String) {
        val st = _state.value
        val isRemovingCurrent = st.currentEntry?.queueItemId == queueItemId
        val remaining = st.queue.filterNot { it.queueItemId == queueItemId }
        originalUnshuffledQueue = originalUnshuffledQueue.filterNot { it.queueItemId == queueItemId }

        if (isRemovingCurrent) {
            flushListeningSessionIfEligible()
            if (remaining.isEmpty()) {
                driver.stopAndRelease()
                _state.update {
                    it.copy(
                        currentEntry = null,
                        queue = emptyList(),
                        isPlaying = false,
                        isBuffering = false,
                        positionMs = 0L,
                        durationMs = 0L,
                        alternativeVerifiedTracks = emptyList()
                    )
                }
            } else {
                _state.update { it.copy(queue = remaining) }
                startEntry(remaining.first())
            }
        } else {
            _state.update { it.copy(queue = remaining) }
        }
    }

    fun moveQueueItem(queueItemId: String, directionDelta: Int) {
        val list = _state.value.queue.toMutableList()
        val idx = list.indexOfFirst { it.queueItemId == queueItemId }
        if (idx == -1) return
        val targetIdx = (idx + directionDelta).coerceIn(0, list.lastIndex)
        if (idx == targetIdx) return
        val item = list.removeAt(idx)
        list.add(targetIdx, item)
        _state.update { it.copy(queue = list) }
    }

    fun clearQueuePreservingCurrent() {
        val current = _state.value.currentEntry
        val kept = if (current != null) listOf(current) else emptyList()
        originalUnshuffledQueue = kept
        _state.update { it.copy(queue = kept) }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = (minutes.coerceIn(1, 360)) * 60
        _state.update { it.copy(sleepTimerRemainingSec = totalSeconds) }
        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _state.update { it.copy(sleepTimerRemainingSec = remaining) }
            }
            if (remaining <= 0) {
                flushListeningSessionIfEligible()
                driver.pause()
                _state.update {
                    it.copy(
                        isPlaying = false,
                        sleepTimerRemainingSec = null
                    )
                }
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _state.update { it.copy(sleepTimerRemainingSec = null) }
    }

    fun retryCurrentTrack() {
        val current = _state.value.currentEntry ?: return
        startEntry(current)
    }

    fun release() {
        flushListeningSessionIfEligible()
        loadStreamJob?.cancel()
        progressPollingJob?.cancel()
        sleepTimerJob?.cancel()
        driver.stopAndRelease()
    }

    private fun appendDiagnostic(source: String, summary: String) {
        val item = DiagnosticLogEntry(
            timestampEpochMs = System.currentTimeMillis(),
            source = source,
            summary = summary
        )
        _state.update { st ->
            st.copy(diagnosticEvents = (listOf(item) + st.diagnosticEvents).take(20))
        }
    }

    private fun startEntry(entry: QueueEntry) {
        loadStreamJob?.cancel()
        progressPollingJob?.cancel()
        activeListenAccumulatedSec = 0
        currentSessionId = "sess_${UUID.randomUUID().toString().take(8)}"

        val initialSingleVerseKey = entry.track.specificVerseNumber?.let { "${entry.track.surahNumber}:$it" }
        _state.update {
            it.copy(
                currentEntry = entry,
                isBuffering = true,
                isPlaying = false,
                positionMs = 0L,
                durationMs = 0L,
                activeVerseKey = initialSingleVerseKey,
                repeatVerseKey = null,
                repeatVerseBoundsMs = null,
                verseTimings = emptyMap(),
                playbackError = null,
                streamSourceLabel = null,
                alternativeVerifiedTracks = emptyList()
            )
        }

        loadStreamJob = scope.launch {
            // 1. Check personal offline cache first
            val localFile = offlineCacheManager?.getLocalFileForTrack(entry.track.id)
            if (localFile != null) {
                val localSourceLabel = "Saved Offline Copy • ${entry.track.sourceProvider.shortBadge}"
                appendDiagnostic("OfflineCache", "Playing local file for ${entry.track.id}")
                val updatedEntry = entry.copy(
                    track = entry.track.copy(availabilityStatus = TrackAvailability.READY_TO_PLAY)
                )
                _state.update {
                    it.copy(
                        currentEntry = updatedEntry,
                        streamSourceLabel = localSourceLabel
                    )
                }
                playMediaDataSource(localFile.absolutePath, entry.track)
                return@launch
            }

            // 2. Resolve & verify live stream
            val resolved = catalogRepository.resolvePlayableStream(entry.track)
            appendDiagnostic(resolved.provider.shortBadge, resolved.diagnosticMessage)

            if (!resolved.isAvailable || resolved.audioUrl == null) {
                driver.stopAndRelease()
                _state.update {
                    it.copy(
                        isBuffering = false,
                        isPlaying = false,
                        playbackError = resolved.sourceNote,
                        streamSourceLabel = "Stream Unavailable",
                        alternativeVerifiedTracks = resolved.alternativeTracks
                    )
                }
                return@launch
            }

            val updatedEntry = entry.copy(
                track = entry.track.copy(
                    audioUrl = resolved.audioUrl,
                    sourceProvider = resolved.provider,
                    licenceMetadata = resolved.licenceMetadata,
                    availabilityStatus = TrackAvailability.READY_TO_PLAY
                )
            )
            _state.update {
                it.copy(
                    currentEntry = updatedEntry,
                    verseTimings = resolved.timestamps,
                    streamSourceLabel = resolved.sourceNote,
                    alternativeVerifiedTracks = emptyList()
                )
            }

            playMediaDataSource(resolved.audioUrl, updatedEntry.track)
        }
    }

    private fun playMediaDataSource(dataSourceUrlOrPath: String, activeTrack: AudioTrack) {
        val effectiveVol = if (_state.value.isMuted) 0f else _state.value.volume
        driver.loadAndPlay(
            url = dataSourceUrlOrPath,
            volume = effectiveVol,
            onPrepared = { durMs ->
                _state.update {
                    it.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMs = durMs,
                        playbackError = null,
                        alternativeVerifiedTracks = emptyList()
                    )
                }
                ensureProgressLoop()
            },
            onCompleted = {
                if (_state.value.autoplayNext || _state.value.repeatMode != RepeatMode.OFF) {
                    skipNext(fromAutoCompletion = true)
                } else {
                    flushListeningSessionIfEligible()
                    _state.update { it.copy(isPlaying = false, positionMs = 0L) }
                }
            },
            onError = { errMsg ->
                flushListeningSessionIfEligible()
                appendDiagnostic(activeTrack.sourceProvider.shortBadge, errMsg)
                val alts = catalogRepository.getVerifiedAlternativesForSurah(
                    activeTrack.surahNumber,
                    activeTrack.reciterId
                )
                _state.update {
                    it.copy(
                        isBuffering = false,
                        isPlaying = false,
                        playbackError = errMsg,
                        alternativeVerifiedTracks = alts
                    )
                }
            }
        )
    }

    private fun ensureProgressLoop() {
        progressPollingJob?.cancel()
        progressPollingJob = scope.launch {
            var tickCount = 0
            while (isActive && _state.value.isPlaying) {
                delay(500L)
                val pos = driver.currentPositionMs()
                val dur = driver.durationMs().let { if (it > 0L) it else _state.value.durationMs }
                val st = _state.value

                val verseBounds = st.repeatVerseBoundsMs
                if (verseBounds != null && pos >= verseBounds.second && verseBounds.second > verseBounds.first) {
                    driver.seekTo(verseBounds.first)
                }

                tickCount++
                if (tickCount % 2 == 0 && driver.isActuallyPlaying()) {
                    activeListenAccumulatedSec++
                }

                _state.update {
                    it.copy(
                        positionMs = pos,
                        durationMs = dur,
                        activeVerseKey = resolveActiveVerse(pos, it.verseTimings, it.currentEntry?.track)
                    )
                }
            }
        }
    }

    private fun flushListeningSessionIfEligible() {
        val currentTrack = _state.value.currentEntry?.track ?: return
        val secs = activeListenAccumulatedSec
        val lastPos = _state.value.positionMs
        if (secs >= 5) {
            activeListenAccumulatedSec = 0
            onVerifiedListeningProgress(currentTrack, secs, lastPos, currentSessionId)
        } else {
            activeListenAccumulatedSec = 0
        }
    }

    private fun resolveActiveVerse(
        positionMs: Long,
        timings: Map<String, Pair<Long, Long>>,
        activeTrack: AudioTrack? = null
    ): String? {
        if (timings.isEmpty()) {
            return activeTrack?.specificVerseNumber?.let { "${activeTrack.surahNumber}:$it" }
        }
        return timings.entries.firstOrNull { (_, range) ->
            positionMs >= range.first && positionMs <= range.second
        }?.key ?: activeTrack?.specificVerseNumber?.let { "${activeTrack.surahNumber}:$it" }
    }
}
