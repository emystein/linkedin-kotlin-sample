package com.linkedin.service

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class representing the response from LinkedIn's post creation API
 */
data class PostCreationResponse(
    @JsonProperty("success")
    val success: Boolean,
    
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("postId")
    val postId: String? = null,
    
    @JsonProperty("author")
    val author: String? = null
) {
    companion object {
        fun success(postId: String, author: String): PostCreationResponse {
            return PostCreationResponse(true, "Post created successfully", postId, author)
        }

        fun failure(message: String): PostCreationResponse {
            return PostCreationResponse(false, message)
        }
    }
}
