package com.example.gamelibrary.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamelibrary.GoogleAuthHelper
import com.example.gamelibrary.auth.EpicAuth
import com.example.gamelibrary.auth.SteamAuth
import com.example.gamelibrary.auth.XboxAuth
import com.example.gamelibrary.data.AccountRepository
import com.example.gamelibrary.data.SettingsRepository
import com.example.gamelibrary.model.Platform
import com.example.gamelibrary.model.SettingsUiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

/**
 * Single source of truth for the Settings screen. Combines the persisted
 * DataStore settings with the in-memory linked-account state into one
 * SettingsUiState, following the "single UI state" pattern for Compose
 * screens (The IIE, 2026).
 *
 * Also responsible for persisting the linked Steam ID to Firebase Realtime
 * Database (via GoogleAuthHelper) once AccountRepository confirms a link —
 * AccountRepository itself has no Firebase dependency, so this class is
 * the seam between the two (Gosavi, 2020).
 */
class SettingsViewModel(
    private val settingsRepo: SettingsRepository,
    private val accountRepo: AccountRepository
) : ViewModel() {

    private val authHelper = GoogleAuthHelper()

    // ---------------------------------------------------------------
    // UI STATE
    // ---------------------------------------------------------------

    // combine() merges the two independent flows (DataStore settings and
    // in-memory linked accounts) into a single state object every time
    // either one changes
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepo.settingsFlow,
        accountRepo.linkedAccountsFlow
    ) { settings, accounts ->

        settings.copy(
            linkedAccounts = accounts
        )

    }.stateIn(
        scope = viewModelScope,

        // Keeps the flow alive for 5 seconds after the last subscriber
        // leaves, so rotating the screen or switching tabs briefly
        // doesn't reset the state
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    // ---------------------------------------------------------------
    // USER CHANGED (login, logout, or switched account)
    // ---------------------------------------------------------------

    /**
     * Call this whenever the signed-in Firebase user changes to a
     * new, non-null user (fresh login, registration, or switching
     * accounts while the app is running).
     *
     * AccountRepository is a singleton for the whole app process, so
     * any links left over from a previously signed-in user must be
     * cleared before restoring whatever links actually belong to
     * this user.
     */
    fun onUserChanged() {

        accountRepo.clearAll()

        restoreSteamLink()
    }

    /**
     * Call this when the user signs out, so a subsequent sign-in by
     * a different user (without an app restart) doesn't inherit any
     * linked accounts from the previous session.
     */
    fun clearAccounts() {
        accountRepo.clearAll()
    }

    // ---------------------------------------------------------------
    // STEAM RESTORE
    // ---------------------------------------------------------------

    /**
     * Restores the previously linked Steam account. Firebase Auth
     * remembers the user's login, Firebase Realtime Database remembers
     * the SteamID64, so Steam's OpenID login does not need to run again
     * (Petrescu, 2021; Valve Corporation, 2026a).
     *
     * Assumes accountRepo has already been cleared for the current
     * user by onUserChanged() before this runs.
     */
    fun restoreSteamLink() {

        val uid = Firebase.auth.currentUser?.uid

        if (uid == null) {

            Log.d(
                TAG,
                "No Firebase user - cannot restore Steam link"
            )

            return
        }

        Log.d(
            TAG,
            "Checking Firebase for saved SteamID64..."
        )

        // get()-style one-time read of the saved SteamID64
        // (Petrescu, 2021)
        authHelper.readSteamId64(

            uid = uid,

            onResult = { steamId64 ->

                if (!steamId64.isNullOrBlank()) {

                    Log.d(
                        TAG,
                        "Found saved SteamID64: $steamId64"
                    )

                    viewModelScope.launch {

                        try {

                            accountRepo.restoreSteamLink(
                                steamId64
                            )

                        } catch (e: Exception) {

                            Log.e(
                                TAG,
                                "Failed to restore Steam link",
                                e
                            )
                        }
                    }

                } else {

                    Log.d(
                        TAG,
                        "No Steam account linked to this Firebase user"
                    )
                }
            },

            onError = { e ->

                Log.e(
                    TAG,
                    "Failed to read SteamID64 from Firebase",
                    e
                )
            }
        )
    }

    // ---------------------------------------------------------------
    // STEAM CALLBACK
    // ---------------------------------------------------------------

    // Runs once when Steam redirects back to the app via the Firebase
    // Hosting bridge page and gamelibrary:// deep link
    // (Google, 2026; Valve Corporation, 2026a)
    fun onSteamCallback(
        callbackUri: Uri
    ) {

        val params = SteamAuth.extractOpenIdParams(
            callbackUri
        )

        viewModelScope.launch {

            try {

                val steamId64 =
                    accountRepo.completeSteamLink(
                        params
                    )

                val uid =
                    Firebase.auth.currentUser?.uid

                if (uid == null) {

                    Log.e(
                        TAG,
                        "Steam linked but no Firebase user is signed in"
                    )

                    return@launch
                }

                // Persist the SteamID64 so the Wishlist screen (which
                // reads independently from Firebase) and future app
                // launches (restoreSteamLink) can both find it
                // (GeeksForGeeks, 2023; Gosavi, 2020)
                authHelper.saveSteamId64(

                    uid = uid,
                    steamId64 = steamId64,

                    onSuccess = {

                        Log.d(
                            TAG,
                            "Steam ID saved to Firebase: $steamId64"
                        )
                    },

                    onError = { e ->

                        Log.e(
                            TAG,
                            "Failed to save Steam ID to Firebase",
                            e
                        )
                    }
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Steam link failed",
                    e
                )
            }
        }
    }

    // ---------------------------------------------------------------
    // EPIC CALLBACK
    // ---------------------------------------------------------------

    fun onEpicCallback(
        callbackUri: Uri
    ) {

        viewModelScope.launch {

            try {

                val code =
                    callbackUri.getQueryParameter("code")
                        ?: throw IllegalStateException(
                            "Epic callback missing authorization code"
                        )

                val params = mapOf(
                    "code" to code
                )

                accountRepo.completeEpicLink(
                    params
                )

                Log.d(
                    TAG,
                    "Epic account linked successfully"
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Epic link failed",
                    e
                )
            }
        }
    }

    // ---------------------------------------------------------------
    // XBOX CALLBACK
    // ---------------------------------------------------------------

    fun onXboxCallback(
        callbackUri: Uri
    ) {

        viewModelScope.launch {

            try {

                val code =
                    callbackUri.getQueryParameter("code")
                        ?: throw IllegalStateException(
                            "Xbox callback missing authorization code"
                        )

                val params = mapOf(
                    "code" to code
                )

                accountRepo.completeXboxLink(
                    params
                )

                Log.d(
                    TAG,
                    "Xbox account linked successfully"
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Xbox link failed",
                    e
                )
            }
        }
    }

    // ---------------------------------------------------------------
    // PSN
    // ---------------------------------------------------------------

    fun simulatePsnLink() {

        viewModelScope.launch {

            try {

                accountRepo.simulatePsnLink()

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "PSN simulation failed",
                    e
                )
            }
        }
    }

    // ---------------------------------------------------------------
    // UNLINK ACCOUNT
    // ---------------------------------------------------------------

    // Unlinks locally, and for Steam specifically also clears the saved
    // SteamID64 in Firebase so the Wishlist screen doesn't keep showing
    // stale data after unlinking (GeeksForGeeks, 2023)
    fun onUnlinkAccount(
        platform: Platform
    ) {

        accountRepo.unlink(
            platform
        )

        if (platform == Platform.STEAM) {

            val uid =
                Firebase.auth.currentUser?.uid

            if (uid != null) {

                authHelper.saveSteamId64(

                    uid = uid,
                    steamId64 = "",

                    onSuccess = {

                        Log.d(
                            TAG,
                            "Cleared SteamID64 in Firebase after unlink"
                        )
                    },

                    onError = { e ->

                        Log.e(
                            TAG,
                            "Failed to clear SteamID64 after unlink",
                            e
                        )
                    }
                )
            }
        }
    }

    // ---------------------------------------------------------------
    // LOGIN URL
    // ---------------------------------------------------------------

    // Returns the platform-specific login URL to open in a Custom Tab,
    // or null for PSN, which has no such URL (Android Developers, 2026)
    fun loginUrlFor(
        platform: Platform
    ): String? {

        return when (platform) {

            Platform.STEAM ->
                SteamAuth.buildLoginUrl()

            Platform.EPIC ->
                EpicAuth.buildLoginUrl()

            Platform.XBOX ->
                XboxAuth.buildLoginUrl()

            Platform.PSN ->
                null
        }
    }

    // ---------------------------------------------------------------
    // SETTINGS
    // ---------------------------------------------------------------

    // Simple pass-throughs to SettingsRepository's DataStore writes
    fun setTheme(
        theme: String
    ) {

        viewModelScope.launch {

            settingsRepo.setTheme(
                theme
            )
        }
    }

    fun setLanguage(
        code: String
    ) {

        viewModelScope.launch {

            settingsRepo.setLanguage(
                code
            )
        }
    }

    fun setNotificationsEnabled(
        enabled: Boolean
    ) {

        viewModelScope.launch {

            settingsRepo.setNotifications(
                enabled
            )
        }
    }
}

