package com.nexa.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.nexa.app.data.AuthApiClient
import com.nexa.app.data.AuthPreferences
import com.nexa.app.model.AuthResponse
import com.nexa.app.model.AuthSettings
import com.nexa.app.model.SessionState
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var preferences: AuthPreferences
    private val apiClient = AuthApiClient()
    private var loginMode = true
    private val activityLog = mutableListOf<String>()

    private lateinit var authToggle: MaterialButtonToggleGroup
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var baseUrlInput: TextInputEditText
    private lateinit var loginEndpointInput: TextInputEditText
    private lateinit var signupEndpointInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var saveSettingsButton: MaterialButton
    private lateinit var clearSessionButton: MaterialButton
    private lateinit var sessionText: TextView
    private lateinit var responseText: TextView
    private lateinit var activityText: TextView
    private lateinit var signupOnlyViews: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = AuthPreferences(this)
        bindViews()
        applyStoredSettings()
        renderSession(preferences.loadSession())
        renderActivity(preferences.loadLog())
        setListeners()
        updateModeUi()
    }

    private fun bindViews() {
        authToggle = findViewById(R.id.authToggle)
        fullNameInput = findViewById(R.id.fullNameInput)
        usernameInput = findViewById(R.id.usernameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        baseUrlInput = findViewById(R.id.baseUrlInput)
        loginEndpointInput = findViewById(R.id.loginEndpointInput)
        signupEndpointInput = findViewById(R.id.signupEndpointInput)
        submitButton = findViewById(R.id.submitButton)
        saveSettingsButton = findViewById(R.id.saveSettingsButton)
        clearSessionButton = findViewById(R.id.clearSessionButton)
        sessionText = findViewById(R.id.sessionText)
        responseText = findViewById(R.id.responseText)
        activityText = findViewById(R.id.activityText)

        signupOnlyViews = listOf(
            findViewById(R.id.fullNameLayout),
            findViewById(R.id.usernameLayout),
            findViewById(R.id.confirmPasswordLayout)
        )
    }

    private fun setListeners() {
        authToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            loginMode = checkedId == R.id.loginButton
            updateModeUi()
        }

        saveSettingsButton.setOnClickListener {
            val settings = currentSettings()
            preferences.saveSettings(settings)
            addLog("Saved auth settings for ${settings.baseUrl}.")
            toast(getString(R.string.settings_saved))
        }

        clearSessionButton.setOnClickListener {
            preferences.clearSession()
            renderSession(SessionState())
            responseText.text = getString(R.string.no_response_yet)
            addLog("Cleared stored session data.")
        }

        submitButton.setOnClickListener {
            submitAuth()
        }
    }

    private fun applyStoredSettings() {
        val settings = preferences.loadSettings()
        baseUrlInput.setText(settings.baseUrl)
        loginEndpointInput.setText(settings.loginEndpoint)
        signupEndpointInput.setText(settings.signupEndpoint)
    }

    private fun updateModeUi() {
        signupOnlyViews.forEach { view -> view.visibility = if (loginMode) View.GONE else View.VISIBLE }
        submitButton.text = getString(if (loginMode) R.string.login_cta else R.string.signup_cta)
        if (loginMode) {
            authToggle.check(R.id.loginButton)
        } else {
            authToggle.check(R.id.signupButton)
        }
    }

    private fun currentSettings(): AuthSettings {
        return AuthSettings(
            baseUrl = baseUrlInput.text?.toString()?.trim().orEmpty().ifBlank { "http://10.0.2.2:3000" },
            loginEndpoint = loginEndpointInput.text?.toString()?.trim().orEmpty().ifBlank { "/api/auth/login" },
            signupEndpoint = signupEndpointInput.text?.toString()?.trim().orEmpty().ifBlank { "/api/auth/signup" }
        )
    }

    private fun submitAuth() {
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString().orEmpty()
        val confirmPassword = confirmPasswordInput.text?.toString().orEmpty()

        if (email.isBlank() || password.isBlank()) {
            toast(getString(R.string.validation_required))
            return
        }

        if (!loginMode && password != confirmPassword) {
            toast(getString(R.string.validation_password_mismatch))
            addLog("Signup blocked because passwords do not match.")
            return
        }

        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)

            if (!loginMode) {
                val fullName = fullNameInput.text?.toString()?.trim().orEmpty()
                val username = usernameInput.text?.toString()?.trim().orEmpty().ifBlank { email.substringBefore('@') }
                put("name", fullName)
                put("username", username)
                put("confirmPassword", confirmPassword)
            }
        }

        val settings = currentSettings()
        preferences.saveSettings(settings)
        submitButton.isEnabled = false
        submitButton.text = getString(R.string.loading)

        thread {
            try {
                val response = if (loginMode) apiClient.login(settings, payload) else apiClient.signup(settings, payload)
                val session = extractSession(response)
                preferences.saveSession(session)
                runOnUiThread {
                    renderSession(session)
                    renderResponse(response)
                    addLog("${if (loginMode) "Login" else "Signup"} succeeded via ${response.endpoint}.")
                    clearAuthForm()
                    toast(getString(if (loginMode) R.string.login_success else R.string.signup_success))
                }
            } catch (exception: Exception) {
                runOnUiThread {
                    responseText.text = exception.message ?: getString(R.string.unknown_error)
                    addLog("${if (loginMode) "Login" else "Signup"} failed: ${exception.message}")
                    toast(exception.message ?: getString(R.string.unknown_error))
                }
            } finally {
                runOnUiThread {
                    submitButton.isEnabled = true
                    submitButton.text = getString(if (loginMode) R.string.login_cta else R.string.signup_cta)
                }
            }
        }
    }

    private fun extractSession(response: AuthResponse): SessionState {
        if (response.body.isBlank()) return SessionState()
        val json = runCatching { JSONObject(response.body) }.getOrNull() ?: return SessionState(token = response.body)

        val tokenKeys = listOf("token", "accessToken", "jwt", "access_token")
        val refreshKeys = listOf("refreshToken", "refresh_token")
        val userKeys = listOf("user", "profile", "data")

        val token = tokenKeys.firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
        val refreshToken = refreshKeys.firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
        val userJson = userKeys.firstNotNullOfOrNull { key -> json.optJSONObject(key)?.toString(2) }

        return SessionState(token = token, refreshToken = refreshToken, userJson = userJson)
    }

    private fun renderSession(session: SessionState) {
        val tokenText = session.token ?: getString(R.string.no_session_yet)
        val refreshText = session.refreshToken?.let { "\n\nRefresh token\n$it" }.orEmpty()
        val userText = session.userJson?.let { "\n\nUser\n$it" }.orEmpty()
        sessionText.text = tokenText + refreshText + userText
    }

    private fun renderResponse(response: AuthResponse) {
        responseText.text = getString(R.string.response_template, response.endpoint, response.statusCode.toString(), response.body)
    }

    private fun renderActivity(lines: List<String>) {
        activityLog.clear()
        activityLog.addAll(lines.take(8))
        activityText.text = if (activityLog.isEmpty()) getString(R.string.no_activity_yet) else activityLog.joinToString("\n\n")
    }

    private fun addLog(message: String) {
        activityLog.add(0, "${System.currentTimeMillis()}: $message")
        while (activityLog.size > 8) {
            activityLog.removeLast()
        }
        preferences.saveLog(activityLog)
        renderActivity(activityLog)
    }

    private fun clearAuthForm() {
        fullNameInput.text = null
        usernameInput.text = null
        emailInput.text = null
        passwordInput.text = null
        confirmPasswordInput.text = null
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
