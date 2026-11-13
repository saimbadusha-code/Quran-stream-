package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.data.model.CollectionCategory
import com.example.data.model.CuratedCollection
import com.example.data.model.UserPreferencesState
import com.example.data.remote.QuranCatalogRepository
import com.example.i18n.UiStrings
import com.example.player.GlobalPlayerState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.TrackRowItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionsOverviewScreen(
    collections: List<CuratedCollection>,
    selectedCategory: CollectionCategory,
    searchQuery: String,
    preferences: UserPreferencesState,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onSelectCategory: (CollectionCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenCollection: (String) -> Unit,
    onPlayCollection: (CuratedCollection) -> Unit,
    onToggleSaveCollection: (String) -> Unit,
    onAddCollectionToQueue: (CuratedCollection) -> Unit
) {
    var sortOption by remember { mutableStateOf("Featured") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val filteredCollections = remember(collections, selectedCategory, searchQuery, preferences.savedCollectionIds, sortOption) {
        val byCategory = collections.filter { col ->
            when (selectedCategory) {
                CollectionCategory.ALL -> true
                CollectionCategory.FAVORITES -> col.id in preferences.savedCollectionIds
                else -> col.category == selectedCategory
            }
        }
        val byQuery = if (searchQuery.isBlank()) {
            byCategory
        } else {
            byCategory.filter { col ->
                QuranCatalogRepository.matchesSearchQuery(col.title, searchQuery) ||
                    QuranCatalogRepository.matchesSearchQuery(col.description, searchQuery)
            }
        }
        when (sortOption) {
            "Alphabetical" -> byQuery.sortedBy { it.title }
            "Track Count" -> byQuery.sortedByDescending { it.trackIds.size }
            else -> byQuery
        }
    }

    val featuredBannerCollection = filteredCollections.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("collections_overview_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "collections_header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.collectionsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.collectionsSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "collections_search_and_sort") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(strings.searchCollectionsPlaceholder) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear filter")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("collections_search_input")
                )

                Box {
                    OutlinedButton(
                        onClick = { sortMenuExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("collections_sort_button")
                    ) {
                        Text("Sort: $sortOption")
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        listOf("Featured", "Alphabetical", "Track Count").forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = {
                                    sortOption = mode
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item(key = "collections_category_chips") {
            val categories = listOf(
                CollectionCategory.ALL to strings.filterAll,
                CollectionCategory.FOCUS_WORK to strings.filterFocus,
                CollectionCategory.SLEEP to strings.filterSleep,
                CollectionCategory.DUAA_RUQIA to strings.filterDuaa,
                CollectionCategory.EMOTIONAL to strings.filterEmotional,
                CollectionCategory.RECITER to strings.filterReciterCols,
                CollectionCategory.FAVORITES to strings.filterFavorites
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (cat, label) ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("collection_chip_${cat.id}")
                    )
                }
            }
        }

        if (featuredBannerCollection != null && searchQuery.isBlank() && selectedCategory == CollectionCategory.ALL) {
            item(key = "featured_collection_banner") {
                FeaturedCollectionHero(
                    collection = featuredBannerCollection,
                    strings = strings,
                    onOpen = { onOpenCollection(featuredBannerCollection.id) },
                    onPlay = { onPlayCollection(featuredBannerCollection) }
                )
            }
        }

        if (filteredCollections.isEmpty()) {
            item(key = "collections_empty") {
                EmptyStateCard(
                    title = strings.emptyResults,
                    subtitle = "No collections match the selected filter or search text.",
                    actionLabel = "Reset Filters",
                    onActionClick = {
                        onSearchQueryChange("")
                        onSelectCategory(CollectionCategory.ALL)
                    }
                )
            }
        } else {
            item(key = "collections_grid") {
                ResponsiveCollectionsGrid(
                    collections = filteredCollections,
                    savedCollectionIds = preferences.savedCollectionIds,
                    onOpenCollection = onOpenCollection,
                    onPlayCollection = onPlayCollection,
                    onToggleSaveCollection = onToggleSaveCollection,
                    onAddCollectionToQueue = onAddCollectionToQueue
                )
            }
        }
    }
}

@Composable
private fun FeaturedCollectionHero(
    collection: CuratedCollection,
    strings: UiStrings,
    onOpen: () -> Unit,
    onPlay: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(206.dp)
        ) {
            Image(
                painter = painterResource(id = collection.coverResId),
                contentDescription = collection.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xF009090B),
                                Color(0xAA09090B)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "FEATURED COLLECTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFAFAFA)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA1A1AA),
                        maxLines = 2
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPlay,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.playAll, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onOpen,
                        shape = CircleShape,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Open (${collection.trackIds.size} Surahs)")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionDetailScreen(
    collection: CuratedCollection?,
    tracks: List<AudioTrack>,
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    onToggleSaveCollection: () -> Unit,
    onAddCollectionToQueue: () -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onToggleFavoriteTrack: (String) -> Unit,
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onAddTrackToPlaylist: (AudioTrack) -> Unit,
    onOpenReciter: (String) -> Unit,
    savedOfflineTrackIds: Set<String> = emptySet(),
    activeDownloadProgress: Map<String, Float> = emptyMap(),
    onIsDownloadPermitted: (AudioTrack) -> Boolean = { false },
    onDownloadTrackOffline: (AudioTrack) -> Unit = {},
    onRemoveOfflineTrack: (String) -> Unit = {},
    onShowTrackLicence: (AudioTrack) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    if (collection == null) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            EmptyStateCard(
                title = "Collection Not Found",
                subtitle = "The requested collection is not available.",
                actionLabel = "Back to Collections",
                onActionClick = onBack
            )
        }
        return
    }

    val isSaved = collection.id in preferences.savedCollectionIds

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("collection_detail_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = bottomReservedPadding + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(key = "back_row") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("collection_detail_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = strings.navCollections,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "collection_hero_header") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.padding(20.dp)) {
                    val isWide = maxWidth >= 600.dp
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = if (isWide) Alignment.CenterVertically else Alignment.Top
                        ) {
                            Image(
                                painter = painterResource(id = collection.coverResId),
                                contentDescription = collection.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(if (isWide) 164.dp else 112.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = collection.provenance.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = collection.title,
                                    style = if (isWide) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = collection.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${tracks.size} surahs curated",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onPlayAll,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("collection_detail_play_all")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.playAll, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = onShufflePlay,
                                shape = CircleShape,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("collection_detail_shuffle")
                            ) {
                                Icon(Icons.Filled.Shuffle, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.shufflePlay)
                            }
                            OutlinedButton(
                                onClick = onToggleSaveCollection,
                                shape = CircleShape,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("collection_detail_save")
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isSaved) "Saved" else "Save")
                            }
                            OutlinedButton(
                                onClick = onAddCollectionToQueue,
                                shape = CircleShape,
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Queue All")
                            }
                        }
                    }
                }
            }
        }

        items(items = tracks, key = { it.id }) { track ->
            val isCurrent = playerState.currentEntry?.track?.id == track.id
            TrackRowItem(
                track = track,
                isCurrentTrack = isCurrent,
                isPlaying = isCurrent && playerState.isPlaying,
                isFavorite = track.id in preferences.favoriteTrackIds,
                onPlayClick = { onPlayTrack(track) },
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
}
