package com.example.gamelibrary.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gamelibrary.model.SettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Top-level DataStore delegate.
 *
 * A single DataStore instance is created per Context.
 *
 * Reference:
 * Android Developers (2024b) Preferences DataStore.
 */
val Context.settingsDataStore by preferencesDataStore(
    name = "settings"
)

/**
 * Stores and exposes application settings.
 */
class SettingsRepository(
    private val context: Context
) {

    private object Keys {

        val LANGUAGE =
            stringPreferencesKey("language")

        val NOTIFICATIONS =
            booleanPreferencesKey("notifications_enabled")

        val THEME =
            stringPreferencesKey("theme")
    }

    val settingsFlow: Flow<SettingsUiState> =
        context.settingsDataStore.data.map { prefs ->

            SettingsUiState(
                language = prefs[Keys.LANGUAGE] ?: "en",

                notificationsEnabled =
                    prefs[Keys.NOTIFICATIONS] ?: true,

                theme =
                    prefs[Keys.THEME] ?: "system"
            )
        }

    suspend fun setLanguage(
        code: String
    ) {

        context.settingsDataStore.edit { preferences ->

            preferences[Keys.LANGUAGE] = code
        }
    }

    suspend fun setNotifications(
        enabled: Boolean
    ) {

        context.settingsDataStore.edit { preferences ->

            preferences[Keys.NOTIFICATIONS] = enabled
        }
    }

    suspend fun setTheme(
        theme: String
    ) {

        context.settingsDataStore.edit { preferences ->

            preferences[Keys.THEME] = theme
        }
    }
}