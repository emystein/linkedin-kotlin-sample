package com.example.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing the response from LinkedIn's token introspection API
 * Based on LinkedIn OAuth2 token introspection specification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenIntrospectionResponse(
    @JsonProperty("active")
    val active: Boolean,
    
    @JsonProperty("client_id")
    val clientId: String? = null,
    
    @JsonProperty("authorized_at")
    val authorizedAt: Long? = null,
    
    @JsonProperty("created_at")
    val createdAt: Long? = null,
    
    @JsonProperty("status")
    val status: String? = null,
    
    @JsonProperty("expires_at")
    val expiresAt: Long? = null,
    
    @JsonProperty("scope")
    val scope: String? = null,
    
    @JsonProperty("auth_type")
    val authType: String? = null
)
