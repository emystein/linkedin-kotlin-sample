package com.linkedin.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing the response from LinkedIn's organization access API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationAccessResponse(
    @JsonProperty("elements")
    val elements: List<OrganizationElement>? = null,
    
    @JsonProperty("paging")
    val paging: PagingInfo? = null
)

/**
 * Data class representing an organization element in the access response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationElement(
    @JsonProperty("organization")
    val organization: OrganizationInfo? = null,
    
    @JsonProperty("role")
    val role: String? = null,
    
    @JsonProperty("state")
    val state: String? = null
)

/**
 * Data class representing organization information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganizationInfo(
    @JsonProperty("id")
    val id: String? = null,
    
    @JsonProperty("localizedName")
    val localizedName: String? = null,
    
    @JsonProperty("vanityName")
    val vanityName: String? = null
)
