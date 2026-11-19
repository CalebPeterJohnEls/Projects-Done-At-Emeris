package com.example.funinactionproto.api

import android.content.Context

// Stores the login session on the device using SharedPreferences (Android Developers, 2026e; The IIE, 2026b).
class SessionManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("fia_session", Context.MODE_PRIVATE)

    fun saveSession(token: String, role: String, fullName: String, email: String, clientId: String?) {
        prefs.edit().putString("token", token).putString("role", role)
            .putString("fullName", fullName).putString("email", email)
            .putString("clientId", clientId).apply()
    }

    fun getToken() = prefs.getString("token", null)
    fun getRole() = prefs.getString("role", null)
    fun getFullName() = prefs.getString("fullName", null)
    fun getEmail() = prefs.getString("email", null)
    fun clear() = prefs.edit().clear().apply()
}

// Android Developers, 2026e. Save simple data with SharedPreferences. [online] Available at: <https://developer.android.com/training/data-storage/shared-preferences.html> [Accessed 22 September 2026]
// The IIE, 2026b. Programming 3C [PROG7313 Module Manual]. The Independent Institute of Education: Unpublished