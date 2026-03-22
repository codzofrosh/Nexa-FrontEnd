package com.nexa.app.data

import com.nexa.app.model.AuthResponse
import com.nexa.app.model.AuthSettings
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthApiClient {
    private val loginFallbacks = listOf(
        "/api/auth/login",
        "/api/v1/auth/login",
        "/auth/login",
        "/api/login"
    )

    private val signupFallbacks = listOf(
        "/api/auth/signup",
        "/api/auth/register",
        "/api/v1/auth/signup",
        "/api/v1/auth/register",
        "/auth/signup",
        "/auth/register"
    )

    fun login(settings: AuthSettings, payload: JSONObject): AuthResponse {
        return submit(settings, settings.loginEndpoint, loginFallbacks, payload)
    }

    fun signup(settings: AuthSettings, payload: JSONObject): AuthResponse {
        return submit(settings, settings.signupEndpoint, signupFallbacks, payload)
    }

    private fun submit(
        settings: AuthSettings,
        primaryEndpoint: String,
        fallbacks: List<String>,
        payload: JSONObject
    ): AuthResponse {
        val attempts = buildList {
            add(primaryEndpoint)
            addAll(fallbacks.filterNot { it == primaryEndpoint })
        }
        val failures = mutableListOf<String>()

        for (endpoint in attempts) {
            try {
                val response = postJson(settings.baseUrl, endpoint, payload)
                if (response.statusCode in 200..299) {
                    return response
                }

                failures += "$endpoint -> ${response.statusCode} ${response.body}"
                if (response.statusCode !in listOf(404, 405)) {
                    throw IllegalStateException("Request failed at $endpoint with ${response.statusCode}")
                }
            } catch (exception: Exception) {
                failures += "$endpoint -> ${exception.message ?: "Unknown error"}"
            }
        }

        throw IllegalStateException("No auth endpoint succeeded. Tried: ${failures.joinToString(" | ")}")
    }

    private fun postJson(baseUrl: String, endpoint: String, payload: JSONObject): AuthResponse {
        val normalizedBase = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
        val cleanEndpoint = endpoint.removePrefix("/")
        val url = URL(normalizedBase + cleanEndpoint)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(payload.toString())
            writer.flush()
        }

        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        connection.disconnect()

        return AuthResponse(endpoint = endpoint, statusCode = statusCode, body = body)
    }
}
