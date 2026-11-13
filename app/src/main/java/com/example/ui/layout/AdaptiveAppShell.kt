package com.example.ui.layout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.OfflineDownloadsUiState
import com.example.data.model.AppLanguage
import com.example.data.model.AudioTrack
import com.example.i18n.LocalizationProvider
import com.example.i18n.UiStrings
import com.example.ui.components.QuranWaveSymbol
import com.example.ui.components.TilawahLogoMark
import com.example.ui.navigation.AppDestination
import com.example.ui.navigation.mobileBottomHighlightKey
import com.example.ui.navigation.topLevelHighlightKey
import com.example.ui.player.AmbienceMixerDialog
import com.example.ui.player.DesktopBottomPlayerBar
import com.example.ui.player.MobileMiniPlayerBar
import com.example.ui.player.NowPlayingFullScreenOverlay
import com.example.ui.player.QueueDialog
import com.example.ui.player.SleepTimerDialog
import com.example.ui.screens.AccountManagementDialog
import com.example.ui.screens.AddToPlaylistDialog
import com.example.ui.screens.AudioSourcesAndAttributionDialog
import com.example.ui.screens.CollectionDetailScreen
import com.example.ui.screens.CollectionsOverviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ReciterDetailScreen
import com.example.ui.screens.RecitersOverviewScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrackLicenceInfoDialog
import com.example.viewmodel.TilawahViewModel
import com.google.firebase.auth.FirebaseUser

private data class NavEntry(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val destination: AppDestination
)

