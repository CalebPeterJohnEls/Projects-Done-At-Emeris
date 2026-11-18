package com.example.gamelibrary.auth

import android.net.Uri

/**
 * Builds the Steam OpenID login URL and extracts the SteamID64 from the
 * returned OpenID assertion.
 *
 * Steam acts as an OpenID 2.0 identity provider rather than OAuth2 — the
 * user logs in directly on steamcommunity.com and Steam redirects back
 * with signed parameters, so no client secret is stored in the app
 * (Valve Corporation, 2026a).
 */
object SteamAuth {

    // The Steam page that performs the actual login (Valve Corporation, 2026a)
    private const val STEAM_OPENID_ENDPOINT =
        "https://steamcommunity.com/openid/login"

    // Steam's OpenID implementation only accepts an http(s) return_to URL,
    // not a custom app scheme — this Firebase Hosting page exists purely
    // to receive Steam's redirect and immediately forward it on to the
    // app's own gamelibrary:// deep link (Google, 2026).
    private const val RETURN_TO =
        "https://geekshelfonline.web.app/"

    // "realm" must also be an http(s) origin, for the same reason as
    // return_to above (Valve Corporation, 2026a)
    private const val REALM =
        "https://geekshelfonline.web.app/"

    /**
     * Builds the full "checkid_setup" login URL. The parameter names below
     * (openid.ns, openid.mode, etc.) come directly from Steam's own OpenID
     * documentation (Valve Corporation, 2026a).
     */
    fun buildLoginUrl(): String {

        val params = linkedMapOf(

            "openid.ns" to
                    "http://specs.openid.net/auth/2.0",

            "openid.mode" to
                    "checkid_setup",

            "openid.return_to" to
                    RETURN_TO,

            "openid.realm" to
                    REALM,

            "openid.identity" to
                    "http://specs.openid.net/auth/2.0/identifier_select",

            "openid.claimed_id" to
                    "http://specs.openid.net/auth/2.0/identifier_select"
        )

        // Turns the map above into a single "?key=value&key=value..." query string
        val query = params.entries.joinToString("&") { (key, value) ->
            "$key=${Uri.encode(value)}"
        }

        return "$STEAM_OPENID_ENDPOINT?$query"
    }

    /**
     * After Steam redirects back, every query parameter starting with
     * "openid." needs to be read out of the callback URL so the app can
     * work out who logged in (Valve Corporation, 2026a).
     */
    fun extractOpenIdParams(
        callbackUri: Uri
    ): Map<String, String> {

        return callbackUri
            .queryParameterNames
            .filter {
                it.startsWith("openid.")
            }
            .associateWith {
                callbackUri.getQueryParameter(it) ?: ""
            }
    }

    /**
     * Steam returns the user's identity as a full URL, e.g.
     * https://steamcommunity.com/openid/id/76561198012345678
     * The number at the end is the permanent SteamID64 the rest of the
     * app uses to identify this user (Valve Corporation, 2026a).
     */
    fun steamIdFromClaimedId(
        claimedId: String
    ): String? {

        val steamId =
            claimedId.substringAfterLast("/")

        return steamId.takeIf {
            it.isNotBlank() &&
                    it.all(Char::isDigit)
        }
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