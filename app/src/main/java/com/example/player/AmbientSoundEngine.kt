package com.example.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AmbientUiState(
    val activeSoundId: String = "none",
    val selectedTrackId: String = "painted_dream",
    val isEnabled: Boolean = false,
    val previewingSoundId: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val volume: Float = 0.35f,
    val errorMessage: String? = null
)

class AmbientSoundEngine(
    private val enableAudioHardware: Boolean = true
) {
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var synthJob: Job? = null
    private var previewTimerJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private val _state = MutableStateFlow(AmbientUiState())
    val state: StateFlow<AmbientUiState> = _state.asStateFlow()

    fun selectSoundscape(soundId: String) {
        previewTimerJob?.cancel()
        previewTimerJob = null
        if (soundId == "none") {
            stopAll()
            return
        }
        val currentVol = _state.value.volume
        _state.value = AmbientUiState(
            activeSoundId = soundId,
            selectedTrackId = soundId,
            isEnabled = true,
            previewingSoundId = null,
            isPlaying = true,
            isLoading = enableAudioHardware,
            volume = currentVol,
            errorMessage = null
        )
        if (enableAudioHardware) {
            startSynthesisLoop(soundId)
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectBackgroundTrack(soundId: String) {
        if (soundId == "none") return
        val current = _state.value
        if (current.isPlaying && current.previewingSoundId == null) {
            selectSoundscape(soundId)
        } else {
            _state.update { it.copy(selectedTrackId = soundId, errorMessage = null) }
        }
    }

    fun setEnabled(enabled: Boolean, autoPlayNow: Boolean = true) {
        if (enabled) {
            val targetId = _state.value.selectedTrackId.takeIf { it != "none" && it.isNotBlank() } ?: "painted_dream"
            if (autoPlayNow) {
                selectSoundscape(targetId)
            } else {
                _state.update { it.copy(isEnabled = true, selectedTrackId = targetId, errorMessage = null) }
            }
        } else {
            previewTimerJob?.cancel()
            previewTimerJob = null
            synthJob?.cancel()
            synthJob = null
            releaseTrack()
            _state.update {
                it.copy(
                    activeSoundId = "none",
                    isEnabled = false,
                    previewingSoundId = null,
                    isPlaying = false,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun pausePlayback() {
        previewTimerJob?.cancel()
        previewTimerJob = null
        synthJob?.cancel()
        synthJob = null
        releaseTrack()
        _state.update {
            it.copy(
                activeSoundId = "none",
                previewingSoundId = null,
                isPlaying = false,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun togglePlayPause() {
        val current = _state.value
        if (current.isPlaying) {
            pausePlayback()
        } else {
            val targetId = current.selectedTrackId.takeIf { it != "none" && it.isNotBlank() } ?: "painted_dream"
            selectSoundscape(targetId)
        }
    }

    fun toggleSoundscape(soundId: String): String {
        val current = _state.value
        return if (current.activeSoundId == soundId && current.isPlaying && current.previewingSoundId == null) {
            stopAll()
            "none"
        } else {
            selectSoundscape(soundId)
            soundId
        }
    }

    fun previewSoundscape(soundId: String, durationMs: Long = 5000L) {
        if (soundId == "none") return
        previewTimerJob?.cancel()
        val previousActive = _state.value.activeSoundId.takeIf {
            it != "none" && _state.value.previewingSoundId == null && _state.value.isPlaying
        }
        val currentVol = _state.value.volume
        val keptSelected = _state.value.selectedTrackId
        val keptEnabled = _state.value.isEnabled

        _state.value = AmbientUiState(
            activeSoundId = previousActive ?: "none",
            selectedTrackId = keptSelected,
            isEnabled = keptEnabled,
            previewingSoundId = soundId,
            isPlaying = true,
            isLoading = enableAudioHardware,
            volume = currentVol,
            errorMessage = null
        )

        if (enableAudioHardware) {
            startSynthesisLoop(soundId)
        } else {
            _state.update { it.copy(isLoading = false) }
        }

        previewTimerJob = engineScope.launch {
            delay(durationMs)
            if (_state.value.previewingSoundId == soundId) {
                if (previousActive != null) {
                    selectSoundscape(previousActive)
                } else {
                    pausePlayback()
                }
            }
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _state.update { it.copy(volume = clamped) }
        try {
            audioTrack?.setVolume(clamped * 0.45f)
        } catch (_: Exception) {
        }
    }

    fun stopAll() {
        previewTimerJob?.cancel()
        previewTimerJob = null
        synthJob?.cancel()
        synthJob = null
        releaseTrack()
        _state.update {
            it.copy(
                activeSoundId = "none",
                isEnabled = false,
                previewingSoundId = null,
                isPlaying = false,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun release() {
        stopAll()
        engineScope.cancel()
    }

    private fun releaseTrack() {
        val current = audioTrack
        audioTrack = null
        if (current != null) {
            try {
                current.pause()
                current.flush()
                current.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun startSynthesisLoop(targetSoundId: String) {
        synthJob?.cancel()
        releaseTrack()

        synthJob = engineScope.launch {
            val sampleRate = 22050
            val bufferFrames = 2048
            val track = try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(bufferFrames * 2)

                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "Audio output device unavailable (${t.javaClass.simpleName})."
                    )
                }
                null
            } ?: return@launch

            audioTrack = track
            try {
                track.setVolume(_state.value.volume * 0.45f)
                track.play()
                _state.update { it.copy(isLoading = false, errorMessage = null) }
            } catch (t: Throwable) {
                releaseTrack()
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "Unable to start background sound (${t.javaClass.simpleName})."
                    )
                }
                return@launch
            }

            val pcm = ShortArray(bufferFrames)
            var b0 = 0.0
            var b1 = 0.0
            var b2 = 0.0
            var lp = 0.0
            var lpSlow = 0.0
            var dropletEnv = 0.0
            var phase = 0.0
            var secondaryPhase = 0.0
            val rng = Random(42L)

            try {
                while (
                    isActive &&
                    _state.value.isPlaying &&
                    (_state.value.previewingSoundId ?: _state.value.activeSoundId) == targetSoundId
                ) {
                    for (i in 0 until bufferFrames) {
                        val white = rng.nextDouble(-1.0, 1.0)
                        // Paul Kellet pink-noise approximation (non-musical natural spectrum)
                        b0 = 0.99765 * b0 + white * 0.0990460
                        b1 = 0.96300 * b1 + white * 0.2965164
                        b2 = 0.57000 * b2 + white * 1.0526913
                        val pink = (b0 + b1 + b2 + white * 0.1848) * 0.16

                        val sample: Double = when (targetSoundId) {
                            "painted_dream" -> {
                                phase += (2.0 * PI * 0.075) / sampleRate
                                secondaryPhase += (2.0 * PI * 110.0) / sampleRate
                                if (phase > 2.0 * PI) phase -= 2.0 * PI
                                if (secondaryPhase > 2.0 * PI) secondaryPhase -= 2.0 * PI
                                val breath = 0.45 + 0.55 * (0.5 + 0.5 * sin(phase))
                                val softPad = (sin(secondaryPhase) * 0.16 + sin(secondaryPhase * 1.5) * 0.09) * breath
                                lp = lp * 0.94 + pink * 0.06
                                (softPad + lp * 0.55)
                            }

                            "rain" -> {
                                if (rng.nextDouble() < 0.0014) {
                                    dropletEnv = rng.nextDouble(0.18, 0.42)
                                }
                                dropletEnv *= 0.985
                                lp = lp * 0.78 + pink * 0.22
                                (lp * 0.8 + white * dropletEnv * 0.35)
                            }

                            "rain_window" -> {
                                if (rng.nextDouble() < 0.0022) {
                                    dropletEnv = rng.nextDouble(0.22, 0.48)
                                }
                                dropletEnv *= 0.972
                                lp = lp * 0.86 + pink * 0.14
                                lpSlow = lpSlow * 0.60 + white * 0.40
                                (lp * 0.68 + lpSlow * dropletEnv * 0.42)
                            }

                            "stream" -> {
                                phase += (2.0 * PI * 0.85) / sampleRate
                                if (phase > 2.0 * PI) phase -= 2.0 * PI
                                val ripple = 0.75 + 0.25 * sin(phase)
                                lp = lp * 0.86 + pink * 0.14
                                lp * ripple * 1.35
                            }

                            "ocean" -> {
                                phase += (2.0 * PI * 0.09) / sampleRate
                                if (phase > 2.0 * PI) phase -= 2.0 * PI
                                val swell = 0.32 + 0.68 * (0.5 + 0.5 * sin(phase))
                                val cutoff = 0.03 + 0.11 * swell
                                lp = lp * (1.0 - cutoff) + pink * cutoff
                                lp * (0.85 + 0.95 * swell)
                            }

                            "wind" -> {
                                phase += (2.0 * PI * 0.14) / sampleRate
                                if (phase > 2.0 * PI) phase -= 2.0 * PI
                                val alpha = 0.025 + 0.02 * (0.5 + 0.5 * sin(phase))
                                lp = lp * (1.0 - alpha) + pink * alpha
                                lp * 2.4
                            }

                            "forest" -> {
                                phase += (2.0 * PI * 0.22) / sampleRate
                                secondaryPhase += (2.0 * PI * 0.61) / sampleRate
                                if (phase > 2.0 * PI) phase -= 2.0 * PI
                                if (secondaryPhase > 2.0 * PI) secondaryPhase -= 2.0 * PI
                                val rustle = 0.55 + 0.28 * sin(phase) + 0.17 * sin(secondaryPhase)
                                lp = lp * 0.82 + pink * 0.18
                                lp * rustle * 1.25
                            }

                            else -> {
                                // Still Night Ambience (warm low-passed brownian stillness)
                                lp = (lp + white * 0.020).coerceIn(-0.6, 0.6) * 0.996
                                lp * 0.85
                            }
                        }

                        val scaled = (sample.coerceIn(-0.95, 0.95) * Short.MAX_VALUE * 0.55).toInt()
                        pcm[i] = scaled.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(pcm, 0, bufferFrames)
                }
            } catch (_: Throwable) {
                // Clean exit when track stops
            }
        }
    }
}
