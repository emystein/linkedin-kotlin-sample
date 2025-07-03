package com.example.api.service

import com.example.common.AccessToken
import com.example.api.dto.PostCreationResponse
import com.linkedin.api.client.LinkedInPostsClient
import com.linkedin.api.client.createPost
import org.springframework.stereotype.Service

@Service
class LinkedInPostsServiceImpl(
    private val linkedInPostsClient: LinkedInPostsClient,
    private val linkedInProfileService: LinkedInProfileService,
) : LinkedInPostsService {
    override fun createPost(token: AccessToken, content: String?): Any {
        if (content.isNullOrBlank()) {
            throw IllegalArgumentException("Post content cannot be empty.")
        }
        val personUrn = linkedInProfileService.getCurrentUserUrn(token)
        val response = linkedInPostsClient.createPost(token, personUrn, content)
        if (response.status() in 200..299) {
            val postId = response.headers()["x-restli-id"]?.firstOrNull() ?: "Unknown"
            return PostCreationResponse.success(postId = postId, author = personUrn)
        } else {
            throw Exception("Failed to create post. Status: ${response.status()}")
        }
    }
}
