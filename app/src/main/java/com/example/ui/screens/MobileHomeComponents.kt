package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.data.model.CuratedCollection
import com.example.data.model.Reciter
import com.example.data.model.UserPreferencesState
import com.example.i18n.UiStrings
import com.example.player.GlobalPlayerState
import com.example.ui.components.ReciterAvatar
import com.example.ui.components.TrackRowItem
import com.example.viewmodel.CatalogUiState

@Composable
internal fun MobileHomeContent(
    catalogState: CatalogUiState,
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    heroReciter: Reciter?,
    coreCollections: List<CuratedCollection>,
    tajwidReciters: List<Reciter>,
    distinctRecentTracks: List<AudioTrack>,
    favoriteTracks: List<AudioTrack>,
    isGuestMode: Boolean,
    authBannerDismissed: Boolean,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onOpenAccountModal: () -> Unit,
    onDismissAuthBanner: () -> Unit,
    onPlayHeroReciter: (String) -> Unit,
    onPlayReciterFeatured: (String) -> Unit,
    onOpenReciter: (String) -> Unit,
    onOpenAllReciters: () -> Unit,
    onOpenCollection: (String) -> Unit,
    onPlayCollection: (CuratedCollection) -> Unit,
    onOpenAllCollections: () -> Unit,
    onToggleFavoriteTrack: (String) -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onAddTrackToQueue: (AudioTrack) -> Unit,
    onAddTrackToPlaylist: (AudioTrack) -> Unit,
    onRefreshCatalog: () -> Unit,
    spokenSessions: List<AudioTrack>,
    spokenFilter: String,
    savedOfflineTrackIds: Set<String>,
    activeDownloadProgress: Map<String, Float>,
    onSelectSpokenFilter: (String) -> Unit,
    onPlaySpokenSession: (AudioTrack, List<AudioTrack>) -> Unit,
    onIsDownloadPermitted: (AudioTrack) -> Boolean,
    onDownloadTrackOffline: (AudioTrack) -> Unit,
    onRemoveOfflineTrack: (String) -> Unit,
    onShowTrackLicence: (AudioTrack) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 6.dp,
            bottom = bottomReservedPadding + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (catalogState.catalogStatusBanner != null) {
            item(key = "mobile_catalog_banner") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = catalogState.catalogStatusBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRefreshCatalog) {
                            Text(strings.retry)
                        }
                    }
                }
            }
        }

        // 1. Your Reciter Section (matching Phone 1 top section)
        if (heroReciter != null) {
            item(key = "hero_your_reciter") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MobileSectionHeader(
                        title = strings.yourReciter,
                        actionLabel = "See all",
                        onActionClick = onOpenAllReciters,
                        actionTestTag = "see_all_your_reciter"
                    )
                    MobileYourReciterCard(
                        reciter = heroReciter,
                        playerState = playerState,
                        strings = strings,
                        onPlayClick = { onPlayHeroReciter(heroReciter.id) },
                        onViewReciterClick = { onOpenReciter(heroReciter.id) }
                    )
                }
            }
        }

        // 2. Popular Collections Section (compact vertical portrait cards with unique artwork)
        item(key = "curated_collections_section") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MobileSectionHeader(
                    title = "Popular Collections",
                    actionLabel = "See all",
                    onActionClick = onOpenAllCollections,
                    actionTestTag = "see_all_popular_collections"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("popular_collections_row")
                ) {
                    items(items = coreCollections, key = { it.id }) { collection ->
                        val isCollectionPlaying = playerState.isPlaying &&
                            playerState.currentEntry?.track?.id in collection.trackIds
                        MobileCollectionPortraitCard(
                            collection = collection,
                            isPlaying = isCollectionPlaying,
                            onOpenCollection = { onOpenCollection(collection.id) },
                            onPlayCollection = { onPlayCollection(collection) }
                        )
                    }
                }
            }
        }

        // 3. Best for Tajwid Section (Al-Husary, Al-Minshawi, Abdul Samad)
        if (tajwidReciters.isNotEmpty()) {
            item(key = "best_for_tajwid_section") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MobileSectionHeader(
                        title = strings.bestForTajwid,
                        actionLabel = "See all",
                        onActionClick = onOpenAllReciters,
                        actionTestTag = "see_all_best_for_tajwid"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tajwid_reciters_row")
                    ) {
                        items(items = tajwidReciters, key = { it.id }) { reciter ->
                            val isThisReciterPlaying = playerState.isPlaying &&
                                playerState.currentEntry?.track?.reciterId == reciter.id
                            MobileTajwidReciterCard(
                                reciter = reciter,
                                isPlaying = isThisReciterPlaying,
                                onOpenProfile = { onOpenReciter(reciter.id) },
                                onPlayReciter = { onPlayReciterFeatured(reciter.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Continue Listening (when user has listening history)
        if (distinctRecentTracks.isNotEmpty()) {
            item(key = "continue_listening_section") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MobileSectionHeader(title = strings.continueListening)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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

        // 5. Dedicated Spoken Adhkar, Duas & Ruqya Voice Section (below the fold)
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

        // 6. Your Favorites (when user has liked surahs)
        if (favoriteTracks.isNotEmpty()) {
            item(key = "home_favorites_section") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MobileSectionHeader(title = strings.yourFavorites)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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

        // 7. Compact Guest Sync Strip (below the fold so top hierarchy matches Quranify mobile cleanly)
        if (isGuestMode && !authBannerDismissed) {
            item(key = "guest_sync_banner") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_sync_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sign in to sync favorites & playlists",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onOpenAccountModal,
                            shape = CircleShape,
                            modifier = Modifier.testTag("banner_sign_in_button")
                        ) {
                            Text("Sign In", style = MaterialTheme.typography.labelSmall)
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
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionTestTag: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp)
                    .let { mod -> if (actionTestTag != null) mod.testTag(actionTestTag) else mod },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "$actionLabel $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MobileYourReciterCard(
    reciter: Reciter,
    playerState: GlobalPlayerState,
    strings: UiStrings,
    onPlayClick: () -> Unit,
    onViewReciterClick: () -> Unit
) {
    val isThisReciterPlaying = playerState.isPlaying &&
        playerState.currentEntry?.track?.reciterId == reciter.id

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("your_reciter_hero_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            val isVeryNarrow = maxWidth < 310.dp
            val avatarSize = if (isVeryNarrow) 70.dp else 84.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ReciterAvatar(
                    reciterName = reciter.displayName,
                    accentHex = reciter.avatarAccentHex,
                    arabicName = reciter.arabicName,
                    size = avatarSize,
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onViewReciterClick)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reciter.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isVeryNarrow) 17.sp else 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!reciter.country.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            VerifiedCountryEmblem(country = reciter.country)
                            Text(
                                text = reciter.country,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dark Pill Play Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onPlayClick)
                                .testTag("hero_play_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isThisReciterPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isThisReciterPlaying) "Pause ${reciter.displayName}" else "Play ${reciter.displayName}",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isThisReciterPlaying) "Pause" else strings.play,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Dark Pill View Reciter -> Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onViewReciterClick)
                                .testTag("hero_view_reciter_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.viewReciter,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerifiedCountryEmblem(country: String) {
    val badgeBg = when (country.lowercase()) {
        "saudi arabia" -> Color(0xFF15803D)
        "egypt" -> Color(0xFF991B1B)
        "kuwait" -> Color(0xFF0F766E)
        else -> Color(0xFF27272A)
    }
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(13.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(badgeBg)
            .border(0.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 10.dp, height = 6.dp)) {
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(0f, size.height * 0.45f),
                end = Offset(size.width, size.height * 0.45f),
                strokeWidth = 1.4f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.65f),
                start = Offset(size.width * 0.2f, size.height * 0.82f),
                end = Offset(size.width * 0.8f, size.height * 0.82f),
                strokeWidth = 1.1f
            )
        }
    }
}

@Composable
private fun MobileCollectionPortraitCard(
    collection: CuratedCollection,
    isPlaying: Boolean,
    onOpenCollection: () -> Unit,
    onPlayCollection: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(132.dp)
            .height(176.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenCollection)
            .testTag("collection_card_${collection.id}")
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
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.48f to Color(0x3308090C),
                        0.76f to Color(0xCC090B0E),
                        1.0f to Color(0xF50A0B0E)
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = collection.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                ),
                color = Color(0xFFFAFAFA),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlayCollection)
                    .testTag("collection_play_${collection.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xD9232730))
                        .border(0.75.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause ${collection.title}" else "Play ${collection.title}",
                        tint = Color(0xFFFAFAFA),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileTajwidReciterCard(
    reciter: Reciter,
    isPlaying: Boolean,
    onOpenProfile: () -> Unit,
    onPlayReciter: () -> Unit
) {
    val conciseName = when (reciter.id) {
        "mahmoud_husary" -> "Al-Husary"
        "siddiq_minshawi" -> "Al-Minshawi"
        "abdul_basit" -> "Abdul Samad"
        else -> reciter.displayName.split(" ").takeLast(2).joinToString(" ")
    }

    Surface(
        modifier = Modifier
            .width(114.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenProfile)
            .testTag("reciter_card_${reciter.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReciterAvatar(
                reciterName = reciter.displayName,
                accentHex = reciter.avatarAccentHex,
                arabicName = reciter.arabicName,
                size = 64.dp,
                shape = CircleShape
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = conciseName,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlayReciter)
                    .testTag("reciter_play_${reciter.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .border(0.75.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause ${reciter.displayName}" else "Play ${reciter.displayName}",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
