package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.AuthManager
import com.example.data.firebase.authStateFlow
import com.example.data.local.GuestLibraryDataStore
import com.example.data.local.OfflineAudioCacheManager
import com.example.i18n.LocalizationProvider
import com.example.ui.layout.AdaptiveAppShell
import com.example.ui.screens.AuthGateWelcomeScreen
import com.example.ui.theme.TilawahTheme
import com.example.viewmodel.TilawahViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TilawahRootApp()
        }
    }
}

@Composable
fun TilawahRootApp(
    viewModelOverride: TilawahViewModel? = null,
    initialGuestBypass: Boolean = false
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val databaseId = remember(appContext) {
        try {
            appContext.getString(R.string.firestore_database_id)
        } catch (_: Exception) {
            "(default)"
        }
    }

    val viewModel: TilawahViewModel = viewModelOverride ?: viewModel(
        factory = viewModelFactory {
            initializer {
                TilawahViewModel(
                    databaseId = databaseId,
                    guestStore = GuestLibraryDataStore(appContext),
                    offlineCacheManager = OfflineAudioCacheManager(appContext)
                )
            }
        }
    )

    val authFlow = remember {
        try {
            Firebase.auth.authStateFlow()
        } catch (_: Exception) {
            flowOf<FirebaseUser?>(null)
        }
    }
    val initialAuthUser = remember {
        try {
            Firebase.auth.currentUser
        } catch (_: Exception) {
            null
        }
    }

    val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = initialAuthUser)
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val strings = remember(preferences.preferredLanguage) {
        LocalizationProvider.strings(preferences.preferredLanguage)
    }

    var hasEnteredGuestMode by rememberSaveable { mutableStateOf(initialGuestBypass) }
    val scope = rememberCoroutineScope()

    // Attempt non-intrusive silent Google Sign-In once at launch if not yet signed in
    LaunchedEffect(Unit) {
        if (currentUser == null && !initialGuestBypass) {
            try {
                val cm = CredentialManager.create(context)
                AuthManager.attemptAutoSignIn(
                    context = context,
                    credentialManager = cm,
                    scope = scope,
                    onAuthSuccess = {},
                    onUnauthenticated = {}
                )
            } catch (_: Exception) {
            }
        }
    }

    // Bind Cloud Firestore listeners ONLY when authenticated with a valid Google UID
    LaunchedEffect(currentUser?.uid) {
        viewModel.onAuthenticatedUserChanged(currentUser?.uid)
    }

    TilawahTheme(
        darkTheme = preferences.darkTheme,
        isRtl = preferences.preferredLanguage.isRtl
    ) {
        if (currentUser == null && !hasEnteredGuestMode) {
            AuthGateWelcomeScreen(
                strings = strings,
                onAuthSuccess = {},
                onContinueAsLocalGuest = { hasEnteredGuestMode = true }
            )
        } else {
            AdaptiveAppShell(
                viewModel = viewModel,
                currentUser = currentUser,
                onSignedOutToWelcome = { hasEnteredGuestMode = false }
            )
        }
    }
}
