package com.example.gamelibrary.auth

import android.net.Uri

/**
 * Builds the Epic Games login URL and parses the authorization code Epic
 * sends back. Epic uses standard OAuth2 Authorization Code grant
 * (IETF, 2012) via its Developer Portal client registration process
 * (Epic Games, n.d.).
 *
 * SETUP REQUIRED: register a free Epic Games Developer account, create an
 * Organization → Product → Application → Client at
 * https://dev.epicgames.com/portal, add `gamelibrary://auth/epic` as an
 * allowed redirect URI for the client, then replace [CLIENT_ID] below with
 * the generated Client ID. The Client Secret is never used on the Android
 * side — it only belongs in the backend token-exchange call.
 *
 * References:
 * IETF (2012) RFC 6749: The OAuth 2.0 Authorization Framework. Available
 * at: https://datatracker.ietf.org/doc/html/rfc6749 (Accessed: 20 September 2026).
 * Epic Games (n.d.) Epic Games Developer Portal. Available at:
 * https://dev.epicgames.com/portal (Accessed: 20 September 2026).
 */
object EpicAuth {

    private const val AUTHORIZE_ENDPOINT = "https://www.epicgames.com/id/authorize"
    private const val REDIRECT_URI = "gamelibrary://auth/epic"

    // TODO: replace with the Client ID from Product Settings > Client in
    // the Epic Games Developer Portal (Epic Games, n.d.).
    private const val CLIENT_ID = "YOUR_EPIC_CLIENT_ID"

    fun buildLoginUrl(): String {
        val params = linkedMapOf(
            "client_id" to CLIENT_ID,
            "redirect_uri" to REDIRECT_URI,
            "response_type" to "code",
            "scope" to "basic_profile"
        )
        val query = params.entries.joinToString("&") { (key, value) -> "$key=${Uri.encode(value)}" }
        return "$AUTHORIZE_ENDPOINT?$query"
    }

    /** Epic's redirect carries a plain `?code=...` param (IETF, 2012, sec. 4.1.2). */
    fun extractAuthCode(callbackUri: Uri): String? = callbackUri.getQueryParameter("code")
}
