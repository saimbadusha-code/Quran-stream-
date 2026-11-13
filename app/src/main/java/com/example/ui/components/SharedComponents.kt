package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrack
import com.example.data.model.CuratedCollection
import com.example.data.model.Reciter
import com.example.data.model.TrackAvailability
import com.example.ui.theme.QuranArabicFontFamily

@Composable
fun QuranWaveSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val ratios = listOf(0.20f, 0.52f, 0.86f, 1.0f, 0.72f, 0.44f, 0.20f)
        val barWidth = w * 0.095f
        val gap = (w - barWidth * ratios.size) / (ratios.size - 1).coerceAtLeast(1)
        val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

        ratios.forEachIndexed { idx, ratio ->
            val barH = (h * ratio).coerceAtLeast(barWidth)
            val x = idx * (barWidth + gap)
            val y = (h - barH) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = corner
            )
        }
    }
}

@DrawableRes
fun trackArtworkResId(track: AudioTrack?): Int {
    if (track == null) return R.drawable.img_col_focus_1793262964938
    return when {
        track.audioCategory == AudioCategory.SPOKEN_RUQYA ||
            track.audioCategory == AudioCategory.SPOKEN_SUPPLICATION_ADHKAR ->
            R.drawable.img_col_duaa_1793262968390
        track.surahNumber in setOf(1, 2, 18, 36, 103) ->
            R.drawable.img_col_focus_1793262964938
        track.surahNumber in setOf(56, 67, 112, 113, 114) ->
            R.drawable.img_col_sleep_1793262966542
        track.surahNumber in setOf(19, 55, 78) ->
            R.drawable.art_col_emotional
        else -> {
            val pool = listOf(
                R.drawable.img_col_focus_1793262964938,
                R.drawable.img_col_beautiful_1793262967630,
                R.drawable.img_col_sleep_1793262966542,
                R.drawable.img_col_duaa_1793262968390,
                R.drawable.art_col_emotional
            )
            pool[(track.surahNumber - 1).mod(pool.size)]
        }
    }
}

@Composable
fun TilawahLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.22f),
                        containerColor
                    )
                )
            )
            .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.58f)) {
            val w = this.size.width
            val h = this.size.height
            val barWidth = w * 0.14f
            val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

            val heights = listOf(0.40f, 0.76f, 1.0f, 0.70f, 0.38f)
            val gap = (w - barWidth * heights.size) / (heights.size - 1).coerceAtLeast(1)

            heights.forEachIndexed { idx, ratio ->
                val barH = h * ratio
                val x = idx * (barWidth + gap)
                val y = (h - barH) / 2f
                drawRoundRect(
                    color = if (idx == 2) primaryColor else textColor.copy(alpha = 0.90f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barH),
                    cornerRadius = corner
                )
            }
        }
    }
}

