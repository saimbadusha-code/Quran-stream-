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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AudioTrack
import com.example.data.model.CuratedCollection
import com.example.data.model.UserPreferencesState
import com.example.i18n.UiStrings
import com.example.player.GlobalPlayerState
import com.example.ui.components.CollectionCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ReciterCardItem
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.TrackRowItem
import com.example.viewmodel.SearchResultsState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchState: SearchResultsState,
    defaultReciterId: String,
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onQueryChange: (String) -> Unit,
    onCommitRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onSelectTab: (String) -> Unit,
    onBuildTrackForSurah: (String, Int) -> AudioTrack,
    onPlayTrack: (AudioTrack) -> Unit,
    onToggleFavoriteTrack: (String) -> Unit,
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onAddTrackToPlaylist: (AudioTrack) -> Unit,
    onOpenReciter: (String) -> Unit,
    onPlayReciterFeatured: (String) -> Unit,
    onToggleFavoriteReciter: (String) -> Unit,
    onOpenCollection: (String) -> Unit,
    onPlayCollection: (CuratedCollection) -> Unit,
    onToggleSaveCollection: (String) -> Unit,
    onAddCollectionToQueue: (CuratedCollection) -> Unit,
    savedOfflineTrackIds: Set<String> = emptySet(),
    activeDownloadProgress: Map<String, Float> = emptyMap(),
    onIsDownloadPermitted: (AudioTrack) -> Boolean = { false },
    onDownloadTrackOffline: (AudioTrack) -> Unit = {},
    onRemoveOfflineTrack: (String) -> Unit = {},
    onShowTrackLicence: (AudioTrack) -> Unit = {}
) {
    val suggestedQueries = remember {
        listOf("Al-Fatihah", "Al-Kahf", "Ya-Sin", "Al-Mulk", "Al-Husary", "Yasser Al-Dosari", "Sleep")
    }

    val trimmedQuery = searchState.query.trim()
    val showSurahs = searchState.selectedTab in listOf("all", "surahs")
    val showReciters = searchState.selectedTab in listOf("all", "reciters")
    val showCollections = searchState.selectedTab in listOf("all", "collections")
    val showSpoken = searchState.selectedTab in listOf("all", "spoken")

    val hasAnyResults = (showSurahs && searchState.matchingSurahs.isNotEmpty()) ||
        (showReciters && searchState.matchingReciters.isNotEmpty()) ||
        (showCollections && searchState.matchingCollections.isNotEmpty()) ||
        (showSpoken && searchState.matchingSpokenSessions.isNotEmpty())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "search_header") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = strings.navSearch,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = searchState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchState.query.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.testTag("search_clear_button")
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (trimmedQuery.isNotEmpty()) {
                                onCommitRecentSearch(trimmedQuery)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "all" to strings.filterAll,
                        "surahs" to "Surahs",
                        "reciters" to strings.navReciters,
                        "collections" to strings.navCollections,
                        "spoken" to "Adhkar & Ruqya"
                    ).forEach { (tabId, label) ->
                        FilterChip(
                            selected = searchState.selectedTab == tabId,
                            onClick = { onSelectTab(tabId) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("search_tab_$tabId")
                        )
                    }
                }
            }
        }

        if (trimmedQuery.isEmpty()) {
            if (searchState.recentSearches.isNotEmpty()) {
                item(key = "recent_searches_section") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = onClearRecentSearches,
                                modifier = Modifier.testTag("clear_recent_searches_button")
                            ) {
                                Text("Clear")
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchState.recentSearches.forEach { term ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onQueryChange(term) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(term, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "suggested_searches_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeaderRow(
                        title = "Suggested Searches",
                        subtitle = "Try searching normalized Latin titles (Fatiha, Baqara, Yasin), Arabic Surah names, or reciters."
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestedQueries.forEach { suggestion ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onQueryChange(suggestion)
                                    onCommitRecentSearch(suggestion)
                                },
                                label = { Text(suggestion) },
                                modifier = Modifier.testTag("suggested_search_${suggestion.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
            }
        } else if (!hasAnyResults) {
            item(key = "search_no_results") {
                EmptyStateCard(
                    title = strings.emptyResults,
                    subtitle = "No Surahs, Reciters, or Collections matched \"$trimmedQuery\".",
                    actionLabel = "Clear Search",
                    onActionClick = { onQueryChange("") }
                )
            }
        } else {
            if (showReciters && searchState.matchingReciters.isNotEmpty()) {
                item(key = "search_reciter_results") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeaderRow(title = "${strings.navReciters} (${searchState.matchingReciters.size})")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(items = searchState.matchingReciters, key = { it.id }) { reciter ->
                                ReciterCardItem(
                                    reciter = reciter,
                                    isFavorite = reciter.id in preferences.favoriteReciterIds,
                                    onOpenProfile = {
                                        onCommitRecentSearch(trimmedQuery)
                                        onOpenReciter(reciter.id)
                                    },
                                    onPlayReciter = {
                                        onCommitRecentSearch(trimmedQuery)
                                        onPlayReciterFeatured(reciter.id)
                                    },
                                    onToggleFavorite = { onToggleFavoriteReciter(reciter.id) },
                                    modifier = Modifier.width(248.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showCollections && searchState.matchingCollections.isNotEmpty()) {
                item(key = "search_collection_results") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeaderRow(title = "${strings.navCollections} (${searchState.matchingCollections.size})")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(items = searchState.matchingCollections, key = { it.id }) { col ->
                                CollectionCard(
                                    collection = col,
                                    isSaved = col.id in preferences.savedCollectionIds,
                                    onOpenCollection = {
                                        onCommitRecentSearch(trimmedQuery)
                                        onOpenCollection(col.id)
                                    },
                                    onPlayCollection = {
                                        onCommitRecentSearch(trimmedQuery)
                                        onPlayCollection(col)
                                    },
                                    onToggleSave = { onToggleSaveCollection(col.id) },
                                    onAddToQueue = { onAddCollectionToQueue(col) },
                                    modifier = Modifier.width(244.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (showSurahs && searchState.matchingSurahs.isNotEmpty()) {
                item(key = "search_surah_header") {
                    SectionHeaderRow(title = "Surahs (${searchState.matchingSurahs.size})")
                }
                items(
                    items = searchState.matchingSurahs.take(40),
                    key = { it.id }
                ) { surah ->
                    val track = onBuildTrackForSurah(defaultReciterId, surah.number)
                    val isCurrent = playerState.currentEntry?.track?.id == track.id
                    TrackRowItem(
                        track = track,
                        isCurrentTrack = isCurrent,
                        isPlaying = isCurrent && playerState.isPlaying,
                        isFavorite = track.id in preferences.favoriteTrackIds,
                        onPlayClick = {
                            onCommitRecentSearch(trimmedQuery)
                            onPlayTrack(track)
                        },
                        onToggleFavorite = { onToggleFavoriteTrack(track.id) },
                        onAddToQueue = { onAddTrackToQueue(track) },
                        onAddToPlaylistClick = { onAddTrackToPlaylist(track) },
                        onViewReciter = { onOpenReciter(track.reciterId) },
                        isOfflineSaved = track.id in savedOfflineTrackIds,
                        downloadProgress = activeDownloadProgress[track.id],
                        isDownloadPermitted = onIsDownloadPermitted(track),
                        onDownloadOfflineClick = { onDownloadTrackOffline(track) },
                        onRemoveOfflineClick = { onRemoveOfflineTrack(track.id) },
                        onShowLicenceInfo = onShowTrackLicence
                    )
                }
            }

            if (showSpoken && searchState.matchingSpokenSessions.isNotEmpty()) {
                item(key = "search_spoken_header") {
                    SectionHeaderRow(title = "Adhkar, Duas & Ruqya (${searchState.matchingSpokenSessions.size})")
                }
                items(
                    items = searchState.matchingSpokenSessions,
                    key = { "spoken_${it.id}" }
                ) { session ->
                    val isCurrent = playerState.currentEntry?.track?.id == session.id
                    TrackRowItem(
                        track = session,
                        isCurrentTrack = isCurrent,
                        isPlaying = isCurrent && playerState.isPlaying,
                        isFavorite = session.id in preferences.favoriteTrackIds,
                        onPlayClick = {
                            onCommitRecentSearch(trimmedQuery)
                            onPlayTrack(session)
                        },
                        onToggleFavorite = { onToggleFavoriteTrack(session.id) },
                        onAddToQueue = { onAddTrackToQueue(session) },
                        onAddToPlaylistClick = { onAddTrackToPlaylist(session) },
                        onViewReciter = { onOpenReciter(session.reciterId) },
                        isOfflineSaved = session.id in savedOfflineTrackIds,
                        downloadProgress = activeDownloadProgress[session.id],
                        isDownloadPermitted = onIsDownloadPermitted(session),
                        onDownloadOfflineClick = { onDownloadTrackOffline(session) },
                        onRemoveOfflineClick = { onRemoveOfflineTrack(session.id) },
                        onShowLicenceInfo = onShowTrackLicence
                    )
                }
            }
        }
    }
}
