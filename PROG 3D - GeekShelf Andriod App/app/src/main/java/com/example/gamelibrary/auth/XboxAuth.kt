package com.example.gamelibrary.auth

import android.net.Uri

/**
 * Builds the Xbox Live login URL and parses the authorization code
 * Microsoft sends back.
 *
 * Xbox Live sign-in is layered on top of the Microsoft identity platform's
 * standard OAuth2 authorization code flow (Microsoft, 2024), requesting the
 * `Xboxlive.signin` and `Xboxlive.offline_access` scopes. The code returned
 * here is only the FIRST hop — a real integration still needs the backend
 * to exchange it for a Microsoft access token, then chain two more
 * Xbox-specific token exchanges (user token via
 * user.auth.xboxlive.com/user/authenticate, then an XSTS token via
 * xsts.auth.xboxlive.com/xsts/authorize) before a gamertag is available.
 * Those XBL-specific hops are not officially documented end-to-end by
 * Microsoft for third-party consumer apps, so [verifyXboxCallback] in
 * [com.example.gamelibrary.data.AccountRepository] is mocked rather than
 * attempting that undocumented chain from a student project.
 *
 * SETUP REQUIRED: register an app at https://portal.azure.com under Azure
 * Active Directory → App registrations, set "Supported account types" to
 * personal Microsoft accounts only, add `gamelibrary://auth/xbox` as a
 * "Mobile and desktop applications" redirect URI, then replace
 * [CLIENT_ID] with the generated Application (client) ID.
 *
 * Reference: Microsoft (2024) Microsoft identity platform and the OAuth
 * 2.0 authorization code flow. Available at:
 * https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow
 * (Accessed: 20 September 2026).
 */
object XboxAuth {

    private const val AUTHORIZE_ENDPOINT = "https://login.live.com/oauth20_authorize.srf"
    private const val REDIRECT_URI = "gamelibrary://auth/xbox"

    // TODO: replace with the Application (client) ID from your Azure app
    // registration (Microsoft, 2024).
    private const val CLIENT_ID = "YOUR_XBOX_CLIENT_ID"

    fun buildLoginUrl(): String {
        val params = linkedMapOf(
            "client_id" to CLIENT_ID,
            "response_type" to "code",
            "approval_prompt" to "auto",
            "scope" to "Xboxlive.signin Xboxlive.offline_access",
            "redirect_uri" to REDIRECT_URI
        )
        val query = params.entries.joinToString("&") { (key, value) -> "$key=${Uri.encode(value)}" }
        return "$AUTHORIZE_ENDPOINT?$query"
    }

    fun extractAuthCode(callbackUri: Uri): String? = callbackUri.getQueryParameter("code")
}
