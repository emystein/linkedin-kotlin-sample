package com.example.api

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing an error response
 * Used for consistent error handling across all endpoints
 */
data class ErrorResponse(
    @JsonProperty("error")
    val error: String,

    @JsonProperty("message")
    val message: String? = null,

    @JsonProperty("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)