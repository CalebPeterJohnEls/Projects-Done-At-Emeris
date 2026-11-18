package com.example.gamelibrary.data

import android.util.Log
import com.example.gamelibrary.api.AuthApiService
import com.example.gamelibrary.api.MockAuthApiService
import com.example.gamelibrary.api.OAuthCodeRequest
import com.example.gamelibrary.api.SteamVerifyRequest
import com.example.gamelibrary.auth.SteamAuth
import com.example.gamelibrary.model.LinkedAccount
import com.example.gamelibrary.model.Platform
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "AccountRepository"

/**
 * Holds and mutates the linked/unlinked state for each supported platform.
 *
 * Steam is linked through OpenID (Valve Corporation, 2026a) rather than a
 * managed service. A production app could instead use a third-party
 * identity platform (e.g. PlayFab's LinkSteamAccount) to handle account
 * linking server-side, but that requires its own backend and account
 * system — the approach here keeps everything client-side for this
 * project's scope (Microsoft, 2026).
 *
 * The SteamID64 itself is persisted by SettingsViewModel in Firebase.
 * When the application starts again, SettingsViewModel reads the saved
 * SteamID64 and calls restoreSteamLink() so the Steam account appears
 * linked again without requiring another Steam login (The IIE, 2026).
 *
 * NOTE: This repository is a singleton for the entire app process (it is
 * created once in MainActivity), not scoped per signed-in user. Whenever
 * the signed-in Firebase user changes (login, logout, or switching
 * accounts), callers MUST call clearAll() before restoring whatever is
 * actually linked for the new user.
 */
class AccountRepository(
    private val authApi: AuthApiService = MockAuthApiService()
) {

    // Holds one LinkedAccount entry per platform, starting all unlinked
    private val _linkedAccounts = MutableStateFlow(
        Platform.entries.map { LinkedAccount(platform = it) }
    )

    val linkedAccountsFlow: StateFlow<List<LinkedAccount>> =
        _linkedAccounts.asStateFlow()

    // ---------------------------------------------------------------
    // Steam
    // ---------------------------------------------------------------

    /**
     * Completes the initial Steam OpenID account link. The SteamID64 is
     * returned to SettingsViewModel, which persists it in Firebase for
     * future app launches (Valve Corporation, 2026a).
     */
    suspend fun completeSteamLink(
        openIdParams: Map<String, String>
    ): String {

        Log.d(
            TAG,
            "Steam callback received with ${openIdParams.size} openid.* params"
        )

        // Pull the user's claimed identity out of Steam's response
        val claimedId = openIdParams["openid.claimed_id"]

        // Extract just the numeric SteamID64 from the end of that URL
        val steamId = claimedId?.let(
            SteamAuth::steamIdFromClaimedId
        ) ?: throw IllegalStateException(
            "Could not read Steam ID from callback"
        )

        // authApi is a mock for now — verifySteam just echoes back a
        // display name instead of calling a real backend
        val result = authApi.verifySteam(
            SteamVerifyRequest(
                steamId64 = steamId
            )
        )

        markLinked(
            platform = Platform.STEAM,
            username = result.username
        )

        return steamId
    }

    /**
     * Restores the Steam account after the app has been restarted, using
     * the SteamID64 already saved in Firebase — the user does not need to
     * log in to Steam again (The IIE, 2026).
     */
    suspend fun restoreSteamLink(
        steamId64: String
    ) {

        Log.d(
            TAG,
            "Restoring Steam account with SteamID64: $steamId64"
        )

        val result = authApi.verifySteam(
            SteamVerifyRequest(
                steamId64 = steamId64
            )
        )

        markLinked(
            platform = Platform.STEAM,
            username = result.username
        )

        Log.d(
            TAG,
            "Steam account restored successfully as '${result.username}'"
        )
    }

    // ---------------------------------------------------------------
    // Epic
    // ---------------------------------------------------------------

    // Same OAuth2 "exchange a code for a verified account" pattern as
    // Xbox below, just against a different mock endpoint
    suspend fun completeEpicLink(
        callbackUriQuery: Map<String, String>
    ) {

        val code = callbackUriQuery["code"]
            ?: throw IllegalStateException(
                "Epic callback missing authorization code"
            )

        Log.d(
            TAG,
            "Epic callback received with auth code"
        )

        val result = authApi.verifyEpic(
            OAuthCodeRequest(
                authorizationCode = code
            )
        )

        markLinked(
            platform = Platform.EPIC,
            username = result.username
        )
    }

    // ---------------------------------------------------------------
    // Xbox
    // ---------------------------------------------------------------

    suspend fun completeXboxLink(
        callbackUriQuery: Map<String, String>
    ) {

        val code = callbackUriQuery["code"]
            ?: throw IllegalStateException(
                "Xbox callback missing authorization code"
            )

        Log.d(
            TAG,
            "Xbox callback received with auth code"
        )

        val result = authApi.verifyXbox(
            OAuthCodeRequest(
                authorizationCode = code
            )
        )

        markLinked(
            platform = Platform.XBOX,
            username = result.username
        )
    }

    // ---------------------------------------------------------------
    // PSN
    // ---------------------------------------------------------------

    // PSN has no public self-serve login, so this is a plain simulated
    // delay + fake account instead of a real network call
    suspend fun simulatePsnLink() {

        Log.d(
            TAG,
            "Simulating PSN link"
        )

        delay(600)

        markLinked(
            platform = Platform.PSN,
            username = "PSN_Player"
        )
    }

    // ---------------------------------------------------------------
    // Unlink
    // ---------------------------------------------------------------

    // Resets one platform's entry back to "not linked"
    fun unlink(
        platform: Platform
    ) {

        _linkedAccounts.update { list ->

            list.map {

                if (it.platform == platform) {

                    it.copy(
                        isLinked = false,
                        username = null,
                        linkedAt = null
                    )

                } else {

                    it
                }
            }
        }

        Log.d(
            TAG,
            "${platform.displayName} unlinked"
        )
    }

    // ---------------------------------------------------------------
    // Clear all (called whenever the signed-in user changes)
    // ---------------------------------------------------------------

    /**
     * Resets every platform back to unlinked. Needed because this
     * repository is one instance shared by the whole app, not one per
     * user, so a new sign-in must start from a clean slate.
     */
    fun clearAll() {

        _linkedAccounts.value =
            Platform.entries.map { LinkedAccount(platform = it) }

        Log.d(
            TAG,
            "Cleared all linked accounts (user changed)"
        )
    }

    // ---------------------------------------------------------------
    // Internal helper
    // ---------------------------------------------------------------

    // Shared by every platform's link/restore function above — marks one
    // entry as linked with a username and timestamp
    private fun markLinked(
        platform: Platform,
        username: String
    ) {

        _linkedAccounts.update { list ->

            list.map { account ->

                if (account.platform == platform) {

                    account.copy(
                        isLinked = true,
                        username = username,
                        linkedAt = System.currentTimeMillis()
                    )

                } else {

                    account
                }
            }
        }

        Log.d(
            TAG,
            "${platform.displayName} linked successfully as '$username'"
        )
    }
}