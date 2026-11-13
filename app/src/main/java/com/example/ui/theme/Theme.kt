package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val TilawahDarkColorScheme = darkColorScheme(
    primary = MutedSageAccent,
    onPrimary = OnMutedSage,
    primaryContainer = MutedSageContainer,
    onPrimaryContainer = MutedSageBright,
    secondary = SecondaryMutedText,
    onSecondary = ObsidianBackground,
    secondaryContainer = SecondaryCardSurface,
    onSecondaryContainer = PrimaryLightText,
    tertiary = WarmGoldHighlight,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = PrimaryLightText,
    surface = PrimaryCardSurface,
    onSurface = PrimaryLightText,
    surfaceVariant = SecondaryCardSurface,
    onSurfaceVariant = SecondaryMutedText,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = SidebarCharcoal,
    surfaceContainer = PrimaryCardSurface,
    surfaceContainerHigh = SecondaryCardSurface,
    surfaceContainerHighest = HoverElevatedSurface,
    outline = SubtleBorderDivider,
    outlineVariant = TertiaryDimText,
    error = AccessibleErrorRed,
    errorContainer = AccessibleErrorContainer,
    onError = ObsidianBackground,
    onErrorContainer = PrimaryLightText
)

private val TilawahLightColorScheme = lightColorScheme(
    primary = LightSageAccent,
    onPrimary = Color.White,
    primaryContainer = LightSageContainer,
    onPrimaryContainer = LightSageAccent,
    secondary = LightSecondaryText,
    onSecondary = Color.White,
    secondaryContainer = LightCardSecondary,
    onSecondaryContainer = LightPrimaryText,
    tertiary = WarmGoldHighlight,
    background = LightBackground,
    onBackground = LightPrimaryText,
    surface = LightCardPrimary,
    onSurface = LightPrimaryText,
    surfaceVariant = LightCardSecondary,
    onSurfaceVariant = LightSecondaryText,
    surfaceContainerLowest = LightBackground,
    surfaceContainerLow = LightSidebarSurface,
    surfaceContainer = LightCardPrimary,
    surfaceContainerHigh = LightCardSecondary,
    surfaceContainerHighest = LightBorderDivider,
    outline = LightBorderDivider,
    outlineVariant = LightSecondaryText,
    error = Color(0xFFB3261E),
    onError = Color.White
)

val TilawahShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TilawahTheme(
    darkTheme: Boolean = true,
    isRtl: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TilawahDarkColorScheme else TilawahLightColorScheme
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TilawahTypography,
            shapes = TilawahShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    TilawahTheme(darkTheme = darkTheme, content = content)
}
