package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.data.model.CollectionCategory
import com.example.data.model.CuratedCollection
import com.example.data.model.ListeningEventRecord
import com.example.data.model.Reciter
import com.example.data.model.UserPreferencesState
import com.example.i18n.UiStrings
import com.example.player.GlobalPlayerState
import com.example.ui.components.AppFooterSection
import com.example.ui.components.CollectionCard
import com.example.ui.components.ReciterAvatar
import com.example.ui.components.ReciterCardItem
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.TajwidMasterCard
import com.example.ui.components.TrackRowItem
import com.example.ui.theme.QuranArabicFontFamily
import com.example.viewmodel.CatalogUiState

@Composable
fun HomeScreen(
    catalogState: CatalogUiState,
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    recentEvents: List<ListeningEventRecord>,
    isGuestMode: Boolean,
    authBannerDismissed: Boolean,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onOpenAccountModal: () -> Unit,
    onDismissAuthBanner: () -> Unit,
    onPlaySurahWithReciter: (reciterId: String, surahNumber: Int) -> Unit,
    onPlayReciterFeatured: (reciterId: String) -> Unit,
    onOpenReciter: (String) -> Unit,
    onOpenAllReciters: () -> Unit,
    onOpenCollection: (String) -> Unit,
    onPlayCollection: (CuratedCollection) -> Unit,
    onAddCollectionToQueue: (CuratedCollection) -> Unit,
    onOpenAllCollections: () -> Unit,
    onToggleSaveCollection: (String) -> Unit,
    onToggleFavoriteReciter: (String) -> Unit,
    onToggleFavoriteTrack: (String) -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onAddTrackToPlaylist: (AudioTrack) -> Unit,
    onResolveTracks: (List<String>) -> List<AudioTrack>,
    onOpenPremium: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefreshCatalog: () -> Unit,
    spokenSessions: List<AudioTrack> = emptyList(),
    spokenFilter: String = "all",
    savedOfflineTrackIds: Set<String> = emptySet(),
    activeDownloadProgress: Map<String, Float> = emptyMap(),
    onSelectSpokenFilter: (String) -> Unit = {},
    onPlaySpokenSession: (AudioTrack, List<AudioTrack>) -> Unit = { _, _ -> },
    onIsDownloadPermitted: (AudioTrack) -> Boolean = { false },
    onDownloadTrackOffline: (AudioTrack) -> Unit = {},
    onRemoveOfflineTrack: (String) -> Unit = {},
    onShowTrackLicence: (AudioTrack) -> Unit = {},
    selectedHeroReciterId: String = "yasser_dosari",
    onSelectHeroReciter: (String) -> Unit = {},
    onPlayHeroReciter: (String) -> Unit = onPlayReciterFeatured
) {
    val heroSwitcherReciters = remember(catalogState.reciters) {
        catalogState.reciters.filterNot { it.isTajwidFeatured }.take(5)
    }

    val heroReciter = remember(catalogState.reciters, selectedHeroReciterId) {
        catalogState.reciters.firstOrNull { it.id == selectedHeroReciterId }
            ?: catalogState.reciters.firstOrNull { it.isFeaturedHero }
            ?: catalogState.reciters.firstOrNull()
    }

    val coreCollections = remember(catalogState.collections) {
        catalogState.collections.filter { it.category != CollectionCategory.RECITER }
    }
    val reciterCollections = remember(catalogState.collections) {
        catalogState.collections.filter { it.category == CollectionCategory.RECITER }
    }
    val tajwidReciters = remember(catalogState.reciters) {
        catalogState.reciters.filter { it.isTajwidFeatured }
    }
    val popularReciters = remember(catalogState.reciters) {
        catalogState.reciters.filterNot { it.isTajwidFeatured }
    }
    val favoriteTracks = remember(preferences.favoriteTrackIds, catalogState.surahs) {
        onResolveTracks(preferences.favoriteTrackIds.take(4))
    }
    val distinctRecentTracks = remember(recentEvents, catalogState.surahs) {
        val ids = recentEvents.map { it.trackId }.distinct().take(4)
        onResolveTracks(ids)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth < 600.dp) {
            MobileHomeContent(
                catalogState = catalogState,
                preferences = preferences,
                playerState = playerState,
                heroReciter = heroReciter,
                coreCollections = coreCollections,
                tajwidReciters = tajwidReciters,
                distinctRecentTracks = distinctRecentTracks,
                favoriteTracks = favoriteTracks,
                isGuestMode = isGuestMode,
                authBannerDismissed = authBannerDismissed,
                strings = strings,
                bottomReservedPadding = bottomReservedPadding,
                onOpenAccountModal = onOpenAccountModal,
                onDismissAuthBanner = onDismissAuthBanner,
                onPlayHeroReciter = onPlayHeroReciter,
                onPlayReciterFeatured = onPlayReciterFeatured,
                onOpenReciter = onOpenReciter,
                onOpenAllReciters = onOpenAllReciters,
                onOpenCollection = onOpenCollection,
                onPlayCollection = onPlayCollection,
                onOpenAllCollections = onOpenAllCollections,
                onToggleFavoriteTrack = onToggleFavoriteTrack,
                onPlayTrack = onPlayTrack,
                onAddTrackToQueue = onAddTrackToQueue,
                onAddTrackToPlaylist = onAddTrackToPlaylist,
                onRefreshCatalog = onRefreshCatalog,
                spokenSessions = spokenSessions,
                spokenFilter = spokenFilter,
                savedOfflineTrackIds = savedOfflineTrackIds,
                activeDownloadProgress = activeDownloadProgress,
                onSelectSpokenFilter = onSelectSpokenFilter,
                onPlaySpokenSession = onPlaySpokenSession,
                onIsDownloadPermitted = onIsDownloadPermitted,
                onDownloadTrackOffline = onDownloadTrackOffline,
                onRemoveOfflineTrack = onRemoveOfflineTrack,
                onShowTrackLicence = onShowTrackLicence
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_screen_list"),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = bottomReservedPadding + 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
        // Offline status banner if live catalog had to use verified fallback
        if (catalogState.catalogStatusBanner != null) {
            item(key = "catalog_banner") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = catalogState.catalogStatusBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRefreshCatalog) {
                            Text(strings.retry)
                        }
                    }
                }
            }
        }

        // Compact Dismissible Guest Cloud Sync Strip
        if (isGuestMode && !authBannerDismissed) {
            item(key = "guest_sync_banner") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_sync_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Guest Mode • Sign in to sync favorites & playlists",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenAccountModal,
                            shape = CircleShape,
                            modifier = Modifier.testTag("banner_sign_in_button")
                        ) {
                            Text("Sign In", style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(
                            onClick = onDismissAuthBanner,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dismiss_guest_banner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss sync reminder",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 1. "Your Reciter" Featured Split Hero (Portrait + Switcher + 3-Surah Tracklist)
        if (heroReciter != null) {
            item(key = "hero_your_reciter") {
                YourReciterHeroCard(
                    reciter = heroReciter,
                    switcherReciters = heroSwitcherReciters,
                    playerState = playerState,
                    strings = strings,
                    onSelectHeroReciter = onSelectHeroReciter,
                    onPlayMain = { onPlayReciterFeatured(heroReciter.id) },
                    onViewReciter = { onOpenReciter(heroReciter.id) },
                    onPlayQuickSurah = { surahNum ->
                        onPlaySurahWithReciter(heroReciter.id, surahNum)
                    }
                )
            }
        }

        // 2. Personalized Continue Listening / Recently Played
        if (distinctRecentTracks.isNotEmpty()) {
            item(key = "continue_listening_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeaderRow(
                        title = strings.continueListening,
                        subtitle = strings.recentlyPlayed
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            distinctRecentTracks.forEach { track ->
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
                }
            }
        }

        // 3. Curated Collections (True 1:1 Square Album Proportions)
        item(key = "curated_collections_section") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionHeaderRow(
                    title = strings.collectionsTitle,
                    subtitle = strings.collectionsSubtitle,
                    actionLabel = "See All",
                    onActionClick = onOpenAllCollections
                )

                ResponsiveCollectionsGrid(
                    collections = coreCollections,
                    savedCollectionIds = preferences.savedCollectionIds,
                    onOpenCollection = onOpenCollection,
                    onPlayCollection = onPlayCollection,
                    onToggleSaveCollection = onToggleSaveCollection,
                    onAddCollectionToQueue = onAddCollectionToQueue
                )
            }
        }

        // 4. Popular Reciters (Centered Circular Portrait Cards)
        item(key = "popular_reciters_section") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionHeaderRow(
                    title = strings.popularReciters,
                    subtitle = "World-renowned Haramain and studio voices in complete 114-chapter editions.",
                    actionLabel = "See All",
                    onActionClick = onOpenAllReciters
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(items = popularReciters, key = { it.id }) { reciter ->
                        ReciterCardItem(
                            reciter = reciter,
                            isFavorite = reciter.id in preferences.favoriteReciterIds,
                            onOpenProfile = { onOpenReciter(reciter.id) },
                            onPlayReciter = { onPlayReciterFeatured(reciter.id) },
                            onToggleFavorite = { onToggleFavoriteReciter(reciter.id) },
                            modifier = Modifier.width(176.dp)
                        )
                    }
                }
            }
        }

        // 5. "Best for Tajwid" Editorial Section (Structured Master Cards)
        if (tajwidReciters.isNotEmpty()) {
            item(key = "best_for_tajwid_section") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeaderRow(
                        title = strings.bestForTajwid,
                        subtitle = "Measured, classical articulation suited for attentive reflection, memorization, and Tajwid study.",
                        actionLabel = "All Reciters",
                        onActionClick = onOpenAllReciters
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.testTag("tajwid_reciters_row")
                    ) {
                        items(items = tajwidReciters, key = { it.id }) { reciter ->
                            TajwidMasterCard(
                                reciter = reciter,
                                isFavorite = reciter.id in preferences.favoriteReciterIds,
                                onOpenProfile = { onOpenReciter(reciter.id) },
                                onPlayReciter = { onPlayReciterFeatured(reciter.id) },
                                onToggleFavorite = { onToggleFavoriteReciter(reciter.id) },
                                modifier = Modifier.width(316.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Reciter-Specific Curated Sets
        if (reciterCollections.isNotEmpty()) {
            item(key = "reciter_collections_section") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeaderRow(
                        title = strings.reciterCollections,
                        subtitle = "Signature chapter selections from Yasser Al-Dosari, Muhammad Al-Luhaidan, and Abdul Rahman Mossad."
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(items = reciterCollections, key = { it.id }) { col ->
                            CollectionCard(
                                collection = col,
                                isSaved = col.id in preferences.savedCollectionIds,
                                onOpenCollection = { onOpenCollection(col.id) },
                                onPlayCollection = { onPlayCollection(col) },
                                onToggleSave = { onToggleSaveCollection(col.id) },
                                onAddToQueue = { onAddCollectionToQueue(col) },
                                modifier = Modifier.width(210.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Dedicated Spoken Adhkar, Duas & Ruqya Voice Section
        if (spokenSessions.isNotEmpty()) {
            item(key = "spoken_adhkar_ruqya_section") {
                SpokenIslamicAudioSection(
                    sessions = spokenSessions,
                    selectedFilter = spokenFilter,
                    playerState = playerState,
                    onSelectFilter = onSelectSpokenFilter,
                    onPlaySession = onPlaySpokenSession,
                    onShowLicenceInfo = onShowTrackLicence
                )
            }
        }

        // 8. Your Favorites (shown when user has saved surahs)
        if (favoriteTracks.isNotEmpty()) {
            item(key = "home_favorites_section") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeaderRow(title = strings.yourFavorites)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            favoriteTracks.forEach { track ->
                                val isCurrent = playerState.currentEntry?.track?.id == track.id
                                TrackRowItem(
                                    track = track,
                                    isCurrentTrack = isCurrent,
                                    isPlaying = isCurrent && playerState.isPlaying,
                                    isFavorite = true,
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
                }
            }
        }

        // 9. Minimalist Responsive Footer
        item(key = "home_footer") {
            AppFooterSection(
                onOpenPremium = onOpenPremium,
                onOpenSettings = onOpenSettings
            )
        }
            }
        }
    }
}

private data class HeroQuickSurah(
    val number: Int,
    val latinTitle: String,
    val arabicTitle: String,
    val ayahCount: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YourReciterHeroCard(
    reciter: Reciter,
    switcherReciters: List<Reciter>,
    playerState: GlobalPlayerState,
    strings: UiStrings,
    onSelectHeroReciter: (String) -> Unit,
    onPlayMain: () -> Unit,
    onViewReciter: () -> Unit,
    onPlayQuickSurah: (Int) -> Unit
) {
    val quickSurahs = remember {
        listOf(
            HeroQuickSurah(1, "Al-Fatihah", "الفاتحة", 7),
            HeroQuickSurah(2, "Al-Baqarah", "البقرة", 286),
            HeroQuickSurah(3, "Ali 'Imran", "آل عمران", 200)
        )
    }

    val isHeroReciterPlaying = playerState.currentEntry?.track?.reciterId == reciter.id && playerState.isPlaying
    val reciterAccent = Color(reciter.avatarAccentHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("your_reciter_hero_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            reciterAccent.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isSplitDesktop = maxWidth >= 660.dp

                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    // Header row: YOUR RECITER label + quick reciter switcher pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.yourReciter.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (switcherReciters.size > 1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                items(items = switcherReciters, key = { it.id }) { candidate ->
                                    val selected = candidate.id == reciter.id
                                    val shortLabel = candidate.displayName.split(" ").lastOrNull() ?: candidate.displayName
                                    Surface(
                                        shape = CircleShape,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .clip(CircleShape)
                                            .clickable { onSelectHeroReciter(candidate.id) }
                                            .testTag("hero_switcher_${candidate.id}")
                                    ) {
                                        Text(
                                            text = shortLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isSplitDesktop) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Column: Portrait + Identity + Actions
                            Column(
                                modifier = Modifier.weight(0.52f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HeroReciterIdentityBlock(
                                    reciter = reciter,
                                    portraitSize = 104.dp
                                )
                                HeroActionButtonsRow(
                                    isHeroReciterPlaying = isHeroReciterPlaying,
                                    strings = strings,
                                    onPlayMain = onPlayMain,
                                    onViewReciter = onViewReciter
                                )
                            }

                            // Right Column: Structured 3-row Quick-Access Surah Tracklist
                            HeroQuickSurahList(
                                reciterId = reciter.id,
                                quickSurahs = quickSurahs,
                                playerState = playerState,
                                onPlayQuickSurah = onPlayQuickSurah,
                                modifier = Modifier.weight(0.48f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            HeroReciterIdentityBlock(
                                reciter = reciter,
                                portraitSize = 82.dp
                            )
                            HeroActionButtonsRow(
                                isHeroReciterPlaying = isHeroReciterPlaying,
                                strings = strings,
                                onPlayMain = onPlayMain,
                                onViewReciter = onViewReciter
                            )
                            HeroQuickSurahList(
                                reciterId = reciter.id,
                                quickSurahs = quickSurahs,
                                playerState = playerState,
                                onPlayQuickSurah = onPlayQuickSurah,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroReciterIdentityBlock(
    reciter: Reciter,
    portraitSize: Dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReciterAvatar(
            reciterName = reciter.displayName,
            accentHex = reciter.avatarAccentHex,
            arabicName = reciter.arabicName,
            size = portraitSize,
            shape = RoundedCornerShape(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reciter.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!reciter.arabicName.isNullOrBlank()) {
                Text(
                    text = reciter.arabicName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = QuranArabicFontFamily,
                        fontSize = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = listOfNotNull(reciter.country, reciter.recitationStyle, reciter.riwayah).joinToString(" • "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = reciter.neutralDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroActionButtonsRow(
    isHeroReciterPlaying: Boolean,
    strings: UiStrings,
    onPlayMain: () -> Unit,
    onViewReciter: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onPlayMain,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .height(48.dp)
                .testTag("hero_play_button")
        ) {
            Icon(
                imageVector = if (isHeroReciterPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isHeroReciterPlaying) strings.nowPlaying else strings.play,
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onViewReciter,
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .height(48.dp)
                .testTag("hero_view_reciter_button")
        ) {
            Text(strings.viewReciter)
        }
    }
}

@Composable
private fun HeroQuickSurahList(
    reciterId: String,
    quickSurahs: List<HeroQuickSurah>,
    playerState: GlobalPlayerState,
    onPlayQuickSurah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            quickSurahs.forEachIndexed { idx, surah ->
                val isThisSurahCurrent = playerState.currentEntry?.track?.let {
                    it.reciterId == reciterId && it.surahNumber == surah.number
                } == true
                val isThisSurahPlaying = isThisSurahCurrent && playerState.isPlaying

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onPlayQuickSurah(surah.number) }
                        .background(
                            if (isThisSurahCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("hero_quick_surah_${surah.number}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isThisSurahCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThisSurahPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isThisSurahPlaying) "Pause ${surah.latinTitle}" else "Play ${surah.latinTitle}",
                            tint = if (isThisSurahCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${surah.number}. ${surah.latinTitle}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isThisSurahCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${surah.ayahCount} ayahs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = surah.arabicTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = QuranArabicFontFamily,
                            fontSize = 18.sp
                        ),
                        color = if (isThisSurahCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (idx < quickSurahs.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ResponsiveCollectionsGrid(
    collections: List<CuratedCollection>,
    savedCollectionIds: Set<String>,
    onOpenCollection: (String) -> Unit,
    onPlayCollection: (CuratedCollection) -> Unit,
    onToggleSaveCollection: (String) -> Unit,
    onAddCollectionToQueue: (CuratedCollection) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 960.dp -> 4
            maxWidth >= 660.dp -> 3
            maxWidth >= 330.dp -> 2
            else -> 1
        }
        val rows = remember(collections, columns) { collections.chunked(columns) }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowItems.forEach { collection ->
                        CollectionCard(
                            collection = collection,
                            isSaved = collection.id in savedCollectionIds,
                            onOpenCollection = { onOpenCollection(collection.id) },
                            onPlayCollection = { onPlayCollection(collection) },
                            onToggleSave = { onToggleSaveCollection(collection.id) },
                            onAddToQueue = { onAddCollectionToQueue(collection) },
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
