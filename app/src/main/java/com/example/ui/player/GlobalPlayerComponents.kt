package com.example.ui.player

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AmbientSoundscape
import com.example.data.model.AudioTrack
import com.example.data.model.RepeatMode
import com.example.data.model.SoundCategory
import com.example.data.remote.AuthenticatedSeedCatalog
import com.example.i18n.UiStrings
import com.example.player.AmbientUiState
import com.example.player.GlobalPlayerState
import com.example.ui.components.ReciterAvatar
import com.example.ui.components.trackArtworkResId
import com.example.ui.navigation.formatDurationMs
import com.example.ui.navigation.formatDurationSeconds

@Composable
fun DesktopBottomPlayerBar(
    playerState: GlobalPlayerState,
    ambientState: AmbientUiState,
    isFavorite: Boolean,
    isCompactTablet: Boolean,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenAmbience: () -> Unit,
    onExpandNowPlaying: () -> Unit,
    onRetryPlayback: () -> Unit = {},
    onPlayAlternativeTrack: (AudioTrack) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeTrack = playerState.currentEntry?.track

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("desktop_player_bar"),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

            if (playerState.playbackError != null) {
                val firstAlt = playerState.alternativeVerifiedTracks.firstOrNull()
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = playerState.playbackError,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRetryPlayback) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", style = MaterialTheme.typography.labelSmall)
                        }
                        if (firstAlt != null) {
                            TextButton(onClick = { onPlayAlternativeTrack(firstAlt) }) {
                                Text(
                                    text = "Play ${firstAlt.reciterName}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Section: Rounded-Square Studio Artwork + Surah + Reciter + Favorite
                Row(
                    modifier = Modifier
                        .weight(0.30f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = activeTrack != null, onClick = onExpandNowPlaying)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReciterAvatar(
                        reciterName = activeTrack?.reciterName ?: "Tilawah",
                        accentHex = 0xFF1F3B2C,
                        size = 48.dp,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTrack?.surahName ?: "Select a Surah",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val activeSoundLabel = remember(ambientState.activeSoundId, ambientState.isPlaying) {
                            if (ambientState.isPlaying && ambientState.activeSoundId != "none") {
                                AuthenticatedSeedCatalog.ambientSoundscapes
                                    .firstOrNull { it.id == ambientState.activeSoundId }
                                    ?.defaultName
                            } else null
                        }
                        Text(
                            text = buildString {
                                append(activeTrack?.reciterName ?: "Ready to stream")
                                if (activeSoundLabel != null) append(" • $activeSoundLabel")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (activeTrack != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("player_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite current surah",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                // Center Section: Tight Transport + Constrained Timeline Scrubber
                Column(
                    modifier = Modifier.weight(0.42f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggleShuffle,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Toggle shuffle",
                                tint = if (playerState.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onPrevious,
                            enabled = activeTrack != null,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous track",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        FilledIconButton(
                            onClick = onTogglePlayPause,
                            enabled = activeTrack != null,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp)
                                .testTag("player_play_pause_button")
                        ) {
                            if (playerState.isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onNext,
                            enabled = activeTrack != null,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next track",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(
                            onClick = onCycleRepeat,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_repeat_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                contentDescription = "Cycle repeat mode",
                                tint = if (playerState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatDurationMs(playerState.positionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val dur = playerState.durationMs.coerceAtLeast(1L)
                        Slider(
                            value = (playerState.positionMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f),
                            onValueChange = { frac ->
                                if (playerState.durationMs > 0L) {
                                    onSeekTo((frac * playerState.durationMs).toLong())
                                }
                            },
                            enabled = activeTrack != null && playerState.durationMs > 0L,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .testTag("player_seek_slider")
                        )
                        Text(
                            text = formatDurationMs(playerState.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right Section: Ambience, Sleep Timer, Queue, Volume, Expand
                Row(
                    modifier = Modifier.weight(0.28f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(
                            onClick = onOpenAmbience,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_ambience_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = "Background soundscapes",
                                tint = if (ambientState.isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (ambientState.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = onOpenSleepTimer,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_sleep_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = "Sleep timer",
                                tint = if (playerState.sleepTimerRemainingSec != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (playerState.sleepTimerRemainingSec != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenQueue,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_queue_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Playback queue",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (!isCompactTablet) {
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Mute or unmute",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Slider(
                            value = if (playerState.isMuted) 0f else playerState.volume,
                            onValueChange = onVolumeChange,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.onSurface,
                                activeTrackColor = MaterialTheme.colorScheme.onSurface,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            modifier = Modifier.width(76.dp)
                        )
                    }
                    IconButton(
                        onClick = onExpandNowPlaying,
                        enabled = activeTrack != null,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_expand_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInFull,
                            contentDescription = "Open Now Playing view",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileMiniPlayerBar(
    playerState: GlobalPlayerState,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit = {},
    onToggleFavorite: () -> Unit,
    onOpenNowPlaying: () -> Unit,
    ambientState: AmbientUiState? = null,
    modifier: Modifier = Modifier
) {
    val activeTrack = playerState.currentEntry?.track ?: return
    val progress = if (playerState.durationMs > 0L) {
        (playerState.positionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val activeAmbienceName = remember(ambientState?.activeSoundId, ambientState?.isPlaying) {
        if (ambientState?.isPlaying == true && ambientState.activeSoundId != "none") {
            AuthenticatedSeedCatalog.ambientSoundscapes
                .firstOrNull { it.id == ambientState.activeSoundId }
                ?.defaultName
        } else null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenNowPlaying)
            .testTag("mobile_mini_player"),
        color = Color(0xFF171A20),
        border = BorderStroke(1.dp, Color(0xFF262A33)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = trackArtworkResId(activeTrack)),
                    contentDescription = "${activeTrack.surahName} cover artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeTrack.surahName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitleText = playerState.playbackError ?: buildString {
                        append(activeTrack.reciterName)
                        if (activeAmbienceName != null) append(" • $activeAmbienceName")
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (playerState.playbackError != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite (Like) heart button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mini_player_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite ${activeTrack.surahName}",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color(0xFFD4D4D8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Solid White Circular Play/Pause Button (48.dp touch target, 36.dp crisp white circle)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("mini_player_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF4F4F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0A0B0E)
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = Color(0xFF0A0B0E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Circular Expand Chevron-Up Button (48.dp touch target, 32.dp subtle dark circle)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenNowPlaying)
                        .testTag("mini_player_expand_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF232730))
                            .border(1.dp, Color(0xFF323744), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Expand Now Playing",
                            tint = Color(0xFFE4E4E7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun QueueDialog(
    playerState: GlobalPlayerState,
    strings: UiStrings,
    onDismiss: () -> Unit,
    onPlayEntry: (String) -> Unit,
    onMoveItem: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearQueue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${strings.queueTitle} (${playerState.queue.size})")
                if (playerState.queue.size > 1) {
                    TextButton(
                        onClick = onClearQueue,
                        modifier = Modifier.testTag("queue_clear_button")
                    ) {
                        Text("Clear Upcoming")
                    }
                }
            }
        },
        text = {
            if (playerState.queue.isEmpty()) {
                Text(
                    text = "Your playback queue is empty. Select any Surah or Collection to start listening.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = playerState.queue,
                        key = { _, item -> item.queueItemId }
                    ) { idx, entry ->
                        val isCurrent = entry.queueItemId == playerState.currentEntry?.queueItemId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayEntry(entry.queueItemId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${entry.track.surahNumber}. ${entry.track.surahName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = entry.track.reciterName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveItem(entry.queueItemId, -1) },
                                    enabled = idx > 0,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move earlier", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onMoveItem(entry.queueItemId, 1) },
                                    enabled = idx < playerState.queue.lastIndex,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move later", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onRemoveItem(entry.queueItemId) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove from queue", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun SleepTimerDialog(
    remainingSeconds: Int?,
    strings: UiStrings,
    onDismiss: () -> Unit,
    onStartTimerMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    var customMinutes by remember { mutableStateOf("") }
    val presets = listOf(15, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.sleepTimer) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (remainingSeconds != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active: ${formatDurationSeconds(remainingSeconds)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            OutlinedButton(
                                onClick = onCancelTimer,
                                modifier = Modifier.testTag("cancel_sleep_timer_button")
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }

                Text(
                    text = "Automatically pause playback after:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { mins ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                onStartTimerMinutes(mins)
                                onDismiss()
                            },
                            label = { Text("${mins}m") },
                            modifier = Modifier.testTag("sleep_preset_$mins")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { val clean = it.filter(Char::isDigit).take(3); customMinutes = clean },
                        label = { Text("Custom minutes") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val parsed = customMinutes.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                onStartTimerMinutes(parsed)
                                onDismiss()
                            }
                        },
                        enabled = (customMinutes.toIntOrNull() ?: 0) > 0
                    ) {
                        Text("Set")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AmbienceMixerDialog(
    ambientState: AmbientUiState,
    strings: UiStrings,
    onDismiss: () -> Unit,
    onSelectSound: (String) -> Unit,
    onPreviewSound: (String) -> Unit = {},
    onVolumeChange: (Float) -> Unit,
    onStopAll: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SoundCategory.ALL) }
    val allPlayableSounds = remember {
        AuthenticatedSeedCatalog.ambientSoundscapes.filter { it.id != "none" }
    }
    val filteredSounds = remember(selectedCategory, allPlayableSounds) {
        if (selectedCategory == SoundCategory.ALL) {
            allPlayableSounds
        } else {
            allPlayableSounds.filter { it.category == selectedCategory }
        }
    }

    val activeSoundName = remember(ambientState.activeSoundId, ambientState.isPlaying, ambientState.previewingSoundId) {
        val targetId = ambientState.previewingSoundId ?: ambientState.activeSoundId
        if ((ambientState.isPlaying || ambientState.previewingSoundId != null) && targetId != "none") {
            allPlayableSounds.firstOrNull { it.id == targetId }?.defaultName
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("background_sounds_panel"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.ambientSounds,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (activeSoundName != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (ambientState.previewingSoundId != null) "Preview • $activeSoundName" else activeSoundName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Layer calm natural soundscapes beneath recitation on an independent acoustic channel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_ambience_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close background sounds",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (ambientState.errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ambientState.errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Prominent Ambience Volume Mixer Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Ambience Volume",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${(ambientState.volume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = ambientState.volume,
                            onValueChange = onVolumeChange,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ambience_volume_slider")
                        )
                    }
                }

                // Category Filter Pills
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SoundCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("ambience_cat_${cat.id}")
                        )
                    }
                }

                // Tactile 2-Column Soundscape Grid
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val columns = if (maxWidth >= 340.dp) 2 else 1
                    val rows = remember(filteredSounds, columns) { filteredSounds.chunked(columns) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 290.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items = rows) { rowSounds ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowSounds.forEach { sound ->
                                    val isActive = ambientState.activeSoundId == sound.id &&
                                        ambientState.isPlaying &&
                                        ambientState.previewingSoundId == null
                                    val isPreviewing = ambientState.previewingSoundId == sound.id

                                    SoundscapeTileCard(
                                        sound = sound,
                                        isActive = isActive,
                                        isPreviewing = isPreviewing,
                                        onSelect = { onSelectSound(sound.id) },
                                        onPreview = { onPreviewSound(sound.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(columns - rowSounds.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Footer Actions: Stop All + Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onStopAll,
                        shape = CircleShape,
                        enabled = ambientState.isPlaying || ambientState.previewingSoundId != null || ambientState.activeSoundId != "none",
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("stop_all_ambience_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.stopAllAmbience)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("ambience_done_button")
                    ) {
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundscapeTileCard(
    sound: AmbientSoundscape,
    isActive: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundIcon = when (sound.category) {
        SoundCategory.AMBIENT -> Icons.Filled.Spa
        SoundCategory.RAIN -> Icons.Filled.WaterDrop
        SoundCategory.WATER -> Icons.Filled.Waves
        SoundCategory.WIND -> Icons.Filled.Air
        SoundCategory.FOREST -> Icons.Filled.Forest
        SoundCategory.NIGHT -> Icons.Filled.NightsStay
        SoundCategory.ALL -> Icons.Filled.Spa
    }

    val highlighted = isActive || isPreviewing

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = BorderStroke(
            width = if (highlighted) 1.5.dp else 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .testTag("ambience_option_${sound.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = soundIcon,
                        contentDescription = null,
                        tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onPreview,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ambience_preview_${sound.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Hearing,
                        contentDescription = "Preview ${sound.defaultName}",
                        tint = if (isPreviewing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Column {
                Text(
                    text = sound.defaultName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sound.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSelect)
                    .testTag("ambience_toggle_${sound.id}")
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isPreviewing -> "Previewing 5s..."
                            isActive -> "Active • Tap to Stop"
                            else -> "Enable"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
