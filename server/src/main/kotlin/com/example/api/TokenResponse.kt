package com.example.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonCreator

/**
 * Data class representing a simple token response
 * Used for getToken endpoint and other token-related responses
 */
data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    
    @JsonProperty("token_type")
    val tokenType: String = "Bearer"
)

/**
 * Data class representing the response from LinkedIn's refresh token API
 * Based on LinkedIn OAuth2 refresh token specification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RefreshTokenResponse @JsonCreator constructor(
    @JsonProperty("access_token")
    val accessToken: String? = null,

    @JsonProperty("expires_in")
    val expiresIn: String? = null,

    @JsonProperty("refresh_token")
    val refreshToken: String? = null,

    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: String? = null,

    @JsonProperty("token_type")
    val tokenType: String = "Bearer"
)