@Composable
fun ReciterAvatar(
    reciterName: String,
    accentHex: Long,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    arabicName: String? = null,
    shape: Shape = CircleShape
) {
    val initials = remember(reciterName) {
        reciterName.split(" ")
            .filter { it.isNotBlank() && !it.startsWith("Al-", ignoreCase = true) && !it.startsWith("Ad-", ignoreCase = true) }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifEmpty { reciterName.take(2).uppercase() }
    }
    val arabicGlyph = remember(arabicName) {
        arabicName?.trim()?.split(" ")?.lastOrNull { it.isNotBlank() }
    }
    val studioAccent = Color(accentHex)
    val darkCore = Color(0xFF0E1014)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        studioAccent.copy(alpha = 0.92f),
                        studioAccent.copy(alpha = 0.45f),
                        darkCore
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape),
        contentAlignment = Alignment.Center
    ) {
        // Subtle architectural studio mihrab arch & acoustic halo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = w * 0.36f,
                center = Offset(w * 0.5f, h * 0.42f),
                style = Stroke(width = 1.2f)
            )
            val archPath = Path().apply {
                moveTo(w * 0.24f, h * 0.88f)
                lineTo(w * 0.24f, h * 0.48f)
                quadraticTo(w * 0.5f, h * 0.18f, w * 0.76f, h * 0.48f)
                lineTo(w * 0.76f, h * 0.88f)
            }
            drawPath(
                path = archPath,
                color = Color.White.copy(alpha = 0.09f),
                style = Stroke(width = 1.4f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            if (!arabicGlyph.isNullOrBlank() && size >= 68.dp) {
                Text(
                    text = arabicGlyph,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = QuranArabicFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (size.value * 0.24f).sp,
                        lineHeight = (size.value * 0.28f).sp
                    ),
                    color = Color(0xFFFAFAFA),
                    maxLines = 1
                )
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (size.value * 0.12f).coerceAtLeast(10f).sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 1
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.30f).sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFFAFAFA),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.testTag("section_action_${title.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CollectionCard(
    collection: CuratedCollection,
    isSaved: Boolean,
    onOpenCollection: () -> Unit,
    onPlayCollection: () -> Unit,
    onToggleSave: () -> Unit,
    onAddToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpenCollection)
            .padding(4.dp)
            .testTag("collection_card_${collection.id}")
    ) {
        // True 1:1 square album-cover proportions matching Quranify reference
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
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
                            colors = listOf(
                                Color.Black.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            Surface(
                color = Color(0xB809090B),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "${collection.trackIds.size} surahs",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFAFAFA),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayCollection,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(48.dp)
                    .testTag("collection_play_${collection.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play ${collection.title}",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("collection_menu_${collection.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "Collection options for ${collection.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Collection") },
                        onClick = {
                            menuExpanded = false
                            onOpenCollection()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isSaved) "Remove from Saved" else "Save Collection") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleSave()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Collection to Queue") },
                        onClick = {
                            menuExpanded = false
                            onAddToQueue()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReciterCardItem(
    reciter: Reciter,
    isFavorite: Boolean,
    onOpenProfile: () -> Unit,
    onPlayReciter: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenProfile)
            .testTag("reciter_card_${reciter.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .testTag("reciter_fav_${reciter.id}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite ${reciter.displayName}",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered circular reciter portrait with floating bottom-end Play button
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ReciterAvatar(
                        reciterName = reciter.displayName,
                        accentHex = reciter.avatarAccentHex,
                        arabicName = reciter.arabicName,
                        size = 108.dp,
                        shape = CircleShape
                    )

                    FilledIconButton(
                        onClick = onPlayReciter,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp)
                            .padding(4.dp)
                            .testTag("reciter_play_${reciter.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play ${reciter.displayName}",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = reciter.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!reciter.arabicName.isNullOrBlank()) {
                    Text(
                        text = reciter.arabicName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = QuranArabicFontFamily,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val metaParts = listOfNotNull(reciter.country, reciter.recitationStyle)
                if (metaParts.isNotEmpty()) {
                    Text(
                        text = metaParts.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun TajwidMasterCard(
    reciter: Reciter,
    isFavorite: Boolean,
    onOpenProfile: () -> Unit,
    onPlayReciter: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenProfile)
            .testTag("reciter_card_${reciter.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ReciterAvatar(
                reciterName = reciter.displayName,
                accentHex = reciter.avatarAccentHex,
                arabicName = reciter.arabicName,
                size = 74.dp,
                shape = RoundedCornerShape(14.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "TAJWID • ${(reciter.riwayah ?: "Hafs").uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = reciter.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!reciter.arabicName.isNullOrBlank()) {
                    Text(
                        text = reciter.arabicName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = QuranArabicFontFamily,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reciter.neutralDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledIconButton(
                    onClick = onPlayReciter,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reciter_play_${reciter.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play ${reciter.displayName}"
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reciter_fav_${reciter.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite ${reciter.displayName}",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: AudioTrack,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onViewReciter: (() -> Unit)? = null,
    isOfflineSaved: Boolean = false,
    downloadProgress: Float? = null,
    isDownloadPermitted: Boolean = false,
    onDownloadOfflineClick: (() -> Unit)? = null,
    onRemoveOfflineClick: (() -> Unit)? = null,
    onShowLicenceInfo: ((AudioTrack) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCurrentTrack) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onPlayClick)
            .testTag("track_row_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left numeric index or active equalizer badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCurrentTrack) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentTrack) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.Pause,
                        contentDescription = "Currently selected track",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = track.surahNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle: Surah English Title + Clean Reciter / Ayah metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.surahName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrentTrack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val offlineStatusSuffix = when {
                    isOfflineSaved -> " • Offline"
                    downloadProgress != null -> " • ${(downloadProgress * 100).toInt()}%"
                    track.availabilityStatus == TrackAvailability.UNAVAILABLE_DEMO -> " • Demo"
                    else -> ""
                }
                Text(
                    text = "${track.reciterName} • ${track.verseCount} ayahs$offlineStatusSuffix",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Calligraphic Arabic Surah Name + Favorite + More Menu
            Text(
                text = track.surahArabicName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = QuranArabicFontFamily,
                    fontSize = 19.sp
                ),
                color = if (isCurrentTrack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("track_fav_${track.id}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite ${track.surahName}",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("track_menu_${track.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Options for ${track.surahName}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Now") },
                        onClick = {
                            menuOpen = false
                            onPlayClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue") },
                        onClick = {
                            menuOpen = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Custom Playlist") },
                        onClick = {
                            menuOpen = false
                            onAddToPlaylistClick()
                        }
                    )
                    if (onViewReciter != null) {
                        DropdownMenuItem(
                            text = { Text("Open Reciter Profile") },
                            onClick = {
                                menuOpen = false
                                onViewReciter()
                            }
                        )
                    }
                    if (isOfflineSaved && onRemoveOfflineClick != null) {
                        DropdownMenuItem(
                            text = { Text("Remove Personal Offline Copy") },
                            onClick = {
                                menuOpen = false
                                onRemoveOfflineClick()
                            }
                        )
                    } else if (!isOfflineSaved && isDownloadPermitted && onDownloadOfflineClick != null) {
                        DropdownMenuItem(
                            text = { Text("Save for Personal Offline Use") },
                            onClick = {
                                menuOpen = false
                                onDownloadOfflineClick()
                            }
                        )
                    } else if (!isOfflineSaved && !isDownloadPermitted && onDownloadOfflineClick != null) {
                        DropdownMenuItem(
                            text = { Text("Stream Only (${track.sourceProvider.shortBadge} Terms)") },
                            enabled = false,
                            onClick = {}
                        )
                    }
                    if (onShowLicenceInfo != null) {
                        DropdownMenuItem(
                            text = { Text("Source & Licence Details") },
                            onClick = {
                                menuOpen = false
                                onShowLicenceInfo(track)
                            }
                        )
                    }
                }
            }
        }

        if (!isCurrentTrack) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                modifier = Modifier.padding(start = 56.dp, end = 10.dp)
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(14.dp))
                TextButton(onClick = onActionClick) {
                    Text(actionLabel, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppFooterSection(
    onOpenPremium: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var infoDialogTopic by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(20.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FooterColumn(
                heading = "Company",
                items = listOf(
                    "About Tilawah" to {
                        infoDialogTopic = "About Tilawah" to
                            "Tilawah is an independently built Quran audio streaming platform designed for distraction-free listening, authentic Uthmani Quran text, and curated focus & rest collections. Not affiliated with Quranify."
                    },
                    "Data & Attribution" to {
                        infoDialogTopic = "Content Attribution" to
                            "Quran recitations and spoken Adhkar/Dua/Ruqya sessions stream from Al Quran Cloud CDN (islamic.network), EveryAyah, and the public Quran.com v4 API / QuranicAudio QDC with source and licence metadata preserved. Environmental soundscapes are synthesized on-device via Tilawah Acoustic DSP."
                    }
                )
            )
            FooterColumn(
                heading = "Catalog",
                items = listOf(
                    "Reciter Directory" to {
                        infoDialogTopic = "Reciter Catalog" to
                            "Reciter profiles present neutral metadata and verified recordings from public Quran.com chapter reciter endpoints without fabricated discography claims."
                    },
                    "Audio Integrity" to {
                        infoDialogTopic = "Audio Integrity" to
                            "Quran recitations are never altered or mixed into musical tracks. Optional environmental soundscapes remain strictly separate and disabled by default."
                    }
                )
            )
            FooterColumn(
                heading = "Support",
                items = listOf(
                    "Help & Audio Setup" to {
                        infoDialogTopic = "Help & Setup" to
                            "Stream full chapters directly over HTTPS, adjust Arabic typography in Settings, set sleep timers, or sign in with Google to sync playlists across devices."
                    },
                    "Privacy & Security" to {
                        infoDialogTopic = "Privacy & Security" to
                            "Guest preferences and playlists stay locally on your device. When signed in with Google, your library is isolated under strict per-user Cloud Firestore security rules."
                    },
                    "App Settings" to onOpenSettings
                )
            )
            FooterColumn(
                heading = "Plans",
                items = listOf(
                    "Tilawah Plus" to onOpenPremium,
                    "Feature Comparison" to onOpenPremium
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Tilawah • Distraction-Free Quran Audio Streaming",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }

    infoDialogTopic?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { infoDialogTopic = null },
            title = { Text(title) },
            text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { infoDialogTopic = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun FooterColumn(
    heading: String,
    items: List<Pair<String, () -> Unit>>
) {
    Column(modifier = Modifier.width(148.dp)) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        items.forEach { (label, onClick) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onClick)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
