package com.linkedin.service

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing the response for person URN requests
 */
data class PersonUrnResponse(
    @JsonProperty("personUrn")
    val personUrn: String
)
