package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.AudioCategory
import com.example.data.model.AudioEditionDescriptor
import com.example.data.model.AudioTrack
import com.example.data.model.DiagnosticLogEntry
import com.example.data.model.LicenceMetadata
import com.example.player.GlobalPlayerState
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.QuranArabicFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpokenIslamicAudioSection(
    sessions: List<AudioTrack>,
    selectedFilter: String,
    playerState: GlobalPlayerState,
    onSelectFilter: (String) -> Unit,
    onPlaySession: (AudioTrack, List<AudioTrack>) -> Unit,
    onShowLicenceInfo: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredSessions = remember(sessions, selectedFilter) {
        when (selectedFilter) {
            "adhkar" -> sessions.filter {
                it.id.startsWith("voice_adhkar_") || it.id == "voice_ruqya_kursi"
            }
            "ruqya" -> sessions.filter { it.audioCategory == AudioCategory.SPOKEN_RUQYA }
            "duas" -> sessions.filter { it.id.startsWith("voice_dua_") }
            else -> sessions
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spoken_islamic_audio_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderRow(
            title = "Adhkar, Quranic Duas & Ruqya",
            subtitle = "Dedicated authentic voice sessions — never auto-mixed over Quran recitation."
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "all" to "All (${sessions.size})",
                "adhkar" to "Morning & Evening Adhkar",
                "ruqya" to "Ruqya Recitations",
                "duas" to "Quranic Duas"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { onSelectFilter(key) },
                    label = { Text(label) },
                    modifier = Modifier.testTag("spoken_filter_$key")
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 760.dp) 2 else 1
            val rows = remember(filteredSessions, columns) { filteredSessions.chunked(columns) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { session ->
                            val isCurrent = playerState.currentEntry?.track?.id == session.id
                            val isPlaying = isCurrent && playerState.isPlaying

                            SpokenSessionTile(
                                session = session,
                                isCurrent = isCurrent,
                                isPlaying = isPlaying,
                                onPlay = { onPlaySession(session, filteredSessions) },
                                onShowLicence = { onShowLicenceInfo(session) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpokenSessionTile(
    session: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onShowLicence: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onPlay)
            .testTag("spoken_session_${session.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onPlay,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .testTag("spoken_play_${session.id}")
            ) {
                Icon(
                    imageVector = when {
                        isPlaying -> Icons.Filled.Pause
                        isCurrent -> Icons.Filled.GraphicEq
                        else -> Icons.Filled.PlayArrow
                    },
                    contentDescription = "Play ${session.surahName}",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.surahName,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${session.reciterName} • ${session.sourceProvider.shortBadge}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = session.surahArabicName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = QuranArabicFontFamily,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )

            IconButton(
                onClick = onShowLicence,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Licence and source info for ${session.surahName}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
fun TrackLicenceInfoDialog(
    track: AudioTrack,
    licence: LicenceMetadata,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Source & Licence Metadata")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${track.surahName} — ${track.reciterName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                MetadataLine("Category", track.audioCategory.displayLabel)
                MetadataLine("Provider", licence.sourceProviderName)
                MetadataLine("Licence", licence.licenceName)
                MetadataLine("Attribution", licence.attributionText)
                MetadataLine("Offline Policy", licence.downloadPermission.label)
                MetadataLine(
                    "Redistribution / Offline Confirmed",
                    if (licence.permissionConfirmed) "Yes (personal single-surah cache permitted)" else "No (streaming only)"
                )
                track.bitrateKbps?.let {
                    MetadataLine("Bitrate", "${it} kbps (${track.format.uppercase()})")
                }
                MetadataLine("Reference URL", licence.sourceWebpage)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = licence.usageNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AudioSourcesAndAttributionDialog(
    editions: List<AudioEditionDescriptor>,
    diagnosticEvents: List<DiagnosticLogEntry>,
    onDismiss: () -> Unit
) {
    val proxyConfigured = remember {
        runCatching { BuildConfig.QURAN_FOUNDATION_PROXY_URL.startsWith("https://") }.getOrDefault(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Audio Sources, Licences & Diagnostics") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "src_a") {
                    ProviderSummaryCard(
                        title = "Source A — Al Quran Cloud (Primary)",
                        status = "Active • No credentials required",
                        details = "Streams complete surahs (/audio-surah/{bitrate}/{edition}/{surah}.mp3) and global ayahs (/audio/{bitrate}/{edition}/{ayah}.mp3) via cdn.islamic.network. Personal single-surah offline caching enabled with attribution."
                    )
                }
                item(key = "src_b") {
                    ProviderSummaryCard(
                        title = "Source B — EveryAyah Library (Supplementary)",
                        status = "Active • Verse-by-verse & Adhkar/Dua audio",
                        details = "Streams verified SSSAAA.mp3 recordings across 9 reciters including Yasser Al-Dosari and Saad Al-Ghamdi. Offline download disabled (streaming only) due to per-studio licence terms."
                    )
                }
                item(key = "src_c") {
                    ProviderSummaryCard(
                        title = "Source C — Quran.com API v4 / QuranicAudio",
                        status = if (proxyConfigured) {
                            "Active • Server-side proxy configured"
                        } else {
                            "Active (Public v4) • Authenticated OAuth2 optional via server proxy"
                        },
                        details = "Provides canonical Uthmani text, translations, verse timestamps, and fallback chapter streams. Client secrets are never bundled in the APK (QURAN_FOUNDATION_PROXY_URL in Secrets)."
                    )
                }
                item(key = "src_dsp") {
                    ProviderSummaryCard(
                        title = "Environmental Ambience — Tilawah On-Device DSP",
                        status = "Active • 100% First-Party Procedural Licence",
                        details = "Synthesizes non-musical Rain, Water, Wind, Forest, and Night soundscapes locally in real time. Avoids Mixkit/Pixabay standalone redistribution restrictions."
                    )
                }

                item(key = "editions_count") {
                    Text(
                        text = "Validated Recitation Editions (${editions.size} mapped):",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(items = editions.take(12), key = { "${it.provider.code}_${it.editionId}" }) { ed ->
                    Text(
                        text = "• ${ed.displayName} [${if (ed.supportsFullSurah) "Full Surah" else "Verse Audio"}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (diagnosticEvents.isNotEmpty()) {
                    item(key = "diag_header") {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recent Stream Validation Diagnostics:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(items = diagnosticEvents.take(8)) { log ->
                        Text(
                            text = "[${log.source}] ${log.summary}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
private fun ProviderSummaryCard(
    title: String,
    status: String,
    details: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetadataLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
