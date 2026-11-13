package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.OfflineDownloadsUiState
import com.example.data.model.AudioTrack
import com.example.data.model.CuratedCollection
import com.example.data.model.CustomPlaylist
import com.example.data.model.ListeningEventRecord
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.model.UserPreferencesState
import com.example.i18n.UiStrings
import com.example.player.GlobalPlayerState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ReciterCardItem
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.TrackRowItem
import com.example.ui.navigation.formatDurationSeconds
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    playlists: List<CustomPlaylist>,
    surahs: List<Surah>,
    reciters: List<Reciter>,
    collections: List<CuratedCollection>,
    listeningEvents: List<ListeningEventRecord>,
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onResolveTracks: (List<String>) -> List<AudioTrack>,
    onBuildTrackForSurah: (String, Int) -> AudioTrack,
    onCreatePlaylist: (String, String) -> Unit,
    onUpdatePlaylistMetadata: (CustomPlaylist, String, String) -> Unit,
    onAddTrackToPlaylist: (CustomPlaylist, String) -> Unit,
    onRemoveTrackFromPlaylist: (CustomPlaylist, String) -> Unit,
    onReorderPlaylistTrack: (CustomPlaylist, Int, Int) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onPlayPlaylist: (CustomPlaylist, Boolean) -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onToggleFavoriteTrack: (String) -> Unit,
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onOpenAddToPlaylistDialog: (AudioTrack) -> Unit,
    onOpenReciter: (String) -> Unit,
    onPlayReciterFeatured: (String) -> Unit,
    onToggleFavoriteReciter: (String) -> Unit,
    onOpenCollection: (String) -> Unit,
    onPlayCollection: (CuratedCollection) -> Unit,
    onToggleSaveCollection: (String) -> Unit,
    onAddCollectionToQueue: (CuratedCollection) -> Unit,
    offlineState: OfflineDownloadsUiState = OfflineDownloadsUiState(),
    onIsDownloadPermitted: (AudioTrack) -> Boolean = { false },
    onDownloadTrackOffline: (AudioTrack) -> Unit = {},
    onRemoveOfflineTrack: (String) -> Unit = {},
    onClearOfflineError: () -> Unit = {},
    onShowTrackLicence: (AudioTrack) -> Unit = {}
) {
    var activeSection by remember { mutableStateOf("playlists") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var inspectingPlaylistId by remember { mutableStateOf<String?>(null) }
    var editingPlaylist by remember { mutableStateOf<CustomPlaylist?>(null) }
    var deletingPlaylist by remember { mutableStateOf<CustomPlaylist?>(null) }
    var addingTracksToPlaylist by remember { mutableStateOf<CustomPlaylist?>(null) }

    val inspectedPlaylist = playlists.firstOrNull { it.id == inspectingPlaylistId }
    val favTracks = remember(preferences.favoriteTrackIds, surahs) {
        onResolveTracks(preferences.favoriteTrackIds.toList())
    }
    val favReciters = remember(preferences.favoriteReciterIds, reciters) {
        reciters.filter { it.id in preferences.favoriteReciterIds }
    }
    val savedCollections = remember(preferences.savedCollectionIds, collections) {
        collections.filter { it.id in preferences.savedCollectionIds }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "library_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.libraryTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("library_create_playlist_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.createPlaylist)
                }
            }
        }

        item(key = "library_tabs") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "playlists" to "${strings.customPlaylists} (${playlists.size})",
                    "surahs" to "${strings.favoriteSurahs} (${favTracks.size})",
                    "offline" to "Offline Chapters (${offlineState.savedTracks.size})",
                    "reciters" to "${strings.favoriteReciters} (${favReciters.size})",
                    "collections" to "${strings.savedCollections} (${savedCollections.size})",
                    "history" to strings.listeningHistory
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = activeSection == key,
                        onClick = {
                            activeSection = key
                            inspectingPlaylistId = null
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("library_tab_$key")
                    )
                }
            }
        }

        when (activeSection) {
            "playlists" -> {
                if (inspectedPlaylist != null) {
                    val playlistTracks = onResolveTracks(inspectedPlaylist.trackIds)
                    item(key = "playlist_detail_card") {
                        PlaylistDetailCard(
                            playlist = inspectedPlaylist,
                            tracks = playlistTracks,
                            strings = strings,
                            onClose = { inspectingPlaylistId = null },
                            onPlayAll = { onPlayPlaylist(inspectedPlaylist, false) },
                            onShuffle = { onPlayPlaylist(inspectedPlaylist, true) },
                            onEditMetadata = { editingPlaylist = inspectedPlaylist },
                            onAddTracksClick = { addingTracksToPlaylist = inspectedPlaylist },
                            onDeleteClick = { deletingPlaylist = inspectedPlaylist },
                            onPlaySingleTrack = { onPlayTrack(it) },
                            onMoveTrack = { idx, delta -> onReorderPlaylistTrack(inspectedPlaylist, idx, delta) },
                            onRemoveTrack = { trackId -> onRemoveTrackFromPlaylist(inspectedPlaylist, trackId) }
                        )
                    }
                } else if (playlists.isEmpty()) {
                    item(key = "playlists_empty") {
                        EmptyStateCard(
                            title = "No Custom Playlists Yet",
                            subtitle = "Create a custom Quran listening playlist for morning study, travel, or evening calm.",
                            actionLabel = strings.createPlaylist,
                            onActionClick = { showCreateDialog = true }
                        )
                    }
                } else {
                    items(items = playlists, key = { it.id }) { pl ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inspectingPlaylistId = pl.id }
                                .testTag("playlist_card_${pl.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pl.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (pl.description.isNotBlank()) {
                                        Text(
                                            text = pl.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${pl.trackIds.size} surahs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { onPlayPlaylist(pl, false) },
                                    enabled = pl.trackIds.isNotEmpty(),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play ${pl.name}")
                                }
                                IconButton(
                                    onClick = { deletingPlaylist = pl },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete ${pl.name}")
                                }
                            }
                        }
                    }
                }
            }

            "surahs" -> {
                if (favTracks.isEmpty()) {
                    item(key = "fav_surahs_empty") {
                        EmptyStateCard(
                            title = "No Favorite Surahs Saved",
                            subtitle = "Tap the heart icon on any chapter row or in the audio player to save it here."
                        )
                    }
                } else {
                    items(items = favTracks, key = { it.id }) { track ->
                        val isCurrent = playerState.currentEntry?.track?.id == track.id
                        TrackRowItem(
                            track = track,
                            isCurrentTrack = isCurrent,
                            isPlaying = isCurrent && playerState.isPlaying,
                            isFavorite = true,
                            onPlayClick = { onPlayTrack(track) },
                            onToggleFavorite = { onToggleFavoriteTrack(track.id) },
                            onAddToQueue = { onAddTrackToQueue(track) },
                            onAddToPlaylistClick = { onOpenAddToPlaylistDialog(track) },
                            onViewReciter = { onOpenReciter(track.reciterId) },
                            isOfflineSaved = track.id in offlineState.savedTracks,
                            downloadProgress = offlineState.activeDownloadProgress[track.id],
                            isDownloadPermitted = onIsDownloadPermitted(track),
                            onDownloadOfflineClick = { onDownloadTrackOffline(track) },
                            onRemoveOfflineClick = { onRemoveOfflineTrack(track.id) },
                            onShowLicenceInfo = onShowTrackLicence
                        )
                    }
                }
            }

            "offline" -> {
                if (offlineState.lastDownloadError != null) {
                    item(key = "offline_error_banner") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = offlineState.lastDownloadError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onClearOfflineError) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }

                if (offlineState.activeDownloadProgress.isNotEmpty()) {
                    val activeEntries = offlineState.activeDownloadProgress.entries.toList()
                    items(items = activeEntries, key = { "dl_${it.key}" }) { (trackId, progress) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Saving personal offline copy ($trackId) — ${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                val savedList = offlineState.savedTracks.values.sortedByDescending { it.savedAtEpochMs }
                if (savedList.isEmpty() && offlineState.activeDownloadProgress.isEmpty()) {
                    item(key = "offline_empty") {
                        EmptyStateCard(
                            title = "No Personal Offline Chapters Saved",
                            subtitle = "Open the options menu on any Al Quran Cloud chapter row and choose \"Save for Personal Offline Use\". Sources with stream-only terms (such as EveryAyah) remain streaming-only."
                        )
                    }
                } else {
                    items(items = savedList, key = { "saved_${it.trackId}" }) { saved ->
                        val sizeMb = String.format(Locale.US, "%.2f MB", saved.fileSizeBytes / (1024.0 * 1024.0))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("offline_saved_card_${saved.trackId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${saved.surahNumber}. ${saved.surahName} — ${saved.reciterName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$sizeMb • ${saved.licenceName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = saved.attributionText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val resolved = onResolveTracks(listOf(saved.trackId)).firstOrNull()
                                            ?: onBuildTrackForSurah(saved.reciterId, saved.surahNumber)
                                        onPlayTrack(resolved)
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play offline ${saved.surahName}")
                                }
                                IconButton(
                                    onClick = { onRemoveOfflineTrack(saved.trackId) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete offline ${saved.surahName}")
                                }
                            }
                        }
                    }
                }
            }

            "reciters" -> {
                if (favReciters.isEmpty()) {
                    item(key = "fav_reciters_empty") {
                        EmptyStateCard(
                            title = "No Saved Reciters",
                            subtitle = "Save your preferred Qaris from the Reciters directory for quick access."
                        )
                    }
                } else {
                    items(items = favReciters, key = { it.id }) { reciter ->
                        ReciterCardItem(
                            reciter = reciter,
                            isFavorite = true,
                            onOpenProfile = { onOpenReciter(reciter.id) },
                            onPlayReciter = { onPlayReciterFeatured(reciter.id) },
                            onToggleFavorite = { onToggleFavoriteReciter(reciter.id) }
                        )
                    }
                }
            }

            "collections" -> {
                if (savedCollections.isEmpty()) {
                    item(key = "saved_cols_empty") {
                        EmptyStateCard(
                            title = "No Saved Collections",
                            subtitle = "Bookmark curated collections to keep them in your library."
                        )
                    }
                } else {
                    item(key = "saved_cols_grid") {
                        ResponsiveCollectionsGrid(
                            collections = savedCollections,
                            savedCollectionIds = preferences.savedCollectionIds,
                            onOpenCollection = onOpenCollection,
                            onPlayCollection = onPlayCollection,
                            onToggleSaveCollection = onToggleSaveCollection,
                            onAddCollectionToQueue = onAddCollectionToQueue
                        )
                    }
                }
            }

            "history" -> {
                if (listeningEvents.isEmpty()) {
                    item(key = "history_empty") {
                        EmptyStateCard(
                            title = "No Recorded Listening History",
                            subtitle = "Listening history records real playback sessions once a chapter plays for at least 5 seconds."
                        )
                    }
                } else {
                    val dateFmt = SimpleDateFormat("MMM d, HH:mm", Locale.US)
                    items(items = listeningEvents.take(40), key = { it.id }) { event ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val resolved = onResolveTracks(listOf(event.trackId)).firstOrNull()
                                        ?: onBuildTrackForSurah(event.reciterId, event.surahNumber)
                                    onPlayTrack(resolved)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${event.surahNumber}. ${event.surahName}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${event.reciterName} • Listened ${formatDurationSeconds(event.durationListenedSec)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = if (event.timestampEpochMs > 0L) dateFmt.format(Date(event.timestampEpochMs)) else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PlaylistFormDialog(
            dialogTitle = strings.createPlaylist,
            initialName = "",
            initialDescription = "",
            confirmLabel = "Create",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreatePlaylist(name, desc)
                showCreateDialog = false
            }
        )
    }

    editingPlaylist?.let { target ->
        PlaylistFormDialog(
            dialogTitle = "Edit Playlist",
            initialName = target.name,
            initialDescription = target.description,
            confirmLabel = "Save",
            onDismiss = { editingPlaylist = null },
            onConfirm = { name, desc ->
                onUpdatePlaylistMetadata(target, name, desc)
                editingPlaylist = null
            }
        )
    }

    deletingPlaylist?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingPlaylist = null },
            title = { Text("Delete Playlist?") },
            text = { Text("Delete \"${target.name}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(target.id)
                        if (inspectingPlaylistId == target.id) inspectingPlaylistId = null
                        deletingPlaylist = null
                    },
                    modifier = Modifier.testTag("confirm_delete_playlist_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPlaylist = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    addingTracksToPlaylist?.let { target ->
        var surahQuery by remember { mutableStateOf("") }
        val defaultReciter = reciters.firstOrNull()?.id ?: "mishary_alafasy"
        val matchingSurahs = remember(surahs, surahQuery) {
            if (surahQuery.isBlank()) surahs.take(25)
            else surahs.filter {
                it.displayName.contains(surahQuery, ignoreCase = true) ||
                    it.arabicName.contains(surahQuery) ||
                    it.number.toString() == surahQuery.trim()
            }.take(25)
        }
        AlertDialog(
            onDismissRequest = { addingTracksToPlaylist = null },
            title = { Text("Add Surahs to ${target.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = surahQuery,
                        onValueChange = { surahQuery = it },
                        placeholder = { Text("Search Surah...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(items = matchingSurahs, key = { it.id }) { surah ->
                            val track = onBuildTrackForSurah(defaultReciter, surah.number)
                            val alreadyAdded = track.id in target.trackIds
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !alreadyAdded) {
                                        onAddTrackToPlaylist(target, track.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${surah.number}. ${surah.displayName}", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = if (alreadyAdded) "Added" else "+ Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { addingTracksToPlaylist = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaylistDetailCard(
    playlist: CustomPlaylist,
    tracks: List<AudioTrack>,
    strings: UiStrings,
    onClose: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onEditMetadata: () -> Unit,
    onAddTracksClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlaySingleTrack: (AudioTrack) -> Unit,
    onMoveTrack: (Int, Int) -> Unit,
    onRemoveTrack: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Text("← All Playlists")
                }
                Row {
                    IconButton(onClick = onEditMetadata, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit playlist")
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete playlist")
                    }
                }
            }

            SectionHeaderRow(
                title = playlist.name,
                subtitle = playlist.description.ifBlank { "${tracks.size} surahs" }
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onPlayAll, enabled = tracks.isNotEmpty()) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.playAll)
                }
                OutlinedButton(onClick = onShuffle, enabled = tracks.isNotEmpty()) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.shufflePlay)
                }
                OutlinedButton(onClick = onAddTracksClick) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Surahs")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            if (tracks.isEmpty()) {
                Text(
                    text = "This playlist has no tracks yet. Tap \"Add Surahs\" above to populate it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tracks.forEachIndexed { idx, track ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaySingleTrack(track) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${track.surahNumber}. ${track.surahName}", style = MaterialTheme.typography.titleSmall)
                                Text(track.reciterName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { onMoveTrack(idx, -1) },
                                enabled = idx > 0,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = { onMoveTrack(idx, 1) },
                                enabled = idx < tracks.lastIndex,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
                            }
                            IconButton(
                                onClick = { onRemoveTrack(track.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove track")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistFormDialog(
    dialogTitle: String,
    initialName: String,
    initialDescription: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(300) },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_description_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim()) },
                enabled = name.trim().isNotEmpty(),
                modifier = Modifier.testTag("playlist_save_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    track: AudioTrack,
    playlists: List<CustomPlaylist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (CustomPlaylist) -> Unit,
    onCreateAndAdd: (String, String) -> Unit
) {
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add \"${track.surahName}\" to Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (playlists.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(items = playlists, key = { it.id }) { pl ->
                            val exists = track.id in pl.trackIds
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !exists) {
                                        onSelectPlaylist(pl)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(pl.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = if (exists) "Already in playlist" else "Add",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }

                Text("Or create a new playlist:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it.take(80) },
                        placeholder = { Text("New playlist title") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newName.trim().isNotEmpty()) {
                                onCreateAndAdd(newName.trim(), "")
                                onDismiss()
                            }
                        },
                        enabled = newName.trim().isNotEmpty()
                    ) {
                        Text("Create")
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
