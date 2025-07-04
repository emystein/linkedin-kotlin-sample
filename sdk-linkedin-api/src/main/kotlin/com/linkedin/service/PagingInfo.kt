package com.linkedin.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

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
