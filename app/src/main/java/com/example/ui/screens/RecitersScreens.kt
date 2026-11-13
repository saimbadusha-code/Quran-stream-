package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.data.model.ListeningEventRecord
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.model.UserPreferencesState
import com.example.data.remote.QuranCatalogRepository
import com.example.i18n.UiStrings
import com.example.player.GlobalPlayerState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ReciterAvatar
import com.example.ui.components.ReciterCardItem
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.TrackRowItem
import com.example.ui.theme.QuranArabicFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecitersOverviewScreen(
    reciters: List<Reciter>,
    searchQuery: String,
    countryFilter: String,
    sortMode: String,
    preferences: UserPreferencesState,
    recentEvents: List<ListeningEventRecord>,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onSearchQueryChange: (String) -> Unit,
    onCountryFilterChange: (String) -> Unit,
    onSortModeChange: (String) -> Unit,
    onOpenReciter: (String) -> Unit,
    onPlayReciterFeatured: (String) -> Unit,
    onToggleFavoriteReciter: (String) -> Unit
) {
    val countries = remember(reciters) {
        listOf("All") + reciters.mapNotNull { it.country }.distinct().sorted()
    }
    val recentReciterIds = remember(recentEvents) {
        recentEvents.map { it.reciterId }.distinct().toSet()
    }

    val filteredReciters = remember(
        reciters,
        searchQuery,
        countryFilter,
        sortMode,
        preferences.favoriteReciterIds,
        recentReciterIds
    ) {
        val qTrim = searchQuery.trim()
        val byQuery = reciters.filter { r ->
            val countryOk = countryFilter == "All" || r.country.equals(countryFilter, ignoreCase = true)
            val queryOk = qTrim.isEmpty() ||
                QuranCatalogRepository.matchesSearchQuery(r.displayName, qTrim) ||
                (r.arabicName?.contains(qTrim) == true) ||
                (r.country?.let { QuranCatalogRepository.matchesSearchQuery(it, qTrim) } == true) ||
                (r.recitationStyle?.let { QuranCatalogRepository.matchesSearchQuery(it, qTrim) } == true)
            val modeOk = when (sortMode) {
                "Best for Tajwid" -> r.isTajwidFeatured
                "Favorites" -> r.id in preferences.favoriteReciterIds
                "Recently Played" -> r.id in recentReciterIds
                else -> true
            }
            countryOk && queryOk && modeOk
        }

        when (sortMode) {
            "Alphabetical" -> byQuery.sortedBy { it.displayName }
            else -> byQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reciters_overview_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "reciters_header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.navReciters,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Select a reciter to stream complete 114-surah chapters with verified Haramain and studio provenance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "reciters_search") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(strings.searchRecitersPlaceholder) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear reciter search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reciters_search_input")
            )
        }

        item(key = "reciters_filters") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Featured", "Alphabetical", "Best for Tajwid", "Favorites", "Recently Played").forEach { mode ->
                        FilterChip(
                            selected = sortMode == mode,
                            onClick = { onSortModeChange(mode) },
                            label = { Text(mode) },
                            modifier = Modifier.testTag("reciter_mode_${mode.lowercase().replace(" ", "_")}")
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    countries.forEach { country ->
                        FilterChip(
                            selected = countryFilter == country,
                            onClick = { onCountryFilterChange(country) },
                            label = { Text(country) },
                            modifier = Modifier.testTag("reciter_country_${country.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        if (filteredReciters.isEmpty()) {
            item(key = "reciters_empty") {
                EmptyStateCard(
                    title = strings.emptyResults,
                    subtitle = "No reciters match the selected filters.",
                    actionLabel = "Clear Filters",
                    onActionClick = {
                        onSearchQueryChange("")
                        onCountryFilterChange("All")
                        onSortModeChange("Featured")
                    }
                )
            }
        } else {
            item(key = "reciters_grid") {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cols = when {
                        maxWidth >= 1160.dp -> 5
                        maxWidth >= 880.dp -> 4
                        maxWidth >= 600.dp -> 3
                        maxWidth >= 330.dp -> 2
                        else -> 1
                    }
                    val rows = remember(filteredReciters, cols) { filteredReciters.chunked(cols) }

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEach { reciter ->
                                    ReciterCardItem(
                                        reciter = reciter,
                                        isFavorite = reciter.id in preferences.favoriteReciterIds,
                                        onOpenProfile = { onOpenReciter(reciter.id) },
                                        onPlayReciter = { onPlayReciterFeatured(reciter.id) },
                                        onToggleFavorite = { onToggleFavoriteReciter(reciter.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(cols - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReciterDetailScreen(
    reciter: Reciter?,
    surahs: List<Surah>,
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    recentEvents: List<ListeningEventRecord>,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onBack: () -> Unit,
    onBuildTrack: (String, Int) -> AudioTrack,
    onPlayReciterFeatured: (String) -> Unit,
    onToggleFavoriteReciter: (String) -> Unit,
    onPlaySurahList: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleFavoriteTrack: (String) -> Unit,
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onAddTrackToPlaylist: (AudioTrack) -> Unit,
    savedOfflineTrackIds: Set<String> = emptySet(),
    activeDownloadProgress: Map<String, Float> = emptyMap(),
    onIsDownloadPermitted: (AudioTrack) -> Boolean = { false },
    onDownloadTrackOffline: (AudioTrack) -> Unit = {},
    onRemoveOfflineTrack: (String) -> Unit = {},
    onShowTrackLicence: (AudioTrack) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    if (reciter == null) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            EmptyStateCard(
                title = "Reciter Not Found",
                subtitle = "The requested reciter profile could not be loaded.",
                actionLabel = "Back to Reciters",
                onActionClick = onBack
            )
        }
        return
    }

    var surahFilter by remember { mutableStateOf("") }
    val isFavReciter = reciter.id in preferences.favoriteReciterIds
    val reciterAccent = Color(reciter.avatarAccentHex)

    val reciterTracks = remember(reciter.id, surahs) {
        surahs.map { s -> onBuildTrack(reciter.id, s.number) }
    }

    val filteredTracks = remember(reciterTracks, surahFilter) {
        val q = surahFilter.trim()
        if (q.isEmpty()) {
            reciterTracks
        } else {
            reciterTracks.filter { t ->
                QuranCatalogRepository.matchesSearchQuery(t.surahName, q) ||
                    QuranCatalogRepository.matchesSearchQuery(t.surahMeaning, q) ||
                    t.surahArabicName.contains(q) ||
                    t.surahNumber.toString() == q
            }
        }
    }

    val recentByReciter = remember(recentEvents, reciter.id, reciterTracks) {
        val ids = recentEvents.filter { it.reciterId == reciter.id }.map { it.trackId }.distinct().take(3)
        reciterTracks.filter { it.id in ids }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reciter_detail_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = bottomReservedPadding + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "reciter_back_row") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reciter_detail_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = strings.navReciters,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "reciter_profile_header") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    reciterAccent.copy(alpha = 0.30f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReciterAvatar(
                                reciterName = reciter.displayName,
                                accentHex = reciter.avatarAccentHex,
                                arabicName = reciter.arabicName,
                                size = 104.dp,
                                shape = CircleShape
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "RECITER PROFILE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = reciter.displayName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!reciter.arabicName.isNullOrBlank()) {
                                    Text(
                                        text = reciter.arabicName,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = QuranArabicFontFamily,
                                            fontSize = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = listOfNotNull(
                                        reciter.country,
                                        reciter.recitationStyle,
                                        reciter.riwayah,
                                        reciter.sourceProvenance
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Text(
                            text = reciter.neutralDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onPlayReciterFeatured(reciter.id) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("reciter_detail_play_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${strings.play} Featured Set", fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = { onToggleFavoriteReciter(reciter.id) },
                                shape = CircleShape,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("reciter_detail_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (isFavReciter) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavReciter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFavReciter) "Saved Reciter" else "Save Reciter")
                            }
                        }
                    }
                }
            }
        }

        if (recentByReciter.isNotEmpty()) {
            item(key = "reciter_recent_section") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    SectionHeaderRow(title = strings.recentlyPlayed)
                    recentByReciter.forEach { track ->
                        val isCurrent = playerState.currentEntry?.track?.id == track.id
                        TrackRowItem(
                            track = track,
                            isCurrentTrack = isCurrent,
                            isPlaying = isCurrent && playerState.isPlaying,
                            isFavorite = track.id in preferences.favoriteTrackIds,
                            onPlayClick = { onPlaySurahList(track, reciterTracks) },
                            onToggleFavorite = { onToggleFavoriteTrack(track.id) },
                            onAddToQueue = { onAddTrackToQueue(track) },
                            onAddToPlaylistClick = { onAddTrackToPlaylist(track) },
                            isOfflineSaved = track.id in savedOfflineTrackIds,
                            downloadProgress = activeDownloadProgress[track.id],
                            isDownloadPermitted = onIsDownloadPermitted(track),
                            onDownloadOfflineClick = { onDownloadTrackOffline(track) },
                            onRemoveOfflineClick = { onRemoveOfflineTrack(track.id) },
                            onShowLicenceInfo = onShowTrackLicence
                        )
                    }
                }
            }
        }

        item(key = "reciter_surah_filter") {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                SectionHeaderRow(
                    title = "Surahs (${filteredTracks.size})",
                    subtitle = "Complete chapter browser for ${reciter.displayName}"
                )
                OutlinedTextField(
                    value = surahFilter,
                    onValueChange = { surahFilter = it },
                    placeholder = { Text("Search surah by number, English name, or Arabic...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (surahFilter.isNotEmpty()) {
                            IconButton(onClick = { surahFilter = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear surah filter")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reciter_surah_search_input")
                )
            }
        }

        items(items = filteredTracks, key = { it.id }) { track ->
            val isCurrent = playerState.currentEntry?.track?.id == track.id
            TrackRowItem(
                track = track,
                isCurrentTrack = isCurrent,
                isPlaying = isCurrent && playerState.isPlaying,
                isFavorite = track.id in preferences.favoriteTrackIds,
                onPlayClick = { onPlaySurahList(track, filteredTracks.take(25)) },
                onToggleFavorite = { onToggleFavoriteTrack(track.id) },
                onAddToQueue = { onAddTrackToQueue(track) },
                onAddToPlaylistClick = { onAddTrackToPlaylist(track) },
                isOfflineSaved = track.id in savedOfflineTrackIds,
                downloadProgress = activeDownloadProgress[track.id],
                isDownloadPermitted = onIsDownloadPermitted(track),
                onDownloadOfflineClick = { onDownloadTrackOffline(track) },
                onRemoveOfflineClick = { onRemoveOfflineTrack(track.id) },
                onShowLicenceInfo = onShowTrackLicence
            )
        }
    }
}
