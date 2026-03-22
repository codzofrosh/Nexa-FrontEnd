package com.nexa.app.data

import android.content.Context
import com.nexa.app.model.AuthSettings
import com.nexa.app.model.SessionState

class AuthPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("nexa_auth_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): AuthSettings = AuthSettings(
        baseUrl = preferences.getString("base_url", "http://10.0.2.2:3000") ?: "http://10.0.2.2:3000",
        loginEndpoint = preferences.getString("login_endpoint", "/api/auth/login") ?: "/api/auth/login",
        signupEndpoint = preferences.getString("signup_endpoint", "/api/auth/signup") ?: "/api/auth/signup"
    )

    fun saveSettings(settings: AuthSettings) {
        preferences.edit()
            .putString("base_url", settings.baseUrl)
            .putString("login_endpoint", settings.loginEndpoint)
            .putString("signup_endpoint", settings.signupEndpoint)
            .apply()
    }

    fun saveSession(session: SessionState) {
        preferences.edit()
            .putString("token", session.token)
            .putString("refresh_token", session.refreshToken)
            .putString("user_json", session.userJson)
            .apply()
    }

    fun loadSession(): SessionState = SessionState(
        token = preferences.getString("token", null),
        refreshToken = preferences.getString("refresh_token", null),
        userJson = preferences.getString("user_json", null)
    )

    fun clearSession() {
        preferences.edit()
            .remove("token")
            .remove("refresh_token")
            .remove("user_json")
            .apply()
    }

    fun saveLog(lines: List<String>) {
        preferences.edit().putString("activity_log", lines.joinToString("\n")) .apply()
    }

    fun loadLog(): MutableList<String> {
        val stored = preferences.getString("activity_log", "") ?: ""
        return stored.lines().filter { it.isNotBlank() }.toMutableList()
    }
}