@Composable
fun AdaptiveAppShell(
    viewModel: TilawahViewModel,
    currentUser: FirebaseUser?,
    onSignedOutToWelcome: () -> Unit
) {
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val listeningEvents by viewModel.listeningEvents.collectAsStateWithLifecycle()
    val authBannerDismissed by viewModel.authBannerDismissed.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val ambientState by viewModel.ambientState.collectAsStateWithLifecycle()
    val scriptureState by viewModel.scriptureState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val insightsSummary by viewModel.insightsSummary.collectAsStateWithLifecycle()
    val collectionFilter by viewModel.collectionFilter.collectAsStateWithLifecycle()
    val collectionSearchQuery by viewModel.collectionSearchQuery.collectAsStateWithLifecycle()
    val reciterSearchQuery by viewModel.reciterSearchQuery.collectAsStateWithLifecycle()
    val reciterCountryFilter by viewModel.reciterCountryFilter.collectAsStateWithLifecycle()
    val reciterSortMode by viewModel.reciterSortMode.collectAsStateWithLifecycle()
    val spokenFilter by viewModel.spokenFilter.collectAsStateWithLifecycle()
    val selectedHeroReciterId by viewModel.selectedHeroReciterId.collectAsStateWithLifecycle()
    val offlineState by viewModel.offlineState.collectAsStateWithLifecycle()

    val strings = remember(preferences.preferredLanguage) {
        LocalizationProvider.strings(preferences.preferredLanguage)
    }

    val navigationBackStack = remember { mutableStateListOf<AppDestination>(AppDestination.Home) }
    val currentDestination = navigationBackStack.last()

    val navigateTo: (AppDestination) -> Unit = { dest ->
        if (dest != navigationBackStack.lastOrNull()) {
            if (dest is AppDestination.Home) {
                navigationBackStack.clear()
                navigationBackStack.add(AppDestination.Home)
            } else {
                val isDetail = dest is AppDestination.CollectionDetail || dest is AppDestination.ReciterDetail
                val existingIdx = if (!isDetail) navigationBackStack.indexOfFirst { it == dest } else -1
                if (existingIdx >= 0) {
                    while (navigationBackStack.size > existingIdx + 1) {
                        navigationBackStack.removeAt(navigationBackStack.lastIndex)
                    }
                } else {
                    navigationBackStack.add(dest)
                }
            }
        }
    }

    val navigateBack: () -> Unit = {
        if (navigationBackStack.size > 1) {
            navigationBackStack.removeAt(navigationBackStack.lastIndex)
        }
    }

    var showNowPlayingOverlay by remember { mutableStateOf(false) }
    var openNowPlayingInReadMode by remember { mutableStateOf(false) }
    var showQueueDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showAmbienceDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showSourcesAttributionDialog by remember { mutableStateOf(false) }
    var pendingPlaylistTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var inspectingLicenceTrack by remember { mutableStateOf<AudioTrack?>(null) }

    BackHandler(enabled = showNowPlayingOverlay || navigationBackStack.size > 1) {
        if (showNowPlayingOverlay) {
            showNowPlayingOverlay = false
            openNowPlayingInReadMode = false
        } else {
            navigateBack()
        }
    }

    val activeHighlightKey = currentDestination.topLevelHighlightKey()
    val mobileActiveKey = currentDestination.mobileBottomHighlightKey()
    val activeTrack = playerState.currentEntry?.track
    val isActiveFavorite = activeTrack != null && activeTrack.id in preferences.favoriteTrackIds

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("adaptive_app_shell"),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedDesktop = maxWidth >= 960.dp
            val isMediumTablet = maxWidth in 600.dp..959.99.dp

            val primaryNavItems = remember(strings) {
                listOf(
                    NavEntry("home", strings.navHome, Icons.Filled.Home, AppDestination.Home),
                    NavEntry("collections", strings.navCollections, Icons.Filled.CollectionsBookmark, AppDestination.Collections),
                    NavEntry("reciters", strings.navReciters, Icons.Filled.RecordVoiceOver, AppDestination.Reciters),
                    NavEntry("search", strings.navSearch, Icons.Filled.Search, AppDestination.Search),
                    NavEntry("library", strings.navLibrary, Icons.Filled.LibraryMusic, AppDestination.Library)
                )
            }

            if (isExpandedDesktop || isMediumTablet) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        if (isExpandedDesktop) {
                            DesktopSidebar(
                                navItems = primaryNavItems,
                                activeKey = activeHighlightKey,
                                strings = strings,
                                currentUser = currentUser,
                                isDemoPremium = preferences.isDemoPremium,
                                onNavigate = navigateTo,
                                onOpenAccount = { showAccountDialog = true }
                            )
                            VerticalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            TabletNavigationRail(
                                navItems = primaryNavItems,
                                activeKey = activeHighlightKey,
                                strings = strings,
                                onNavigate = navigateTo
                            )
                            VerticalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            TopUtilityHeader(
                                strings = strings,
                                currentLanguage = preferences.preferredLanguage,
                                currentUser = currentUser,
                                showLogo = !isExpandedDesktop,
                                isDesktopWidth = isExpandedDesktop,
                                onBrandClick = { navigateTo(AppDestination.Home) },
                                onOpenSearch = { navigateTo(AppDestination.Search) },
                                onSelectLanguage = viewModel::setLanguage,
                                onOpenSettings = { navigateTo(AppDestination.Settings) },
                                onOpenAccount = { showAccountDialog = true }
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                RouteHostContent(
                                    destination = currentDestination,
                                    viewModel = viewModel,
                                    catalogState = catalogState,
                                    preferences = preferences,
                                    playlists = playlists,
                                    listeningEvents = listeningEvents,
                                    authBannerDismissed = authBannerDismissed,
                                    playerState = playerState,
                                    ambientState = ambientState,
                                    searchState = searchState,
                                    insightsSummary = insightsSummary,
                                    collectionFilter = collectionFilter,
                                    collectionSearchQuery = collectionSearchQuery,
                                    reciterSearchQuery = reciterSearchQuery,
                                    reciterCountryFilter = reciterCountryFilter,
                                    reciterSortMode = reciterSortMode,
                                    spokenFilter = spokenFilter,
                                    selectedHeroReciterId = selectedHeroReciterId,
                                    offlineState = offlineState,
                                    currentUser = currentUser,
                                    strings = strings,
                                    bottomReservedPadding = 16.dp,
                                    onNavigate = navigateTo,
                                    onNavigateBack = navigateBack,
                                    onOpenAccountModal = { showAccountDialog = true },
                                    onOpenAddToPlaylist = { pendingPlaylistTrack = it },
                                    onOpenAmbienceModal = { showAmbienceDialog = true },
                                    onOpenSleepTimerModal = { showSleepTimerDialog = true },
                                    onShowTrackLicence = { inspectingLicenceTrack = it },
                                    onOpenSourcesAttributionModal = { showSourcesAttributionDialog = true }
                                )
                            }
                        }
                    }

                    DesktopBottomPlayerBar(
                        playerState = playerState,
                        ambientState = ambientState,
                        isFavorite = isActiveFavorite,
                        isCompactTablet = isMediumTablet,
                        onTogglePlayPause = viewModel.audioEngine::togglePlayPause,
                        onPrevious = viewModel.audioEngine::skipPrevious,
                        onNext = { viewModel.audioEngine.skipNext(false) },
                        onToggleShuffle = viewModel.audioEngine::toggleShuffle,
                        onCycleRepeat = viewModel.audioEngine::cycleRepeatMode,
                        onSeekTo = viewModel.audioEngine::seekTo,
                        onToggleFavorite = {
                            activeTrack?.let { viewModel.toggleFavoriteTrack(it.id) }
                        },
                        onVolumeChange = viewModel.audioEngine::setVolume,
                        onToggleMute = viewModel.audioEngine::toggleMute,
                        onOpenQueue = { showQueueDialog = true },
                        onOpenSleepTimer = { showSleepTimerDialog = true },
                        onOpenAmbience = { showAmbienceDialog = true },
                        onExpandNowPlaying = {
                            openNowPlayingInReadMode = false
                            showNowPlayingOverlay = true
                        },
                        onRetryPlayback = viewModel.audioEngine::retryCurrentTrack,
                        onPlayAlternativeTrack = { viewModel.audioEngine.playTrack(it) }
                    )
                }
            } else {
                // Compact Mobile Layout (< 600.dp down to 320.dp) — Quranify Mobile Design
                data class MobileTabEntry(
                    val key: String,
                    val label: String,
                    val selectedIcon: ImageVector,
                    val unselectedIcon: ImageVector,
                    val destination: AppDestination
                )

                val mobileNavItems = remember(strings) {
                    listOf(
                        MobileTabEntry("home", strings.navHome, Icons.Filled.Home, Icons.Outlined.Home, AppDestination.Home),
                        MobileTabEntry("collections", strings.navCollections, Icons.Filled.GridView, Icons.Outlined.GridView, AppDestination.Collections),
                        MobileTabEntry("reciters", strings.navReciters, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, AppDestination.Reciters),
                        MobileTabEntry("search", strings.navSearch, Icons.Filled.Search, Icons.Outlined.Search, AppDestination.Search),
                        MobileTabEntry("more", "More", Icons.Filled.Menu, Icons.Outlined.Menu, AppDestination.More)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    MobileTopHeader(
                        strings = strings,
                        currentLanguage = preferences.preferredLanguage,
                        onBrandClick = { navigateTo(AppDestination.Home) },
                        onOpenSearch = { navigateTo(AppDestination.Search) },
                        onSelectLanguage = viewModel::setLanguage
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        RouteHostContent(
                            destination = currentDestination,
                            viewModel = viewModel,
                            catalogState = catalogState,
                            preferences = preferences,
                            playlists = playlists,
                            listeningEvents = listeningEvents,
                            authBannerDismissed = authBannerDismissed,
                            playerState = playerState,
                            ambientState = ambientState,
                            searchState = searchState,
                            insightsSummary = insightsSummary,
                            collectionFilter = collectionFilter,
                            collectionSearchQuery = collectionSearchQuery,
                            reciterSearchQuery = reciterSearchQuery,
                            reciterCountryFilter = reciterCountryFilter,
                            reciterSortMode = reciterSortMode,
                            spokenFilter = spokenFilter,
                            selectedHeroReciterId = selectedHeroReciterId,
                            offlineState = offlineState,
                            currentUser = currentUser,
                            strings = strings,
                            bottomReservedPadding = 16.dp,
                            onNavigate = navigateTo,
                            onNavigateBack = navigateBack,
                            onOpenAccountModal = { showAccountDialog = true },
                            onOpenAddToPlaylist = { pendingPlaylistTrack = it },
                            onOpenAmbienceModal = { showAmbienceDialog = true },
                            onOpenSleepTimerModal = { showSleepTimerDialog = true },
                            onShowTrackLicence = { inspectingLicenceTrack = it },
                            onOpenSourcesAttributionModal = { showSourcesAttributionDialog = true },
                            onOpenReadQuranModal = {
                                openNowPlayingInReadMode = true
                                showNowPlayingOverlay = true
                            }
                        )
                    }

                    // Persistent Floating Mini-Player stacked cleanly ABOVE Bottom Navigation Bar
                    MobileMiniPlayerBar(
                        playerState = playerState,
                        isFavorite = isActiveFavorite,
                        ambientState = ambientState,
                        onTogglePlayPause = viewModel.audioEngine::togglePlayPause,
                        onNext = { viewModel.audioEngine.skipNext(false) },
                        onToggleFavorite = {
                            activeTrack?.let { viewModel.toggleFavoriteTrack(it.id) }
                        },
                        onOpenNowPlaying = {
                            openNowPlayingInReadMode = false
                            showNowPlayingOverlay = true
                        }
                    )

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color(0xFF1F222A)
                    )

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("mobile_bottom_nav")
                    ) {
                        mobileNavItems.forEach { entry ->
                            val isSelected = mobileActiveKey == entry.key
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navigateTo(entry.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) entry.selectedIcon else entry.unselectedIcon,
                                        contentDescription = entry.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = entry.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.testTag("bottom_nav_${entry.key}")
                            )
                        }
                    }
                }
            }

            if (showNowPlayingOverlay) {
                NowPlayingFullScreenOverlay(
                    playerState = playerState,
                    scriptureState = scriptureState,
                    availableTranslations = catalogState.translations,
                    arabicFontScale = preferences.arabicFontScale,
                    isFavorite = isActiveFavorite,
                    strings = strings,
                    ambientState = ambientState,
                    initialOpenReadSheet = openNowPlayingInReadMode,
                    onMinimize = {
                        showNowPlayingOverlay = false
                        openNowPlayingInReadMode = false
                    },
                    onTogglePlayPause = viewModel.audioEngine::togglePlayPause,
                    onPrevious = viewModel.audioEngine::skipPrevious,
                    onNext = { viewModel.audioEngine.skipNext(false) },
                    onToggleShuffle = viewModel.audioEngine::toggleShuffle,
                    onCycleRepeat = viewModel.audioEngine::cycleRepeatMode,
                    onSeekTo = viewModel.audioEngine::seekTo,
                    onSeekToVerse = viewModel.audioEngine::seekToVerse,
                    onToggleVerseRepeat = viewModel.audioEngine::toggleVerseRepeat,
                    onToggleFavorite = { activeTrack?.let { viewModel.toggleFavoriteTrack(it.id) } },
                    onSelectTranslation = viewModel::selectTranslationEdition,
                    onToggleCompactReading = viewModel::toggleCompactReadingMode,
                    onRetryScripture = { viewModel.loadScriptureForSurah() },
                    onOpenQueue = { showQueueDialog = true },
                    onOpenSleepTimer = { showSleepTimerDialog = true },
                    onOpenAmbience = { showAmbienceDialog = true },
                    onToggleBgMusicPlayPause = viewModel::toggleBackgroundMusicPlayPause,
                    onSetBgMusicEnabled = viewModel::setBackgroundMusicEnabled,
                    onSelectBgMusicTrack = viewModel::selectBackgroundMusicTrack,
                    onUpdateBgMusicVolume = viewModel::updateAmbientVolume,
                    onStopBgMusic = viewModel::stopAllAmbience,
                    onRetryPlayback = viewModel.audioEngine::retryCurrentTrack,
                    onPlayAlternativeTrack = { viewModel.audioEngine.playTrack(it) },
                    onPlayVerseAudio = { surahNum, verseNum -> viewModel.playVerseAudio(surahNum, verseNum) },
                    onShowTrackLicence = { inspectingLicenceTrack = it }
                )
            }
        }
    }

    if (showQueueDialog) {
        QueueDialog(
            playerState = playerState,
            strings = strings,
            onDismiss = { showQueueDialog = false },
            onPlayEntry = viewModel.audioEngine::playQueueEntry,
            onMoveItem = viewModel.audioEngine::moveQueueItem,
            onRemoveItem = viewModel.audioEngine::removeQueueItem,
            onClearQueue = viewModel.audioEngine::clearQueuePreservingCurrent
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            remainingSeconds = playerState.sleepTimerRemainingSec,
            strings = strings,
            onDismiss = { showSleepTimerDialog = false },
            onStartTimerMinutes = viewModel.audioEngine::startSleepTimer,
            onCancelTimer = viewModel.audioEngine::cancelSleepTimer
        )
    }

    if (showAmbienceDialog) {
        AmbienceMixerDialog(
            ambientState = ambientState,
            strings = strings,
            onDismiss = { showAmbienceDialog = false },
            onSelectSound = viewModel::toggleAmbientSoundscape,
            onPreviewSound = viewModel::previewAmbientSoundscape,
            onVolumeChange = viewModel::updateAmbientVolume,
            onStopAll = viewModel::stopAllAmbience
        )
    }

    if (showAccountDialog) {
        AccountManagementDialog(
            currentUser = currentUser,
            strings = strings,
            onDismiss = { showAccountDialog = false },
            onOpenSettings = { navigateTo(AppDestination.Settings) },
            onSignedOut = onSignedOutToWelcome
        )
    }

    if (showSourcesAttributionDialog) {
        val editions = remember(catalogState.reciters) { viewModel.audioProvider.getAvailableEditions() }
        AudioSourcesAndAttributionDialog(
            editions = editions,
            diagnosticEvents = playerState.diagnosticEvents,
            onDismiss = { showSourcesAttributionDialog = false }
        )
    }

    inspectingLicenceTrack?.let { track ->
        TrackLicenceInfoDialog(
            track = track,
            licence = viewModel.audioProvider.getLicenceMetadata(track),
            onDismiss = { inspectingLicenceTrack = null }
        )
    }

    pendingPlaylistTrack?.let { targetTrack ->
        AddToPlaylistDialog(
            track = targetTrack,
            playlists = playlists,
            onDismiss = { pendingPlaylistTrack = null },
            onSelectPlaylist = { pl -> viewModel.addTrackToPlaylist(pl, targetTrack.id) },
            onCreateAndAdd = { name, desc ->
                viewModel.createCustomPlaylist(name, desc, listOf(targetTrack.id))
            }
        )
    }
}

