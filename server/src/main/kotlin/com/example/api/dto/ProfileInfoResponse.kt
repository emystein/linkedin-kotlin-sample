package com.example.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing the response from LinkedIn's userinfo API
 * Based on LinkedIn OpenID Connect userinfo specification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfileInfoResponse(
    @JsonProperty("sub")
    val sub: String? = null,
    
    @JsonProperty("name")
    val name: String? = null,
    
    @JsonProperty("given_name")
    val givenName: String? = null,
    
    @JsonProperty("family_name")
    val familyName: String? = null,
    
    @JsonProperty("picture")
    val picture: String? = null,
    
    @JsonProperty("locale")
    val locale: String? = null,
    
    @JsonProperty("email")
    val email: String? = null,
    
    @JsonProperty("email_verified")
    val emailVerified: Boolean? = null
)
