package com.example.gamelibrary.ui

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamelibrary.AccentPurple
import com.example.gamelibrary.PixelFont
import com.example.gamelibrary.PixelGradientBackground
import com.example.gamelibrary.PixelPillButton
import com.example.gamelibrary.R
import com.example.gamelibrary.model.LinkedAccount
import com.example.gamelibrary.model.Platform
import com.example.gamelibrary.util.LocaleHelper

/**
 * Settings screen, styled to match the app's pixel-art gradient theme
 * (see MainActivity.kt for PixelGradientBackground / PixelFont /
 * AccentPurple / PixelPillButton). Data and logic are unchanged — only
 * the visuals were reworked, following the same state-hoisting pattern
 * where the UI itself holds no data, only what it is given
 * (Android Developers, 2026b).
 *
 * Handles:
 * - Language
 * - Theme
 * - Notifications
 * - Steam account linking
 * - Epic account linking
 * - Xbox account linking
 * - Simulated PSN linking
 * - Account unlinking
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {

    // collectAsStateWithLifecycle only collects the flow while the screen
    // is actually visible, stopping automatically when it isn't
    // (Android Developers, 2026b)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Tracks which platform (if any) the unlink confirmation dialog is
    // currently showing for
    var pendingUnlink by remember {
        mutableStateOf<Platform?>(null)
    }

    var showPsnSimulationDialog by remember {
        mutableStateOf(false)
    }

    // Same gradient/circle background used across the rest of the app,
    // instead of the plain MaterialTheme surface
    PixelGradientBackground {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            item {
                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Settings",
                    fontFamily = PixelFont,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(20.dp))
            }

            // -----------------------------------------------------------
            // GENERAL
            // -----------------------------------------------------------

            item {

                PixelSettingsSection(
                    title = stringResource(
                        R.string.section_general
                    )
                ) {

                    PixelDropdownRow(
                        label = stringResource(
                            R.string.label_language
                        ),

                        selectedValue = state.language,

                        options = listOf(
                            "en" to stringResource(
                                R.string.language_en
                            ),

                            "af" to stringResource(
                                R.string.language_af
                            ),

                            "zu" to stringResource(
                                R.string.language_zu
                            )
                        ),

                        onSelect = { code ->

                            // Persist first, then recreate — the new base
                            // context is read from disk on the very next
                            // line, so the write must complete
                            // synchronously before recreate() fires
                            LocaleHelper.persistLanguage(
                                context,
                                code
                            )

                            viewModel.setLanguage(
                                code
                            )

                            // Rebuilds the Activity so every
                            // stringResource() call re-resolves against
                            // the new locale immediately
                            // (Android Developers, 2026f)
                            (context as? Activity)?.recreate()
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // -----------------------------------------------------------
            // APPEARANCE
            // -----------------------------------------------------------

            item {

                PixelSettingsSection(
                    title = stringResource(
                        R.string.section_appearance
                    )
                ) {

                    PixelDropdownRow(
                        label = stringResource(
                            R.string.label_theme
                        ),

                        selectedValue = state.theme,

                        options = listOf(
                            "light" to stringResource(
                                R.string.theme_light
                            ),

                            "dark" to stringResource(
                                R.string.theme_dark
                            ),

                            "system" to stringResource(
                                R.string.theme_system
                            )
                        ),

                        onSelect = { theme ->

                            viewModel.setTheme(
                                theme
                            )
                        }
                    )

                    PixelSwitchRow(
                        label = stringResource(
                            R.string.label_notifications
                        ),

                        checked = state.notificationsEnabled,

                        onCheckedChange = { enabled ->

                            viewModel.setNotificationsEnabled(
                                enabled
                            )
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // -----------------------------------------------------------
            // LINKED ACCOUNTS
            // -----------------------------------------------------------

            item {

                Text(
                    text = stringResource(
                        R.string.section_linked_accounts
                    ),
                    fontFamily = PixelFont,
                    fontSize = 11.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Link your gaming accounts",
                    fontFamily = PixelFont,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(12.dp))
            }

            // One card per platform (Steam, Epic, Xbox, PSN), built
            // straight from the list in state.linkedAccounts
            items(
                state.linkedAccounts
            ) { account ->

                PixelLinkedAccountRow(
                    account = account,

                    onLinkClick = { platform ->

                        val url =
                            viewModel.loginUrlFor(
                                platform
                            )

                        if (url != null) {

                            // Custom Tabs opens the platform's real login
                            // page inside a tab that shares the device
                            // browser's cookies/session, instead of a
                            // WebView, which is Google's recommended way
                            // to do OAuth/OpenID logins from a native app
                            // (Android Developers, 2026h)
                            CustomTabsIntent
                                .Builder()
                                .build()
                                .launchUrl(
                                    context,
                                    Uri.parse(url)
                                )

                        } else if (
                            platform == Platform.PSN
                        ) {

                            // PSN has no public login URL to open, so
                            // show the honestly-labelled simulated
                            // dialog instead
                            showPsnSimulationDialog = true
                        }
                    },

                    onUnlinkClick = { platform ->

                        pendingUnlink = platform
                    }
                )

                Spacer(Modifier.height(10.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ---------------------------------------------------------------
    // UNLINK CONFIRMATION
    // ---------------------------------------------------------------

    // Only renders when pendingUnlink is non-null, i.e. right after the
    // user taps "Unlink" on a linked account
    pendingUnlink?.let { platform ->

        PixelUnlinkConfirmDialog(

            platform = platform,

            onConfirm = {

                viewModel.onUnlinkAccount(
                    platform
                )

                pendingUnlink = null
            },

            onDismiss = {

                pendingUnlink = null
            }
        )
    }

    // ---------------------------------------------------------------
    // PSN SIMULATION
    // ---------------------------------------------------------------

    if (showPsnSimulationDialog) {

        PixelPsnSimulatedConsentDialog(

            onConfirm = {

                viewModel.simulatePsnLink()

                showPsnSimulationDialog = false
            },

            onDismiss = {

                showPsnSimulationDialog = false
            }
        )
    }
}


// ===================================================================
// SETTINGS SECTION
// ===================================================================

// White pixel card with a black border, holding one titled group of
// settings rows — visual equivalent of the old Material Card-based
// SettingsSection, restyled to match BottomCard in MainActivity.kt
@Composable
fun PixelSettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(2.dp, Color.Black)
            .padding(16.dp)
    ) {

        Text(
            text = title,
            fontFamily = PixelFont,
            fontSize = 11.sp,
            color = AccentPurple
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        content()
    }
}


// ===================================================================
// SWITCH ROW
// ===================================================================

// A pixel-font label next to a Material 3 Switch, recoloured to
// AccentPurple so it matches the app's accent instead of the default
// Material color scheme, used for the Notifications toggle
@Composable
fun PixelSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = label,
            fontFamily = PixelFont,
            fontSize = 10.sp,
            color = Color.Black
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentPurple,
                checkedBorderColor = Color.Black,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray,
                uncheckedBorderColor = Color.Black
            )
        )
    }
}


// ===================================================================
// DROPDOWN ROW
// ===================================================================

/**
 * A pixel-font label next to a dropdown, used for both Language and
 * Theme.
 *
 * @param options list of (stored value, localized display label) pairs,
 * so the dropdown shows translated names while onSelect still receives
 * the stable underlying code (Android Developers, 2026b).
 */