@Composable
private fun DesktopSidebar(
    navItems: List<NavEntry>,
    activeKey: String,
    strings: UiStrings,
    currentUser: FirebaseUser?,
    isDemoPremium: Boolean,
    onNavigate: (AppDestination) -> Unit,
    onOpenAccount: () -> Unit
) {
    val discoverKeys = setOf("home", "collections", "reciters", "search")
    val discoverItems = remember(navItems) { navItems.filter { it.key in discoverKeys } }
    val libraryItems = remember(navItems) { navItems.filterNot { it.key in discoverKeys } }

    Surface(
        modifier = Modifier
            .width(226.dp)
            .fillMaxHeight()
            .testTag("desktop_sidebar"),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Brand Bar aligned at 56.dp height with TopUtilityHeader
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onNavigate(AppDestination.Home) }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TilawahLogoMark(size = 30.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.appBrand,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "DISCOVER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    discoverItems.forEach { item ->
                        SidebarNavRow(
                            label = item.label,
                            icon = item.icon,
                            selected = activeKey == item.key,
                            onClick = { onNavigate(item.destination) },
                            testTag = "sidebar_nav_${item.key}"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "YOUR LIBRARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    libraryItems.forEach { item ->
                        SidebarNavRow(
                            label = item.label,
                            icon = item.icon,
                            selected = activeKey == item.key,
                            onClick = { onNavigate(item.destination) },
                            testTag = "sidebar_nav_${item.key}"
                        )
                    }

                    SidebarNavRow(
                        label = strings.navInsights,
                        icon = Icons.Filled.BarChart,
                        selected = activeKey == "insights",
                        onClick = { onNavigate(AppDestination.Insights) },
                        testTag = "sidebar_nav_insights"
                    )
                    SidebarNavRow(
                        label = strings.navSettings,
                        icon = Icons.Filled.Settings,
                        selected = activeKey == "settings",
                        onClick = { onNavigate(AppDestination.Settings) },
                        testTag = "sidebar_nav_settings"
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline
                )

                // Compact Plus Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(AppDestination.Premium) }
                        .testTag("sidebar_premium_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isDemoPremium) "${strings.premiumTitle} (Demo)" else strings.premiumTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Soundscapes & analytics",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Compact Account Row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenAccount)
                        .testTag("sidebar_account_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser?.displayName ?: "Local Guest Mode",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (currentUser != null) "Cloud synced" else "Tap to sign in",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarNavRow(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TabletNavigationRail(
    navItems: List<NavEntry>,
    activeKey: String,
    strings: UiStrings,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.testTag("tablet_navigation_rail"),
        header = {
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .padding(vertical = 8.dp)
                    .clickable { onNavigate(AppDestination.Home) },
                contentAlignment = Alignment.Center
            ) {
                TilawahLogoMark(size = 30.dp)
            }
        }
    ) {
        navItems.forEach { item ->
            NavigationRailItem(
                selected = activeKey == item.key,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1) },
                modifier = Modifier.testTag("rail_nav_${item.key}")
            )
        }
        NavigationRailItem(
            selected = activeKey == "insights",
            onClick = { onNavigate(AppDestination.Insights) },
            icon = { Icon(Icons.Filled.BarChart, contentDescription = strings.navInsights) },
            label = { Text(strings.navInsights, maxLines = 1) },
            modifier = Modifier.testTag("rail_nav_insights")
        )
        NavigationRailItem(
            selected = activeKey == "premium",
            onClick = { onNavigate(AppDestination.Premium) },
            icon = { Icon(Icons.Filled.WorkspacePremium, contentDescription = strings.navPremium) },
            label = { Text(strings.navPremium, maxLines = 1) },
            modifier = Modifier.testTag("rail_nav_premium")
        )
    }
}

