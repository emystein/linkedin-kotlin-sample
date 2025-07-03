package com.example.api.service

import com.example.api.dto.PostCreationResponse
import com.linkedin.api.client.LinkedInPostsClient
import com.linkedin.api.dto.LinkedInPostRequest
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
        val postRequest = LinkedInPostRequest(author = personUrn, commentary = content)
        val response = linkedInPostsClient.createPost(
            authorization = "Bearer ${token.value}",
            postRequest = postRequest
        )
        if (response.status() in 200..299) {
            val postId = response.headers()["x-restli-id"]?.firstOrNull() ?: "Unknown"
            return PostCreationResponse.success(postId = postId, author = personUrn)
        } else {
            throw Exception("Failed to create post. Status: ${response.status()}")
        }
    }
}