// ---------------------------------------------------------------
// VIEWMODEL FACTORY
// ---------------------------------------------------------------

// SettingsViewModel needs constructor arguments, so the default
// ViewModelProvider can't create it — this factory supplies them
// (The IIE, 2026)
class SettingsViewModelFactory(
    private val settingsRepo: SettingsRepository,
    private val accountRepo: AccountRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                SettingsViewModel::class.java
            )
        ) {

            return SettingsViewModel(
                settingsRepo = settingsRepo,
                accountRepo = accountRepo
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
// REFERENCE LIST

// Android Developers, 2026a. *About Credential Manager*. [online] Available at: <https://developer.android.com/guide/user-data> [Accessed 21 September 2026]
// Android Developers, 2026b. *androidx.compose.material3 | API reference*. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary> [Accessed 14 September 2026]
// Android Developers, 2026c. *BasicTextField | API reference*. [online] Available at: <https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/BasicTextField.composable> [Accessed 14 September 2026]
// Android Developers, 2026d. *Buttons – Jetpack Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/components/button> [Accessed 14 September 2026]
// Android Developers, 2026e. *Configure text fields – Jetpack Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/text/user-input> [Accessed 14 September 2026]
// Android Developers, 2026f. *Resources in Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/resources> [Accessed 14 September 2026]
// Android Developers, 2026g. *Work with fonts – Jetpack Compose*. [online] Available at: <https://developer.android.com/develop/ui/compose/text/fonts> [Accessed 14 September 2026]
// Android Developers, 2026h. *Overview of Android Custom Tabs*. [online] Available at: <https://developer.android.com/develop/ui/views/layout/webapps/overview-of-android-custom-tabs> [Accessed 17 September 2026]
// GeeksForGeeks, 2023. *Android Jetpack Compose – Add Data to Firebase Realtime Database*. [online] Available at: <https://www.geeksforgeeks.org/kotlin/android-jetpack-compose-add-data-to-firebase-realtime-database/> [Accessed 28 April 2026]
// Google, 2026. *Firebase Documentation – Get started with Firebase Hosting*. [online] Available at: <https://firebase.google.com/docs/hosting/quickstart> [Accessed 17 September 2026]
// Gosavi, S., 2020. *Upload data in Realtime Database and retrieve it*. [blog] November. Available at: <https://sanathgosavi.blogspot.com/2020/11/upload-data-in-realtime-database-and.html> [Accessed 27 April 2026]
// Kotlin, 2026d. *Data classes*. [online] Available at: <https://kotlinlang.org/docs/data-classes.html> [Accessed 27 April 2026]
// Microsoft, 2026. *Link Steam account - PlayFab | Microsoft Learn*. [online] Available at: <https://learn.microsoft.com/en-us/rest/api/playfab/client/account-management/link-steam-account?view=playfab-rest> [Accessed 16 September 2026]
// Okpukoro, T.J., 2024. *File Uploading in Android*. [blog] 23 July. Available at: <https://uploadcare.com/blog/file-uploading-in-android/> [Accessed 20 April 2026]
// Petrescu, A., 2021. *How to read data from Firebase Realtime Database using get()*. [blog] 18 February. Available at: <https://medium.com/firebase-tips-tricks/how-to-read-data-from-firebase-realtime-database-using-get-269ef3e179c5> [Accessed 1 June 2026]
// Smartherd, 2018. *Shape your Image — Circle, Rounded Square or Cuts at the Corner of Image in Android*. [blog] Available at: <https://medium.com/smartherd/shape-your-image-circle-rounded-square-or-cuts-at-the-corner-of-image-in-android-cf92f38c217f> [Accessed 20 May 2026]
// Square, 2026. *Retrofit*. [online] Available at: <https://square.github.io/retrofit/> [Accessed 16 September 2026>
// The IIE, 2026a. *Programming 3D* [PROG7314 Module Manual]. The Independent Institute of Education: Unpublished
// The IIE, 2026b. *Programming 3C* [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished
// Turtle Code, 2022. *Add Photo From Gallery - Kotlin Android Tutorial*. [video online] Available at: <https://www.youtube.com/watch?v=NqWDnqz6-0> [Accessed 24 April 2026]
// Valve Corporation, 2026a. *Steamworks Documentation – Web Browser based authentication with OpenID*. [online] Available at: <https://partner.steamgames.com/doc/features/auth> [Accessed 20 September 2026]
// Valve Corporation, 2026b. *Steamworks Documentation – Web API Overview*. [online] Available at: <https://partner.steamgames.com/doc/webapi_overview> [Accessed 19 September 2026]
// xPaw, 2026a. *Steam Web API Documentation – IStoreBrowseService*. [online] Available at: <https://steamapi.xpaw.me/#IStoreBrowseService> [Accessed 18 September 2026]
// xPaw, 2026b. *Steam Web API Documentation – IWishlistService*. [online] Available at: <https://steamapi.xpaw.me/#IWishlistService> [Accessed 18 September 2026>