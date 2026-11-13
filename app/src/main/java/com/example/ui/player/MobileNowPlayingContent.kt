package com.example.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AmbientSoundscape
import com.example.data.model.AudioTrack
import com.example.data.model.RepeatMode
import com.example.data.model.TranslationEdition
import com.example.data.remote.AuthenticatedSeedCatalog
import com.example.i18n.UiStrings
import com.example.player.AmbientUiState
import com.example.player.GlobalPlayerState
import com.example.ui.components.QuranWaveSymbol
import com.example.ui.components.trackArtworkResId
import com.example.ui.navigation.formatCompactDurationMs
import com.example.ui.navigation.formatDurationSeconds
import com.example.viewmodel.ScripturePanelState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MobileNowPlayingContent(
    playerState: GlobalPlayerState,
    scriptureState: ScripturePanelState,
    availableTranslations: List<TranslationEdition>,
    arabicFontScale: Float,
    isFavorite: Boolean,
    strings: UiStrings,
    ambientState: AmbientUiState?,
    initialOpenReadSheet: Boolean,
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
    onToggleBgMusicPlayPause: () -> Unit,
    onSetBgMusicEnabled: (Boolean) -> Unit,
    onSelectBgMusicTrack: (String) -> Unit,
    onUpdateBgMusicVolume: (Float) -> Unit,
    onStopBgMusic: () -> Unit,
    onRetryPlayback: () -> Unit,
    onPlayAlternativeTrack: (AudioTrack) -> Unit,
    onPlayVerseAudio: (Int, Int) -> Unit,
    onShowTrackLicence: (AudioTrack) -> Unit
) {
    var showReadSheet by remember(initialOpenReadSheet) { mutableStateOf(initialOpenReadSheet) }
    var showBgMusicPanel by remember { mutableStateOf(true) }
    var overflowMenuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = showReadSheet) {
        showReadSheet = false
    }

    val track = playerState.currentEntry?.track
    val resolvedAmbient = ambientState ?: AmbientUiState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 4.dp)
    ) {
        // Top Bar: Left collapse chevron-down + Right overflow menu (...)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (showReadSheet) {
                        showReadSheet = false
                    } else {
                        onMinimize()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("minimize_now_playing_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Minimize Now Playing",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (showReadSheet) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track?.surahName ?: strings.quranTextAndTranslation,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.reciterName ?: "Synchronized Quran Reader",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Box {
                IconButton(
                    onClick = { overflowMenuOpen = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("now_playing_overflow_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "More player options",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = overflowMenuOpen,
                    onDismissRequest = { overflowMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (showReadSheet) "Return to Artwork Player" else strings.quranTextAndTranslation) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null)
                        },
                        onClick = {
                            overflowMenuOpen = false
                            showReadSheet = !showReadSheet
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.queueTitle) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)
                        },
                        onClick = {
                            overflowMenuOpen = false
                            onOpenQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            val suffix = playerState.sleepTimerRemainingSec?.let { " (${formatDurationSeconds(it)})" }.orEmpty()
                            Text("${strings.sleepTimer}$suffix")
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Bedtime, contentDescription = null)
                        },
                        onClick = {
                            overflowMenuOpen = false
                            onOpenSleepTimer()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("All Background Soundscapes") },
                        leadingIcon = {
                            Icon(Icons.Filled.Spa, contentDescription = null)
                        },
                        onClick = {
                            overflowMenuOpen = false
                            onOpenAmbience()
                        }
                    )
                    if (track != null) {
                        DropdownMenuItem(
                            text = { Text("Audio Source & Licence Info") },
                            leadingIcon = {
                                Icon(Icons.Filled.Info, contentDescription = null)
                            },
                            onClick = {
                                overflowMenuOpen = false
                                onShowTrackLicence(track)
                            }
                        )
                    }
                }
            }
        }

        if (showReadSheet) {
            // Synchronized Quran Reading Sheet View with Quick Return & Transport Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable { showReadSheet = false }
                        .testTag("close_read_sheet_button")
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Back to Player",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = track != null, onClick = onTogglePlayPause)
                        .testTag("reader_inline_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF4F4F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color(0xFF0A0B0E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
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
        } else {
            // Main Quranify-Inspired Mobile Now Playing Screen (Phone 2)
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val containerWidth = maxWidth
                val isCompactHeight = maxHeight < 700.dp
                val artworkSize = minOf(
                    containerWidth * 0.58f,
                    maxHeight * 0.27f
                ).coerceIn(124.dp, 208.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 8.dp)
                        .testTag("now_playing_scroll_column"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Centered Square Rounded Atmospheric Cover Artwork
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .size(artworkSize)
                            .testTag("now_playing_artwork")
                    ) {
                        Image(
                            painter = painterResource(id = trackArtworkResId(track)),
                            contentDescription = "${track?.surahName ?: "Quran"} atmospheric cover artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 12.dp))

                    // Centered Surah Title & Reciter Subtitle
                    Text(
                        text = track?.surahName ?: "Select a Surah",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (containerWidth < 340.dp) 20.sp else 23.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("now_playing_surah_title")
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = track?.reciterName ?: "Ready to stream",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("now_playing_reciter_name")
                    )

                    if (playerState.playbackError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
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
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onRetryPlayback,
                                        modifier = Modifier.testTag("now_playing_retry_button")
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retry Stream")
                                    }
                                    playerState.alternativeVerifiedTracks.take(2).forEach { alt ->
                                        OutlinedButton(
                                            onClick = { onPlayAlternativeTrack(alt) },
                                            modifier = Modifier.testTag("alt_track_${alt.id}")
                                        ) {
                                            Text(alt.reciterName)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 10.dp))

                    // Synchronized Emerald Progress Scrubber + Compact Time Labels
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
                            thumbColor = Color(0xFF4ADE80),
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color(0xFF2A2E37),
                            disabledThumbColor = Color(0xFF52525B),
                            disabledActiveTrackColor = Color(0xFF3F3F46),
                            disabledInactiveTrackColor = Color(0xFF27272A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .testTag("now_playing_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatCompactDurationMs(playerState.positionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCompactDurationMs(playerState.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))

                    // Primary Playback Controls Row (Shuffle, Previous, Large White Circle Play/Pause, Next, Repeat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onToggleShuffle,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mobile_footer_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playerState.shuffleEnabled) MaterialTheme.colorScheme.primary else Color(0xFFD4D4D8),
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        IconButton(
                            onClick = onPrevious,
                            enabled = track != null,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mobile_footer_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous surah",
                                tint = Color(0xFFF4F4F5),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Large Solid White Circle Play/Pause Button (58.dp)
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF4F4F5))
                                .clickable(enabled = track != null, onClick = onTogglePlayPause)
                                .testTag("mobile_footer_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (playerState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color(0xFF0A0B0E)
                                )
                            } else {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "Pause recitation" else "Play recitation",
                                    tint = Color(0xFF0A0B0E),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onNext,
                            enabled = track != null,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mobile_footer_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next surah",
                                tint = Color(0xFFF4F4F5),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = onCycleRepeat,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mobile_footer_repeat_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                contentDescription = "Repeat mode",
                                tint = if (playerState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else Color(0xFFD4D4D8),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 12.dp))

                    // Secondary 4-Action Row: Like | Queue | Read | Background Sounds
                    val isBgSoundsHighlighted = showBgMusicPanel || resolvedAmbient.isEnabled || resolvedAmbient.isPlaying
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top
                    ) {
                        SecondaryPlayerActionCircle(
                            label = "Like",
                            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            isHighlighted = isFavorite,
                            onClick = onToggleFavorite,
                            testTag = "now_playing_favorite_button",
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryPlayerActionCircle(
                            label = "Queue",
                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                            isHighlighted = false,
                            onClick = onOpenQueue,
                            testTag = "now_playing_queue_button",
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryPlayerActionCircle(
                            label = "Read",
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            isHighlighted = false,
                            onClick = { showReadSheet = true },
                            testTag = "now_playing_read_button",
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryPlayerWaveCircle(
                            label = "Background\nSounds",
                            isHighlighted = isBgSoundsHighlighted,
                            onClick = { showBgMusicPanel = !showBgMusicPanel },
                            testTag = "now_playing_bg_sounds_button",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Embedded Background Music Panel (Phone 2 bottom card)
                    if (showBgMusicPanel) {
                        Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 12.dp))
                        EmbeddedBackgroundMusicPanel(
                            ambientState = resolvedAmbient,
                            onTogglePlayPause = onToggleBgMusicPlayPause,
                            onSetEnabled = onSetBgMusicEnabled,
                            onSelectTrack = onSelectBgMusicTrack,
                            onUpdateVolume = onUpdateBgMusicVolume,
                            onStopMusic = onStopBgMusic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryPlayerActionCircle(
    label: String,
    icon: ImageVector,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isHighlighted) Color(0xFF102A1B) else Color(0xFF1A1D24))
                .border(
                    width = 1.dp,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color(0xFF282C36),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label.replace('\n', ' '),
                tint = if (isHighlighted) MaterialTheme.colorScheme.primary else Color(0xFFE4E4E7),
                modifier = Modifier.size(21.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 13.sp
            ),
            color = if (isHighlighted) Color(0xFFE4E4E7) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SecondaryPlayerWaveCircle(
    label: String,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isHighlighted) Color(0xFF102A1B) else Color(0xFF1A1D24))
                .border(
                    width = 1.dp,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color(0xFF282C36),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            QuranWaveSymbol(
                size = 20.dp,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color(0xFFE4E4E7)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 13.sp
            ),
            color = Color(0xFFE4E4E7),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmbeddedBackgroundMusicPanel(
    ambientState: AmbientUiState,
    onTogglePlayPause: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onSelectTrack: (String) -> Unit,
    onUpdateVolume: (Float) -> Unit,
    onStopMusic: () -> Unit
) {
    var trackPickerOpen by remember { mutableStateOf(false) }
    val playableSounds: List<AmbientSoundscape> = remember {
        AuthenticatedSeedCatalog.ambientSoundscapes.filter { it.id != "none" }
    }
    val selectedSound = remember(ambientState.selectedTrackId, ambientState.activeSoundId, playableSounds) {
        val idToShow = if (ambientState.activeSoundId != "none") {
            ambientState.activeSoundId
        } else {
            ambientState.selectedTrackId
        }
        playableSounds.firstOrNull { it.id == idToShow } ?: playableSounds.first()
    }
    val artworkRes = if (selectedSound.artworkResId != 0) {
        selectedSound.artworkResId
    } else {
        R.drawable.art_bg_painted_dream
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF16181D),
        border = BorderStroke(1.dp, Color(0xFF252932)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("background_music_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Heading + Track Selector Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Background Music",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { trackPickerOpen = true }
                            .padding(horizontal = 8.dp)
                            .testTag("bg_music_track_selector"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Select background soundscape",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = trackPickerOpen,
                        onDismissRequest = { trackPickerOpen = false }
                    ) {
                        playableSounds.forEach { sound ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = sound.defaultName,
                                            fontWeight = if (sound.id == selectedSound.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = sound.category.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    trackPickerOpen = false
                                    onSelectTrack(sound.id)
                                },
                                modifier = Modifier.testTag("bg_music_option_${sound.id}")
                            )
                        }
                    }
                }
            }

            // Track Artwork + Name/Category + Play/Pause + Enable Switch + Independent Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = artworkRes),
                    contentDescription = "${selectedSound.defaultName} artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSound.defaultName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("bg_music_track_title")
                            )
                            Text(
                                text = selectedSound.category.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Independent Circular Play/Pause Button (48.dp touch target)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onTogglePlayPause)
                                .testTag("bg_music_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF232730))
                                    .border(1.dp, Color(0xFF323744), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (ambientState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (ambientState.isPlaying) "Pause background music" else "Play background music",
                                    tint = Color(0xFFF4F4F5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Emerald Enable / Disable Toggle Switch (disabled by default)
                        Switch(
                            checked = ambientState.isEnabled,
                            onCheckedChange = onSetEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color(0xFFA1A1AA),
                                uncheckedTrackColor = Color(0xFF262A33),
                                uncheckedBorderColor = Color(0xFF3F3F46)
                            ),
                            modifier = Modifier.testTag("bg_music_enable_switch")
                        )
                    }

                    // Independent Background Music Volume Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Background music volume",
                            tint = Color(0xFFD4D4D8),
                            modifier = Modifier.size(18.dp)
                        )
                        Slider(
                            value = ambientState.volume.coerceIn(0f, 1f),
                            onValueChange = onUpdateVolume,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color(0xFF2C303A)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .testTag("bg_music_volume_slider")
                        )
                    }
                }
            }

            // Full-Width Pill "Stop Music" Button
            Surface(
                shape = CircleShape,
                color = Color(0xFF242830),
                border = BorderStroke(1.dp, Color(0xFF303540)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onStopMusic)
                    .testTag("bg_music_stop_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop Music",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFF4F4F5)
                    )
                }
            }
        }
    }
}