@Composable
fun PixelDropdownRow(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    // Looks up the display label matching whatever value is stored
    val selectedDisplay =
        options
            .firstOrNull {
                it.first == selectedValue
            }
            ?.second
            ?: selectedValue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = label,
            fontFamily = PixelFont,
            fontSize = 10.sp,
            color = Color.Black
        )

        Column {

            // Small bordered pill showing the current value, tapped to
            // open the dropdown — styled like a miniature PixelPillButton
            Row(
                modifier = Modifier
                    .border(2.dp, Color.Black, RoundedCornerShape(50))
                    .background(AccentPurple, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .then(
                        Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = {
                        expanded = true
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {

                    Text(
                        text = selectedDisplay,
                        fontFamily = PixelFont,
                        fontSize = 9.sp,
                        color = Color.White
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,

                onDismissRequest = {
                    expanded = false
                }
            ) {

                options.forEach { (value, display) ->

                    DropdownMenuItem(

                        text = {
                            Text(
                                text = display,
                                fontFamily = PixelFont,
                                fontSize = 9.sp
                            )
                        },

                        onClick = {

                            onSelect(
                                value
                            )

                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


// ===================================================================
// LINKED ACCOUNT ROW
// ===================================================================

// White pixel card per platform, showing its name and, if linked, its
// username — plus a pixel pill Link/Unlink button depending on state
@Composable
fun PixelLinkedAccountRow(
    account: LinkedAccount,
    onLinkClick: (Platform) -> Unit,
    onUnlinkClick: (Platform) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(2.dp, Color.Black)
            .padding(16.dp),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                text = account.platform.displayName,
                fontFamily = PixelFont,
                fontSize = 10.sp,
                color = Color.Black
            )

            if (
                account.isLinked &&
                account.username != null
            ) {

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(
                        R.string.linked_as,
                        account.username
                    ),

                    fontFamily = PixelFont,
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }

        if (account.isLinked) {

            Row(
                modifier = Modifier
                    .border(2.dp, Color.Black, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp)
            ) {
                TextButton(
                    onClick = {
                        onUnlinkClick(
                            account.platform
                        )
                    }
                ) {

                    Text(
                        text = stringResource(
                            R.string.action_unlink
                        ),
                        fontFamily = PixelFont,
                        fontSize = 8.sp,
                        color = Color.Red
                    )
                }
            }

        } else {

            Button(
                onClick = {

                    onLinkClick(
                        account.platform
                    )
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
            ) {

                Text(
                    text = stringResource(
                        R.string.action_link
                    ),
                    fontFamily = PixelFont,
                    fontSize = 9.sp,
                    color = Color.White
                )
            }
        }
    }
}


// ===================================================================
// PSN SIMULATED CONSENT
// ===================================================================

// AlertDialog restyled with the pixel font and app colors — its title,
// body, and buttons are all set to PixelFont, and the confirm dialog
// stays clearly labelled as a simulation in string resources since PSN
// has no real login to show (Android Developers, 2026b)
@Composable
fun PixelPsnSimulatedConsentDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color.White,

        title = {
            Text(
                text = stringResource(
                    R.string.psn_dialog_title
                ),
                fontFamily = PixelFont,
                fontSize = 11.sp,
                color = Color.Black
            )
        },

        text = {
            Text(
                text = stringResource(
                    R.string.psn_dialog_body
                ),
                fontFamily = PixelFont,
                fontSize = 9.sp,
                color = Color.Black
            )
        },

        confirmButton = {

            TextButton(
                onClick = onConfirm
            ) {

                Text(
                    text = stringResource(
                        R.string.psn_dialog_confirm
                    ),
                    fontFamily = PixelFont,
                    fontSize = 9.sp,
                    color = AccentPurple
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = stringResource(
                        R.string.action_cancel
                    ),
                    fontFamily = PixelFont,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    )
}


// ===================================================================
// UNLINK CONFIRMATION
// ===================================================================

// Same pixel-styled AlertDialog treatment as the PSN dialog above, asks
// the user to confirm before actually unlinking, to avoid an accidental
// tap losing their linked account
@Composable
fun PixelUnlinkConfirmDialog(
    platform: Platform,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color.White,

        title = {

            Text(
                text = stringResource(
                    R.string.unlink_dialog_title,
                    platform.displayName
                ),
                fontFamily = PixelFont,
                fontSize = 11.sp,
                color = Color.Black
            )
        },

        text = {

            Text(
                text = stringResource(
                    R.string.unlink_dialog_body
                ),
                fontFamily = PixelFont,
                fontSize = 9.sp,
                color = Color.Black
            )
        },

        confirmButton = {

            TextButton(
                onClick = onConfirm
            ) {

                Text(
                    text = stringResource(
                        R.string.action_unlink
                    ),
                    fontFamily = PixelFont,
                    fontSize = 9.sp,
                    color = Color.Red
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = stringResource(
                        R.string.action_cancel
                    ),
                    fontFamily = PixelFont,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    )
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