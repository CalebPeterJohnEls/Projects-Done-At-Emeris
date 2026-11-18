package com.example.gamelibrary.model

enum class Platform(
    val displayName: String
) {
    STEAM("Steam"),
    PSN("PlayStation Network"),
    XBOX("Xbox Live"),
    EPIC("Epic Games")
}

data class LinkedAccount(
    val platform: Platform,
    val isLinked: Boolean = false,
    val username: String? = null,
    val linkedAt: Long? = null
)

data class SettingsUiState(

    val language: String = "en",

    val notificationsEnabled: Boolean = true,

    val theme: String = "system",

    val linkedAccounts: List<LinkedAccount> =
        Platform.entries.map {
            LinkedAccount(it)
        },

    val isLinkingInProgress: Boolean = false
)