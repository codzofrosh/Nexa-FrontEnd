package com.nexa.app.model

data class AuthSettings(
    val baseUrl: String = "http://10.0.2.2:3000",
    val loginEndpoint: String = "/api/auth/login",
    val signupEndpoint: String = "/api/auth/signup"
)

data class AuthResponse(
    val endpoint: String,
    val statusCode: Int,
    val body: String
)

data class SessionState(
    val token: String? = null,
    val refreshToken: String? = null,
    val userJson: String? = null
)
