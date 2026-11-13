package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.UserPreferencesState
import com.example.i18n.UiStrings
import com.example.player.AmbientUiState
import com.example.player.GlobalPlayerState
import com.example.ui.navigation.formatDurationSeconds
import com.google.firebase.auth.FirebaseUser

@Composable
fun MoreScreen(
    preferences: UserPreferencesState,
    playerState: GlobalPlayerState,
    ambientState: AmbientUiState,
    currentUser: FirebaseUser?,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onOpenLibrary: () -> Unit,
    onOpenReadQuran: () -> Unit,
    onOpenAmbience: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSourcesAttribution: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_screen"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "more_header") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Library, Quran reader, listening tools, account sync, and settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Account & Sync Card
        item(key = "more_account_card") {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenAccount)
                    .testTag("more_item_account")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "Local Guest Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentUser?.email ?: "Tap to sign in with Google & sync cloud library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Primary Library & Listening Tools Group
        item(key = "more_listening_group") {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MoreMenuRow(
                        icon = Icons.Filled.LibraryMusic,
                        title = strings.navLibrary,
                        subtitle = "${preferences.favoriteTrackIds.size} liked surahs • Playlists & offline copies",
                        onClick = onOpenLibrary,
                        testTag = "more_item_library"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuRow(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = strings.quranTextAndTranslation,
                        subtitle = playerState.currentEntry?.track?.let { "Read ${it.surahName} with translation" } ?: "Open Uthmani text & translations",
                        onClick = onOpenReadQuran,
                        testTag = "more_item_read_quran"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuRow(
                        icon = Icons.Filled.GraphicEq,
                        title = strings.ambientSounds,
                        subtitle = if (ambientState.isPlaying) "Active background soundscape" else "Independent background audio mixer",
                        badgeText = if (ambientState.isPlaying) "Active" else null,
                        onClick = onOpenAmbience,
                        testTag = "more_item_ambience"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuRow(
                        icon = Icons.Filled.Bedtime,
                        title = strings.sleepTimer,
                        subtitle = playerState.sleepTimerRemainingSec?.let { "Remaining: ${formatDurationSeconds(it)}" } ?: "Schedule automatic playback pause",
                        badgeText = playerState.sleepTimerRemainingSec?.let { formatDurationSeconds(it) },
                        onClick = onOpenSleepTimer,
                        testTag = "more_item_sleep_timer"
                    )
                }
            }
        }

        // Insights, Plus, Settings & Attribution Group
        item(key = "more_system_group") {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MoreMenuRow(
                        icon = Icons.Filled.BarChart,
                        title = strings.navInsights,
                        subtitle = "Verified listening streaks & reciter history",
                        onClick = onOpenInsights,
                        testTag = "more_item_insights"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuRow(
                        icon = Icons.Filled.WorkspacePremium,
                        title = strings.premiumTitle,
                        subtitle = if (preferences.isDemoPremium) "Demo Plus active" else "Ambient studio & extended insights",
                        onClick = onOpenPremium,
                        testTag = "more_item_premium"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuRow(
                        icon = Icons.Filled.Settings,
                        title = strings.navSettings,
                        subtitle = "Theme, language, Arabic font scale & audio source",
                        onClick = onOpenSettings,
                        testTag = "more_item_settings"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuRow(
                        icon = Icons.Filled.Info,
                        title = "Audio Sources & Attribution",
                        subtitle = "Al Quran Cloud, EveryAyah, Quran.com v4 & licences",
                        onClick = onOpenSourcesAttribution,
                        testTag = "more_item_attribution"
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    badgeText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (badgeText != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