@Composable
private fun MobileTopHeader(
    strings: UiStrings,
    currentLanguage: AppLanguage,
    onBrandClick: () -> Unit,
    onOpenSearch: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    var langMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            .testTag("mobile_top_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBrandClick)
                .padding(end = 8.dp)
                .testTag("mobile_brand_logo")
        ) {
            QuranWaveSymbol(
                size = 24.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = strings.appBrand,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("top_search_shortcut")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = strings.navSearch,
                    tint = Color(0xFFE4E4E7),
                    modifier = Modifier.size(22.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { langMenuOpen = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("top_language_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = strings.languageSection,
                        tint = Color(0xFFE4E4E7),
                        modifier = Modifier.size(21.dp)
                    )
                }
                DropdownMenu(
                    expanded = langMenuOpen,
                    onDismissRequest = { langMenuOpen = false }
                ) {
                    AppLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${lang.nativeName} (${lang.code.uppercase()})",
                                    fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                langMenuOpen = false
                                onSelectLanguage(lang)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopUtilityHeader(
    strings: UiStrings,
    currentLanguage: AppLanguage,
    currentUser: FirebaseUser?,
    showLogo: Boolean,
    isDesktopWidth: Boolean,
    onBrandClick: () -> Unit,
    onOpenSearch: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAccount: () -> Unit
) {
    var langMenuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showLogo) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBrandClick)
                        .padding(horizontal = 4.dp)
                ) {
                    TilawahLogoMark(size = 28.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.appBrand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isDesktopWidth) {
                // Sleek Desktop Quick Search Pill aligned horizontally with Sidebar Brand Row
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .width(300.dp)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSearch)
                        .testTag("top_search_shortcut")
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = strings.navSearch,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Search surahs, reciters, collections...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isDesktopWidth) {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("top_search_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = strings.navSearch,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { langMenuOpen = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("top_language_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = strings.languageSection,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = langMenuOpen,
                        onDismissRequest = { langMenuOpen = false }
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.nativeName} (${lang.code.uppercase()})") },
                                onClick = {
                                    langMenuOpen = false
                                    onSelectLanguage(lang)
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = strings.navSettings,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenAccount,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("top_account_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Account",
                        tint = if (currentUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun RouteHostContent(
    destination: AppDestination,
    viewModel: TilawahViewModel,
    catalogState: com.example.viewmodel.CatalogUiState,
    preferences: com.example.data.model.UserPreferencesState,
    playlists: List<com.example.data.model.CustomPlaylist>,
    listeningEvents: List<com.example.data.model.ListeningEventRecord>,
    authBannerDismissed: Boolean,
    playerState: com.example.player.GlobalPlayerState,
    ambientState: com.example.player.AmbientUiState,
    searchState: com.example.viewmodel.SearchResultsState,
    insightsSummary: com.example.data.model.ListeningInsightsSummary,
    collectionFilter: com.example.data.model.CollectionCategory,
    collectionSearchQuery: String,
    reciterSearchQuery: String,
    reciterCountryFilter: String,
    reciterSortMode: String,
    spokenFilter: String,
    selectedHeroReciterId: String,
    offlineState: OfflineDownloadsUiState,
    currentUser: FirebaseUser?,
    strings: UiStrings,
    bottomReservedPadding: androidx.compose.ui.unit.Dp,
    onNavigate: (AppDestination) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenAccountModal: () -> Unit,
    onOpenAddToPlaylist: (AudioTrack) -> Unit,
    onOpenAmbienceModal: () -> Unit,
    onOpenSleepTimerModal: () -> Unit,
    onShowTrackLicence: (AudioTrack) -> Unit,
    onOpenSourcesAttributionModal: () -> Unit,
    onOpenReadQuranModal: () -> Unit = {}
) {
    val savedOfflineIds = offlineState.savedTracks.keys

    when (destination) {
        is AppDestination.Home -> HomeScreen(
            catalogState = catalogState,
            preferences = preferences,
            playerState = playerState,
            recentEvents = listeningEvents,
            isGuestMode = currentUser == null,
            authBannerDismissed = authBannerDismissed,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onOpenAccountModal = onOpenAccountModal,
            onDismissAuthBanner = viewModel::dismissAuthBanner,
            onPlaySurahWithReciter = viewModel::playSurahWithReciter,
            onPlayReciterFeatured = viewModel::playReciterFeaturedSet,
            onOpenReciter = { onNavigate(AppDestination.ReciterDetail(it)) },
            onOpenAllReciters = { onNavigate(AppDestination.Reciters) },
            onOpenCollection = { onNavigate(AppDestination.CollectionDetail(it)) },
            onPlayCollection = { viewModel.playCollection(it, false) },
            onAddCollectionToQueue = viewModel::addCollectionToQueue,
            onOpenAllCollections = { onNavigate(AppDestination.Collections) },
            onToggleSaveCollection = viewModel::toggleSavedCollection,
            onToggleFavoriteReciter = viewModel::toggleFavoriteReciter,
            onToggleFavoriteTrack = viewModel::toggleFavoriteTrack,
            onPlayTrack = { viewModel.audioEngine.playOrToggleTrack(it) },
            onAddTrackToQueue = viewModel.audioEngine::addTrackToQueue,
            onAddTrackToPlaylist = onOpenAddToPlaylist,
            onResolveTracks = viewModel::resolveTracks,
            onOpenPremium = { onNavigate(AppDestination.Premium) },
            onOpenSettings = { onNavigate(AppDestination.Settings) },
            onRefreshCatalog = viewModel::refreshCatalog,
            spokenSessions = viewModel.spokenSessions,
            spokenFilter = spokenFilter,
            savedOfflineTrackIds = savedOfflineIds,
            activeDownloadProgress = offlineState.activeDownloadProgress,
            onSelectSpokenFilter = viewModel::setSpokenFilter,
            onPlaySpokenSession = { track, queue -> viewModel.playSpokenSession(track, queue) },
            onIsDownloadPermitted = viewModel::isOfflineDownloadPermitted,
            onDownloadTrackOffline = viewModel::downloadTrackForPersonalOfflineUse,
            onRemoveOfflineTrack = viewModel::removeOfflineTrack,
            onShowTrackLicence = onShowTrackLicence,
            selectedHeroReciterId = selectedHeroReciterId,
            onSelectHeroReciter = viewModel::selectHeroReciter,
            onPlayHeroReciter = viewModel::toggleHeroReciterPlayback
        )

        is AppDestination.Collections -> CollectionsOverviewScreen(
            collections = catalogState.collections,
            selectedCategory = collectionFilter,
            searchQuery = collectionSearchQuery,
            preferences = preferences,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onSelectCategory = viewModel::setCollectionFilter,
            onSearchQueryChange = viewModel::setCollectionSearchQuery,
            onOpenCollection = { onNavigate(AppDestination.CollectionDetail(it)) },
            onPlayCollection = { viewModel.playCollection(it, false) },
            onToggleSaveCollection = viewModel::toggleSavedCollection,
            onAddCollectionToQueue = viewModel::addCollectionToQueue
        )

        is AppDestination.CollectionDetail -> {
            val col = catalogState.collections.firstOrNull { it.id == destination.collectionId }
            val tracks = col?.let { viewModel.resolveTracks(it.trackIds) }.orEmpty()
            CollectionDetailScreen(
                collection = col,
                tracks = tracks,
                preferences = preferences,
                playerState = playerState,
                strings = strings,
                bottomReservedPadding = bottomReservedPadding,
                onBack = onNavigateBack,
                onPlayAll = { col?.let { viewModel.playCollection(it, false) } },
                onShufflePlay = { col?.let { viewModel.playCollection(it, true) } },
                onToggleSaveCollection = { col?.let { viewModel.toggleSavedCollection(it.id) } },
                onAddCollectionToQueue = { col?.let { viewModel.addCollectionToQueue(it) } },
                onPlayTrack = { track -> viewModel.audioEngine.playOrToggleTrack(track, newQueue = tracks) },
                onToggleFavoriteTrack = viewModel::toggleFavoriteTrack,
                onAddTrackToQueue = viewModel.audioEngine::addTrackToQueue,
                onAddTrackToPlaylist = onOpenAddToPlaylist,
                onOpenReciter = { onNavigate(AppDestination.ReciterDetail(it)) },
                savedOfflineTrackIds = savedOfflineIds,
                activeDownloadProgress = offlineState.activeDownloadProgress,
                onIsDownloadPermitted = viewModel::isOfflineDownloadPermitted,
                onDownloadTrackOffline = viewModel::downloadTrackForPersonalOfflineUse,
                onRemoveOfflineTrack = viewModel::removeOfflineTrack,
                onShowTrackLicence = onShowTrackLicence
            )
        }

        is AppDestination.Reciters -> RecitersOverviewScreen(
            reciters = catalogState.reciters,
            searchQuery = reciterSearchQuery,
            countryFilter = reciterCountryFilter,
            sortMode = reciterSortMode,
            preferences = preferences,
            recentEvents = listeningEvents,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onSearchQueryChange = viewModel::setReciterSearchQuery,
            onCountryFilterChange = viewModel::setReciterCountryFilter,
            onSortModeChange = viewModel::setReciterSortMode,
            onOpenReciter = { onNavigate(AppDestination.ReciterDetail(it)) },
            onPlayReciterFeatured = viewModel::playReciterFeaturedSet,
            onToggleFavoriteReciter = viewModel::toggleFavoriteReciter
        )

        is AppDestination.ReciterDetail -> {
            val reciter = catalogState.reciters.firstOrNull { it.id == destination.reciterId }
            ReciterDetailScreen(
                reciter = reciter,
                surahs = catalogState.surahs,
                preferences = preferences,
                playerState = playerState,
                recentEvents = listeningEvents,
                strings = strings,
                bottomReservedPadding = bottomReservedPadding,
                onBack = onNavigateBack,
                onBuildTrack = viewModel::buildTrackForReciter,
                onPlayReciterFeatured = viewModel::playReciterFeaturedSet,
                onToggleFavoriteReciter = viewModel::toggleFavoriteReciter,
                onPlaySurahList = { track, queue -> viewModel.audioEngine.playOrToggleTrack(track, newQueue = queue) },
                onToggleFavoriteTrack = viewModel::toggleFavoriteTrack,
                onAddTrackToQueue = viewModel.audioEngine::addTrackToQueue,
                onAddTrackToPlaylist = onOpenAddToPlaylist,
                savedOfflineTrackIds = savedOfflineIds,
                activeDownloadProgress = offlineState.activeDownloadProgress,
                onIsDownloadPermitted = viewModel::isOfflineDownloadPermitted,
                onDownloadTrackOffline = viewModel::downloadTrackForPersonalOfflineUse,
                onRemoveOfflineTrack = viewModel::removeOfflineTrack,
                onShowTrackLicence = onShowTrackLicence
            )
        }

        is AppDestination.Search -> SearchScreen(
            searchState = searchState,
            defaultReciterId = catalogState.reciters.firstOrNull()?.id ?: "mishary_alafasy",
            preferences = preferences,
            playerState = playerState,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onQueryChange = viewModel::updateSearchQuery,
            onCommitRecentSearch = viewModel::commitRecentSearch,
            onClearRecentSearches = viewModel::clearRecentSearches,
            onSelectTab = viewModel::setSearchTab,
            onBuildTrackForSurah = viewModel::buildTrackForReciter,
            onPlayTrack = { viewModel.audioEngine.playOrToggleTrack(it) },
            onToggleFavoriteTrack = viewModel::toggleFavoriteTrack,
            onAddTrackToQueue = viewModel.audioEngine::addTrackToQueue,
            onAddTrackToPlaylist = onOpenAddToPlaylist,
            onOpenReciter = { onNavigate(AppDestination.ReciterDetail(it)) },
            onPlayReciterFeatured = viewModel::playReciterFeaturedSet,
            onToggleFavoriteReciter = viewModel::toggleFavoriteReciter,
            onOpenCollection = { onNavigate(AppDestination.CollectionDetail(it)) },
            onPlayCollection = { viewModel.playCollection(it, false) },
            onToggleSaveCollection = viewModel::toggleSavedCollection,
            onAddCollectionToQueue = viewModel::addCollectionToQueue,
            savedOfflineTrackIds = savedOfflineIds,
            activeDownloadProgress = offlineState.activeDownloadProgress,
            onIsDownloadPermitted = viewModel::isOfflineDownloadPermitted,
            onDownloadTrackOffline = viewModel::downloadTrackForPersonalOfflineUse,
            onRemoveOfflineTrack = viewModel::removeOfflineTrack,
            onShowTrackLicence = onShowTrackLicence
        )

        is AppDestination.Library -> LibraryScreen(
            playlists = playlists,
            surahs = catalogState.surahs,
            reciters = catalogState.reciters,
            collections = catalogState.collections,
            listeningEvents = listeningEvents,
            preferences = preferences,
            playerState = playerState,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onResolveTracks = viewModel::resolveTracks,
            onBuildTrackForSurah = viewModel::buildTrackForReciter,
            onCreatePlaylist = { name, desc -> viewModel.createCustomPlaylist(name, desc) },
            onUpdatePlaylistMetadata = viewModel::updateCustomPlaylistMetadata,
            onAddTrackToPlaylist = viewModel::addTrackToPlaylist,
            onRemoveTrackFromPlaylist = viewModel::removeTrackFromPlaylist,
            onReorderPlaylistTrack = viewModel::reorderPlaylistTrack,
            onDeletePlaylist = viewModel::deleteCustomPlaylist,
            onPlayPlaylist = viewModel::playCustomPlaylist,
            onPlayTrack = { viewModel.audioEngine.playOrToggleTrack(it) },
            onToggleFavoriteTrack = viewModel::toggleFavoriteTrack,
            onAddTrackToQueue = viewModel.audioEngine::addTrackToQueue,
            onOpenAddToPlaylistDialog = onOpenAddToPlaylist,
            onOpenReciter = { onNavigate(AppDestination.ReciterDetail(it)) },
            onPlayReciterFeatured = viewModel::playReciterFeaturedSet,
            onToggleFavoriteReciter = viewModel::toggleFavoriteReciter,
            onOpenCollection = { onNavigate(AppDestination.CollectionDetail(it)) },
            onPlayCollection = { viewModel.playCollection(it, false) },
            onToggleSaveCollection = viewModel::toggleSavedCollection,
            onAddCollectionToQueue = viewModel::addCollectionToQueue,
            offlineState = offlineState,
            onIsDownloadPermitted = viewModel::isOfflineDownloadPermitted,
            onDownloadTrackOffline = viewModel::downloadTrackForPersonalOfflineUse,
            onRemoveOfflineTrack = viewModel::removeOfflineTrack,
            onClearOfflineError = viewModel::clearOfflineDownloadError,
            onShowTrackLicence = onShowTrackLicence
        )

        is AppDestination.Insights -> InsightsScreen(
            summary = insightsSummary,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onOpenReciter = { onNavigate(AppDestination.ReciterDetail(it)) }
        )

        is AppDestination.Premium -> PremiumScreen(
            preferences = preferences,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onToggleDemoPremium = viewModel::setDemoPremiumEnabled,
            onOpenAmbience = onOpenAmbienceModal,
            onOpenInsights = { onNavigate(AppDestination.Insights) }
        )

        is AppDestination.Settings -> SettingsScreen(
            preferences = preferences,
            ambientState = ambientState,
            currentUser = currentUser,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onSetDarkTheme = viewModel::setDarkTheme,
            onSetReducedMotion = viewModel::setReducedMotion,
            onSetArabicScale = viewModel::setArabicFontScale,
            onSetAutoplayNext = viewModel::setAutoplayNext,
            onSetDefaultRepeat = viewModel::setDefaultRepeatMode,
            onSetLanguage = viewModel::setLanguage,
            onOpenAmbienceDialog = onOpenAmbienceModal,
            onOpenSleepTimerDialog = onOpenSleepTimerModal,
            onOpenAccountDialog = onOpenAccountModal,
            onSetPreferredAudioSource = viewModel::setPreferredAudioSource,
            onOpenSourcesAttributionDialog = onOpenSourcesAttributionModal
        )

        is AppDestination.More -> MoreScreen(
            preferences = preferences,
            playerState = playerState,
            ambientState = ambientState,
            currentUser = currentUser,
            strings = strings,
            bottomReservedPadding = bottomReservedPadding,
            onOpenLibrary = { onNavigate(AppDestination.Library) },
            onOpenReadQuran = onOpenReadQuranModal,
            onOpenAmbience = onOpenAmbienceModal,
            onOpenSleepTimer = onOpenSleepTimerModal,
            onOpenInsights = { onNavigate(AppDestination.Insights) },
            onOpenPremium = { onNavigate(AppDestination.Premium) },
            onOpenAccount = onOpenAccountModal,
            onOpenSettings = { onNavigate(AppDestination.Settings) },
            onOpenSourcesAttribution = onOpenSourcesAttributionModal
        )
    }
}
