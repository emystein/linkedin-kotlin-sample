package com.example.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing a LinkedIn post creation request
 * Based on LinkedIn Posts API v2 specification
 */
data class LinkedInPostRequest(
    @JsonProperty("author")
    val author: String,
    
    @JsonProperty("commentary")
    val commentary: String,
    
    @JsonProperty("visibility")
    val visibility: String = "PUBLIC",
    
    @JsonProperty("distribution")
    val distribution: PostDistribution = PostDistribution(),
    
    @JsonProperty("lifecycleState")
    val lifecycleState: String = "PUBLISHED",
    
    @JsonProperty("isReshareDisabledByAuthor")
    val isReshareDisabledByAuthor: Boolean = false
)

/**
 * Data class representing the distribution settings for a LinkedIn post
 */
data class PostDistribution(
    @JsonProperty("feedDistribution")
    val feedDistribution: String = "MAIN_FEED",
    
    @JsonProperty("targetEntities")
    val targetEntities: List<String> = emptyList(),
    
    @JsonProperty("thirdPartyDistributionChannels")
    val thirdPartyDistributionChannels: List<String> = emptyList()
)
