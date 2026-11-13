package com.example.ui.navigation

sealed interface AppDestination {
    val routeKey: String

    data object Home : AppDestination {
        override val routeKey: String = "home"
    }

    data object Collections : AppDestination {
        override val routeKey: String = "collections"
    }

    data class CollectionDetail(val collectionId: String) : AppDestination {
        override val routeKey: String = "collection_detail/$collectionId"
    }

    data object Reciters : AppDestination {
        override val routeKey: String = "reciters"
    }

    data class ReciterDetail(val reciterId: String) : AppDestination {
        override val routeKey: String = "reciter_detail/$reciterId"
    }

    data object Search : AppDestination {
        override val routeKey: String = "search"
    }

    data object Library : AppDestination {
        override val routeKey: String = "library"
    }

    data object Insights : AppDestination {
        override val routeKey: String = "insights"
    }

    data object Premium : AppDestination {
        override val routeKey: String = "premium"
    }

    data object Settings : AppDestination {
        override val routeKey: String = "settings"
    }

    data object More : AppDestination {
        override val routeKey: String = "more"
    }
}

fun AppDestination.topLevelHighlightKey(): String = when (this) {
    is AppDestination.Home -> "home"
    is AppDestination.Collections, is AppDestination.CollectionDetail -> "collections"
    is AppDestination.Reciters, is AppDestination.ReciterDetail -> "reciters"
    is AppDestination.Search -> "search"
    is AppDestination.Library -> "library"
    is AppDestination.Insights -> "insights"
    is AppDestination.Premium -> "premium"
    is AppDestination.Settings -> "settings"
    is AppDestination.More -> "more"
}

fun AppDestination.mobileBottomHighlightKey(): String = when (this) {
    is AppDestination.Home -> "home"
    is AppDestination.Collections, is AppDestination.CollectionDetail -> "collections"
    is AppDestination.Reciters, is AppDestination.ReciterDetail -> "reciters"
    is AppDestination.Search -> "search"
    is AppDestination.More,
    is AppDestination.Library,
    is AppDestination.Insights,
    is AppDestination.Premium,
    is AppDestination.Settings -> "more"
}

fun formatDurationSeconds(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val hrs = safe / 3600
    val mins = (safe % 3600) / 60
    val secs = safe % 60
    return if (hrs > 0) {
        String.format("%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

fun formatDurationMs(ms: Long): String = formatDurationSeconds((ms.coerceAtLeast(0L) / 1000L).toInt())

fun formatCompactDurationMs(ms: Long): String {
    val safe = (ms.coerceAtLeast(0L) / 1000L).toInt()
    val hrs = safe / 3600
    val mins = (safe % 3600) / 60
    val secs = safe % 60
    return if (hrs > 0) {
        String.format("%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%d:%02d", mins, secs)
    }
}
