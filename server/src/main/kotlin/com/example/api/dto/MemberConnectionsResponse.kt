package com.example.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing the response from LinkedIn's Member Data Portability API
 * for member connections data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MemberConnectionsResponse(
    @JsonProperty("data")
    val data: List<ConnectionData>? = null,
    
    @JsonProperty("paging")
    val paging: PagingInfo? = null,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any>? = null
)

/**
 * Data class representing individual connection data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ConnectionData(
    @JsonProperty("id")
    val id: String? = null,
    
    @JsonProperty("firstName")
    val firstName: String? = null,
    
    @JsonProperty("lastName")
    val lastName: String? = null,
    
    @JsonProperty("profileUrl")
    val profileUrl: String? = null,
    
    @JsonProperty("connectedAt")
    val connectedAt: String? = null,
    
    @JsonProperty("company")
    val company: String? = null,
    
    @JsonProperty("position")
    val position: String? = null
)

/**
 * Data class representing paging information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PagingInfo(
    @JsonProperty("count")
    val count: Int? = null,
    
    @JsonProperty("start")
    val start: Int? = null,
    
    @JsonProperty("total")
    val total: Int? = null,
    
    @JsonProperty("links")
    val links: List<PagingLink>? = null
)

/**
 * Data class representing paging links
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PagingLink(
    @JsonProperty("rel")
    val rel: String? = null,
    
    @JsonProperty("href")
    val href: String? = null
)
