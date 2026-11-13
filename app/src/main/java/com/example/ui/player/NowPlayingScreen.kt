package com.example.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.data.model.RepeatMode
import com.example.data.model.TranslationEdition
import com.example.data.remote.AuthenticatedSeedCatalog
import com.example.i18n.UiStrings
import com.example.player.AmbientUiState
import com.example.player.GlobalPlayerState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ReciterAvatar
import com.example.ui.navigation.formatDurationMs
import com.example.ui.navigation.formatDurationSeconds
import com.example.ui.theme.QuranArabicFontFamily
import com.example.viewmodel.ScripturePanelState

@Composable
fun NowPlayingFullScreenOverlay(
    playerState: GlobalPlayerState,
    scriptureState: ScripturePanelState,
    availableTranslations: List<TranslationEdition>,
    arabicFontScale: Float,
    isFavorite: Boolean,
    strings: UiStrings,
    onMinimize: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekToVerse: (String) -> Unit,
    onToggleVerseRepeat: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectTranslation: (TranslationEdition) -> Unit,
    onToggleCompactReading: () -> Unit,
    onRetryScripture: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAmbience: () -> Unit,
    ambientState: AmbientUiState? = null,
    onRetryPlayback: () -> Unit = {},
    onPlayAlternativeTrack: (AudioTrack) -> Unit = {},
    onPlayVerseAudio: (Int, Int) -> Unit = { _, _ -> },
    onShowTrackLicence: (AudioTrack) -> Unit = {},
    initialOpenReadSheet: Boolean = false,
    onToggleBgMusicPlayPause: () -> Unit = {},
    onSetBgMusicEnabled: (Boolean) -> Unit = {},
    onSelectBgMusicTrack: (String) -> Unit = {},
    onUpdateBgMusicVolume: (Float) -> Unit = {},
    onStopBgMusic: () -> Unit = {}
) {
    BackHandler(onBack = onMinimize)

    val track = playerState.currentEntry?.track
    val activeAmbienceLabel = remember(ambientState?.activeSoundId, ambientState?.isPlaying) {
        if (ambientState?.isPlaying == true && ambientState.activeSoundId != "none") {
            AuthenticatedSeedCatalog.ambientSoundscapes
                .firstOrNull { it.id == ambientState.activeSoundId }
                ?.defaultName
        } else null
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("now_playing_fullscreen"),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val isWide = maxWidth >= 760.dp
            if (isWide) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Top Utility Bar (Desktop / Wide Tablet)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("minimize_now_playing_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = "Minimize Now Playing",
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = track?.let { "${it.surahNumber}. ${it.surahName}" } ?: strings.nowPlaying,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track?.reciterName ?: playerState.streamSourceLabel.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (track != null) {
                                IconButton(
                                    onClick = onToggleFavorite,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("now_playing_favorite_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = onOpenAmbience, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Spa,
                                    contentDescription = strings.ambientSounds,
                                    tint = if (activeAmbienceLabel != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onOpenSleepTimer, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Bedtime,
                                    contentDescription = strings.sleepTimer,
                                    tint = if (playerState.sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onOpenQueue, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = strings.queueTitle,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.39f)) {
                            PlayerTransportColumn(
                                playerState = playerState,
                                activeAmbienceLabel = activeAmbienceLabel,
                                isFavorite = isFavorite,
                                showInlineTransport = true,
                                onTogglePlayPause = onTogglePlayPause,
                                onPrevious = onPrevious,
                                onNext = onNext,
                                onToggleShuffle = onToggleShuffle,
                                onCycleRepeat = onCycleRepeat,
                                onSeekTo = onSeekTo,
                                onToggleFavorite = onToggleFavorite,
                                onOpenAmbience = onOpenAmbience,
                                onOpenSleepTimer = onOpenSleepTimer,
                                onRetryPlayback = onRetryPlayback,
                                onPlayAlternativeTrack = onPlayAlternativeTrack,
                                onShowTrackLicence = onShowTrackLicence
                            )
                        }
                        Box(modifier = Modifier.weight(0.61f)) {
                            ScriptureReadingPanel(
                                playerState = playerState,
                                scriptureState = scriptureState,
                                availableTranslations = availableTranslations,
                                arabicFontScale = arabicFontScale,
                                strings = strings,
                                onSelectTranslation = onSelectTranslation,
                                onToggleCompactReading = onToggleCompactReading,
                                onSeekToVerse = onSeekToVerse,
                                onToggleVerseRepeat = onToggleVerseRepeat,
                                onPlayVerseAudio = onPlayVerseAudio,
                                onRetryScripture = onRetryScripture
                            )
                        }
                    }
                }
            } else {
                MobileNowPlayingContent(
                    playerState = playerState,
                    scriptureState = scriptureState,
                    availableTranslations = availableTranslations,
                    arabicFontScale = arabicFontScale,
                    isFavorite = isFavorite,
                    strings = strings,
                    ambientState = ambientState,
                    initialOpenReadSheet = initialOpenReadSheet,
                    onMinimize = onMinimize,
                    onTogglePlayPause = onTogglePlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onToggleShuffle = onToggleShuffle,
                    onCycleRepeat = onCycleRepeat,
                    onSeekTo = onSeekTo,
                    onSeekToVerse = onSeekToVerse,
                    onToggleVerseRepeat = onToggleVerseRepeat,
                    onToggleFavorite = onToggleFavorite,
                    onSelectTranslation = onSelectTranslation,
                    onToggleCompactReading = onToggleCompactReading,
                    onRetryScripture = onRetryScripture,
                    onOpenQueue = onOpenQueue,
                    onOpenSleepTimer = onOpenSleepTimer,
                    onOpenAmbience = onOpenAmbience,
                    onToggleBgMusicPlayPause = onToggleBgMusicPlayPause,
                    onSetBgMusicEnabled = onSetBgMusicEnabled,
                    onSelectBgMusicTrack = onSelectBgMusicTrack,
                    onUpdateBgMusicVolume = onUpdateBgMusicVolume,
                    onStopBgMusic = onStopBgMusic,
                    onRetryPlayback = onRetryPlayback,
                    onPlayAlternativeTrack = onPlayAlternativeTrack,
                    onPlayVerseAudio = onPlayVerseAudio,
                    onShowTrackLicence = onShowTrackLicence
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerTransportColumn(
    playerState: GlobalPlayerState,
    activeAmbienceLabel: String?,
    isFavorite: Boolean,
    showInlineTransport: Boolean,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenAmbience: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onRetryPlayback: () -> Unit,
    onPlayAlternativeTrack: (AudioTrack) -> Unit,
    onShowTrackLicence: (AudioTrack) -> Unit
) {
    val track = playerState.currentEntry?.track

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ReciterAvatar(
                reciterName = track?.reciterName ?: "Tilawah",
                accentHex = 0xFF1F3B2C,
                size = 168.dp,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = track?.surahArabicName.orEmpty(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = QuranArabicFontFamily,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track?.let { "Surah ${it.surahName} (${it.surahMeaning})" } ?: "Select a Surah",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track?.reciterName ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Studio Pills: Background Sound & Sleep Timer
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (activeAmbienceLabel != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        if (activeAmbienceLabel != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenAmbience)
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Spa,
                            contentDescription = null,
                            tint = if (activeAmbienceLabel != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = activeAmbienceLabel ?: "Background Sound",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activeAmbienceLabel != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (playerState.sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        if (playerState.sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSleepTimer)
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bedtime,
                            contentDescription = null,
                            tint = if (playerState.sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = playerState.sleepTimerRemainingSec?.let { formatDurationSeconds(it) } ?: "Sleep Timer",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (playerState.sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (track != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable { onShowTrackLicence(track) }
                        .testTag("now_playing_licence_badge")
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Source and licence info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        val bitrateSuffix = track.bitrateKbps?.let { " • ${it}kbps" }.orEmpty()
                        Text(
                            text = "${track.sourceProvider.shortBadge}$bitrateSuffix • Licence Info",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (playerState.playbackError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = playerState.playbackError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onRetryPlayback,
                                modifier = Modifier.testTag("now_playing_retry_button")
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Stream")
                            }
                        }
                        if (playerState.alternativeVerifiedTracks.isNotEmpty()) {
                            Text(
                                text = "Switch to Verified Recording of This Surah:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                playerState.alternativeVerifiedTracks.take(4).forEach { alt ->
                                    OutlinedButton(
                                        onClick = { onPlayAlternativeTrack(alt) },
                                        modifier = Modifier.testTag("alt_track_${alt.id}")
                                    ) {
                                        Text("${alt.reciterName} (${alt.sourceProvider.shortBadge})")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showInlineTransport) {
                Spacer(modifier = Modifier.height(20.dp))

                val dur = playerState.durationMs.coerceAtLeast(1L)
                Slider(
                    value = (playerState.positionMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f),
                    onValueChange = { frac ->
                        if (playerState.durationMs > 0L) {
                            onSeekTo((frac * playerState.durationMs).toLong())
                        }
                    },
                    enabled = track != null && playerState.durationMs > 0L,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatDurationMs(playerState.positionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDurationMs(playerState.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerState.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onPrevious, enabled = track != null, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                    }
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        enabled = track != null,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.size(60.dp)
                    ) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play or pause",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    IconButton(onClick = onNext, enabled = track != null, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                    }
                    IconButton(onClick = onCycleRepeat, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = "Repeat",
                            tint = if (playerState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (track != null) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ScriptureReadingPanel(
    playerState: GlobalPlayerState,
    scriptureState: ScripturePanelState,
    availableTranslations: List<TranslationEdition>,
    arabicFontScale: Float,
    strings: UiStrings,
    onSelectTranslation: (TranslationEdition) -> Unit,
    onToggleCompactReading: () -> Unit,
    onSeekToVerse: (String) -> Unit,
    onToggleVerseRepeat: (String) -> Unit,
    onPlayVerseAudio: (Int, Int) -> Unit,
    onRetryScripture: () -> Unit
) {
    var translationMenuOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val hasVerifiedTimings = playerState.verseTimings.isNotEmpty()
    val showBasmalahBanner = scriptureState.surahNumber != 1 && scriptureState.surahNumber != 9

    LaunchedEffect(playerState.activeVerseKey) {
        val activeKey = playerState.activeVerseKey ?: return@LaunchedEffect
        val index = scriptureState.verses.indexOfFirst { it.verseKey == activeKey }
        if (index >= 0) {
            listState.animateScrollToItem(if (showBasmalahBanner) index + 1 else index)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Reader Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.quranTextAndTranslation,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (hasVerifiedTimings) {
                            "Ayah-synced highlighting active"
                        } else {
                            "Chapter stream • Tap Play Ayah for single-verse recitation"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        OutlinedButton(
                            onClick = { translationMenuOpen = true },
                            shape = CircleShape,
                            modifier = Modifier.testTag("translation_selector_button")
                        ) {
                            Text(
                                text = scriptureState.selectedTranslation.name,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = translationMenuOpen,
                            onDismissRequest = { translationMenuOpen = false }
                        ) {
                            availableTranslations.forEach { edition ->
                                DropdownMenuItem(
                                    text = { Text("${edition.name} (${edition.languageName})") },
                                    onClick = {
                                        translationMenuOpen = false
                                        onSelectTranslation(edition)
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onToggleCompactReading,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (scriptureState.isCompactReadingMode) Icons.Filled.UnfoldMore else Icons.Filled.UnfoldLess,
                            contentDescription = "Toggle Arabic-only or translation mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outline
            )

            when {
                scriptureState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                scriptureState.verses.isEmpty() -> {
                    EmptyStateCard(
                        title = "Quran Text Unavailable Offline",
                        subtitle = scriptureState.errorMessage ?: "Connect to api.quran.com to load verified Uthmani text and translations.",
                        actionLabel = strings.retry,
                        onActionClick = onRetryScripture
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        if (showBasmalahBanner) {
                            item(key = "basmalah_banner_${scriptureState.surahNumber}") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = QuranArabicFontFamily,
                                            fontSize = (26f * arabicFontScale).sp,
                                            lineHeight = (44f * arabicFontScale).sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }

                        items(
                            items = scriptureState.verses,
                            key = { it.verseKey }
                        ) { verse ->
                            val isActive = verse.verseKey == playerState.activeVerseKey
                            val isRepeating = verse.verseKey == playerState.repeatVerseKey
                            val hasVerseTiming = verse.timestampFromMs != null

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isActive) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = hasVerseTiming) { onSeekToVerse(verse.verseKey) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp, vertical = 14.dp)
                                    ) {
                                        // Ayah badge & single-ayah actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isActive) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                                }
                                            ) {
                                                Text(
                                                    text = verse.verseKey,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (isActive) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                TextButton(
                                                    onClick = { onPlayVerseAudio(scriptureState.surahNumber, verse.verseNumber) },
                                                    modifier = Modifier.testTag("play_verse_audio_${verse.verseNumber}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayArrow,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "Play Ayah",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                if (hasVerseTiming) {
                                                    TextButton(
                                                        onClick = { onToggleVerseRepeat(verse.verseKey) }
                                                    ) {
                                                        Text(
                                                            text = if (isRepeating) "Repeating" else "Repeat",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (isRepeating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Canonical Uthmani Arabic Scripture rendered strictly RTL
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                            Text(
                                                text = verse.textUthmani,
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontFamily = QuranArabicFontFamily,
                                                    fontSize = (28f * arabicFontScale).sp,
                                                    lineHeight = (52f * arabicFontScale).sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (!scriptureState.isCompactReadingMode) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = verse.translationText ?: strings.translationUnavailable,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (verse.translationText != null) {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                } else {
                                                    MaterialTheme.colorScheme.secondary
                                                }
                                            )
                                            verse.translationAttribution?.let { attr ->
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Translation: $attr",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!isActive) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
