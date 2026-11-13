package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AudioSourceProvider
import com.example.data.model.ListeningInsightsSummary
import com.example.data.model.RepeatMode
import com.example.data.model.UserPreferencesState
import com.example.i18n.UiStrings
import com.example.player.AmbientUiState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.navigation.formatDurationSeconds
import com.example.ui.theme.QuranArabicFontFamily
import com.google.firebase.auth.FirebaseUser

@Composable
fun InsightsScreen(
    summary: ListeningInsightsSummary,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onOpenReciter: (String) -> Unit
) {
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("insights_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "insights_header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.insightsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.insightsDisclaimer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "insights_metrics_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    label = strings.todayListening,
                    value = formatDurationSeconds(summary.todaySeconds),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    label = strings.weekListening,
                    value = formatDurationSeconds(summary.weekSeconds),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    label = strings.sessionsCount,
                    value = summary.distinctSessions.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (summary.distinctSessions == 0) {
            item(key = "insights_empty") {
                EmptyStateCard(
                    title = "No Playback Sessions Recorded Yet",
                    subtitle = "Start listening to any Surah or Collection. Verified audio sessions (>= 5 seconds) automatically populate your 7-day trend."
                )
            }
        } else {
            item(key = "insights_chart") {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "7-Day Audio Duration Trend",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val maxSeconds = (summary.dailyTrend.maxOfOrNull { it.secondsListened } ?: 1).coerceAtLeast(60)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val count = summary.dailyTrend.size.coerceAtLeast(1)
                                val slotWidth = size.width / count
                                val barWidth = (slotWidth * 0.48f).coerceAtMost(42.dp.toPx())
                                val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                                summary.dailyTrend.forEachIndexed { idx, pt ->
                                    val x = idx * slotWidth + (slotWidth - barWidth) / 2f
                                    drawRoundRect(
                                        color = trackColor,
                                        topLeft = Offset(x, 0f),
                                        size = Size(barWidth, size.height),
                                        cornerRadius = corner
                                    )
                                    val ratio = (pt.secondsListened.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
                                    val barH = (size.height * ratio).coerceAtLeast(if (pt.secondsListened > 0) 6.dp.toPx() else 0f)
                                    if (barH > 0f) {
                                        drawRoundRect(
                                            color = barColor,
                                            topLeft = Offset(x, size.height - barH),
                                            size = Size(barWidth, barH),
                                            cornerRadius = corner
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            summary.dailyTrend.forEach { pt ->
                                Text(
                                    text = pt.dayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (summary.frequentReciters.isNotEmpty()) {
                item(key = "frequent_reciters_header") {
                    SectionHeaderRow(title = "Frequently Played Reciters")
                }
                items(items = summary.frequentReciters, key = { it.reciterId }) { stat ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReciter(stat.reciterId) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stat.reciterName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${stat.sessionCount} recorded events",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatDurationSeconds(stat.totalSeconds),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PremiumScreen(
    preferences: UserPreferencesState,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onToggleDemoPremium: (Boolean) -> Unit,
    onOpenAmbience: () -> Unit,
    onOpenInsights: () -> Unit
) {
    val faqs = remember {
        listOf(
            "Is Quran recitation streaming free?" to
                "Yes. Every canonical Surah, reciter profile, Uthmani scripture panel, translation, favorite, and playlist is accessible at no cost.",
            "Are ambient soundscapes mixed into the Quran audio?" to
                "Never. Optional environmental soundscapes (soft rain, breeze, water) are synthesized separately on-device, remain off by default, and can be muted with one tap.",
            "How does billing work in this build?" to
                "No real payment gateway is connected in this build. You can toggle the Demo Plus Preview switch below to inspect Plus badges without any charge or fake checkout."
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("premium_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "premium_hero") {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "MEMBERSHIP OVERVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.premiumTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.premiumSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onOpenAmbience) {
                            Text("Open Soundscapes")
                        }
                        OutlinedButton(onClick = onOpenInsights) {
                            Text("View Insights")
                        }
                    }
                }
            }
        }

        item(key = "comparison_table") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Feature Comparison", style = MaterialTheme.typography.titleLarge)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    listOf(
                        Triple("114 Surahs & Verified Reciters", "Included", "Included"),
                        Triple("Uthmani Scripture & Translations", "Included", "Included"),
                        Triple("Cloud Sync & Custom Playlists", "Included", "Included"),
                        Triple("Synthesized Ambient Soundscapes", "Available", "Highlighted"),
                        Triple("7-Day Listening Analytics", "Available", "Extended")
                    ).forEach { (feature, freeCol, plusCol) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(feature, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.54f))
                            Text(freeCol, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.23f))
                            Row(modifier = Modifier.weight(0.23f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(plusCol, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        item(key = "demo_billing_disclosure") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Honest Billing Disclosure (Demo Preview Mode)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Billing is not configured in this build; no prices are quoted or charged. Toggle Demo Plus Preview to test Plus account states.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = preferences.isDemoPremium,
                        onCheckedChange = onToggleDemoPremium,
                        modifier = Modifier.testTag("demo_premium_switch")
                    )
                }
            }
        }

        item(key = "premium_faq") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeaderRow(title = "Frequently Asked Questions")
                faqs.forEach { (q, a) ->
                    var expanded by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(q, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            if (expanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun SettingsScreen(
    preferences: UserPreferencesState,
    ambientState: AmbientUiState,
    currentUser: FirebaseUser?,
    strings: UiStrings,
    bottomReservedPadding: Dp,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onSetArabicScale: (Float) -> Unit,
    onSetAutoplayNext: (Boolean) -> Unit,
    onSetDefaultRepeat: (RepeatMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onOpenAmbienceDialog: () -> Unit,
    onOpenSleepTimerDialog: () -> Unit,
    onOpenAccountDialog: () -> Unit,
    onSetPreferredAudioSource: (AudioSourceProvider) -> Unit = {},
    onOpenSourcesAttributionDialog: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = bottomReservedPadding + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "settings_header") {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Appearance & Reading
        item(key = "settings_appearance") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(strings.appearanceSection, style = MaterialTheme.typography.titleMedium)

                    SettingToggleRow(
                        label = strings.darkThemeLabel,
                        subtitle = "Rich charcoal surfaces designed for calm listening",
                        checked = preferences.darkTheme,
                        onCheckedChange = onSetDarkTheme,
                        testTag = "setting_dark_theme_switch"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    SettingToggleRow(
                        label = strings.reducedMotionLabel,
                        subtitle = "Minimize interface transitions",
                        checked = preferences.reducedMotion,
                        onCheckedChange = onSetReducedMotion,
                        testTag = "setting_reduced_motion_switch"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${strings.arabicTextSizeLabel}: ${(preferences.arabicFontScale * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Slider(
                            value = preferences.arabicFontScale,
                            onValueChange = onSetArabicScale,
                            valueRange = 0.85f..1.65f,
                            modifier = Modifier.testTag("setting_arabic_scale_slider")
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = QuranArabicFontFamily,
                                    fontSize = (24f * preferences.arabicFontScale).sp
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Playback & Audio
        item(key = "settings_playback") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(strings.playbackSection, style = MaterialTheme.typography.titleMedium)

                    SettingToggleRow(
                        label = strings.autoplayNextLabel,
                        subtitle = "Continue seamlessly through collections and queues",
                        checked = preferences.autoplayNext,
                        onCheckedChange = onSetAutoplayNext,
                        testTag = "setting_autoplay_switch"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Default Repeat Behavior", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                RepeatMode.OFF to "Off",
                                RepeatMode.ALL to "Repeat Queue",
                                RepeatMode.ONE to "Repeat Surah"
                            ).forEach { (mode, label) ->
                                FilterChip(
                                    selected = preferences.defaultRepeatMode == mode,
                                    onClick = { onSetDefaultRepeat(mode) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onOpenAmbienceDialog) {
                            Text("${strings.ambientSounds} (${if (ambientState.isPlaying) ambientState.activeSoundId else "Off"})")
                        }
                        OutlinedButton(onClick = onOpenSleepTimerDialog) {
                            Text(strings.sleepTimer)
                        }
                    }
                }
            }
        }

        // Verified Quran Audio Providers & Attribution
        item(key = "settings_audio_sources") {
            val activeSource = AudioSourceProvider.fromCode(preferences.preferredAudioSourceCode)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Verified Audio Sources & Licences", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Choose your preferred free Quran audio library. Tilawah verifies stream availability via HTTP probes before playback and automatically offers verified fallbacks if an edition is unavailable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AudioSourceProvider.AL_QURAN_CLOUD to "Al Quran Cloud (Full Surah + Offline Permitted)",
                            AudioSourceProvider.QURAN_FOUNDATION_API to "Quran.com API v4 (Segment Timings)",
                            AudioSourceProvider.EVERY_AYAH to "EveryAyah (Verse Audio Priority)"
                        ).forEach { (provider, label) ->
                            FilterChip(
                                selected = activeSource == provider,
                                onClick = { onSetPreferredAudioSource(provider) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("provider_chip_${provider.code}")
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenSourcesAttributionDialog,
                        modifier = Modifier.testTag("settings_attribution_button")
                    ) {
                        Text("Inspect Audio Sources, Licences & Diagnostics")
                    }
                }
            }
        }

        // Language Selection (8 supported languages)
        item(key = "settings_language") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(strings.languageSection, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = preferences.preferredLanguage == lang,
                                onClick = { onSetLanguage(lang) },
                                label = { Text("${lang.nativeName} (${lang.code.uppercase()})") },
                                modifier = Modifier.testTag("lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        // Account & Persistence
        item(key = "settings_account") {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentUser != null) {
                                "Signed in as ${currentUser.displayName ?: currentUser.email ?: "Listener"}"
                            } else {
                                "Local Guest Mode"
                            },
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (currentUser != null) {
                                "Synced with Cloud Firestore"
                            } else {
                                "Preferences and playlists stored locally via DataStore"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenAccountDialog,
                        modifier = Modifier.testTag("settings_account_button")
                    ) {
                        Text(if (currentUser != null) "Manage" else "Sign In")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
