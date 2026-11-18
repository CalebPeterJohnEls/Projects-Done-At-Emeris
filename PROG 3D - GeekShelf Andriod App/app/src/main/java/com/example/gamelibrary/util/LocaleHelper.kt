package com.example.gamelibrary.util

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

/**
 * Applies the Settings screen's language choice reliably.
 *
 * WHY THIS EXISTS: `AppCompatDelegate.setApplicationLocales()` (used
 * elsewhere in this project, in [com.example.gamelibrary.ui.SettingsViewModel])
 * only reliably triggers an immediate resource reload on API 33+ (where
 * the system `LocaleManager` handles it) or when the Activity extends
 * `AppCompatActivity`. This project's `MainActivity` extends plain
 * `ComponentActivity`, so on API < 33 `setApplicationLocales()` can persist
 * the choice without the currently-running process actually picking up the
 * new locale's resources — a documented AppCompat/ComponentActivity gap,
 * not a bug in the rest of this app's logic (Android Developers, 2024j).
 *
 * The fix that works regardless of Activity type is the classic Android
 * pattern of wrapping the base [Context] with a [Configuration] carrying
 * the chosen [Locale] in `Activity.attachBaseContext()`
 * (Android Developers, 2024k). [MainActivity] does exactly that, reading
 * the persisted language via [getPersistedLanguage].
 *
 * A separate, synchronous SharedPreferences store (not the DataStore used
 * by [com.example.gamelibrary.data.SettingsRepository]) is used here
 * specifically because `attachBaseContext()` runs before `onCreate()`,
 * too early to safely await a suspending DataStore read.
 *
 * References:
 * Android Developers (2024j) Add support for per-app language preferences.
 * Available at: https://developer.android.com/guide/topics/resources/app-languages
 * (Accessed: 20 September 2026).
 * Android Developers (2024k) Localize your app. Available at:
 * https://developer.android.com/training/basics/supporting-devices/languages
 * (Accessed: 20 September 2026).
 */
object LocaleHelper {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"

    /** Synchronous write, called right before `Activity.recreate()` so the
     * new Activity instance's `attachBaseContext()` sees the update — a
     * DataStore write here would be asynchronous and risk a race. */
    fun persistLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_LANGUAGE, languageCode) }
    }

    fun getPersistedLanguage(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"

    /** Wraps [context] with a Configuration carrying [languageCode]'s
     * Locale, so every `stringResource()`/`getString()` call made through
     * the returned Context resolves against that language's resources. */
    fun wrap(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
