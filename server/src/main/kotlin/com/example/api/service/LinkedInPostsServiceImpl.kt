package com.example.api.service

import com.example.api.dto.ErrorResponse
import com.example.api.dto.PostCreationResponse
import com.linkedin.api.client.LinkedInPostsClient
import com.linkedin.api.dto.LinkedInPostRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LinkedInPostsServiceImpl : LinkedInPostsService {

    @Autowired
    private lateinit var linkedInPostsClient: LinkedInPostsClient

    @Autowired
    private lateinit var linkedInProfileService: LinkedInProfileService

    private val logger = KotlinLogging.logger {}

    override fun createPost(token: AccessToken, content: String?): Any {
        if (content.isNullOrBlank()) {
            return ErrorResponse("empty_content", "Post content cannot be empty.")
        }

        try {
            // First, get the current user's URN using the profile service
            val personUrn = linkedInProfileService.getCurrentUserUrn(token)
            if (personUrn.startsWith("{\"error\"")) {
                return ErrorResponse("urn_retrieval_error", "Failed to retrieve user URN: $personUrn")
            }

            // Create the post request using the data class
            val postRequest = LinkedInPostRequest(
                author = personUrn,
                commentary = content
            )

            // Make the POST request to LinkedIn Posts API using Feign client
            val response = linkedInPostsClient.createPost(
                authorization = "Bearer ${token.value}",
                linkedInVersion = "202505",
                protocolVersion = "2.0.0",
                postRequest = postRequest
            )

            // Check if the post was created successfully
            if (response.status() in 200..299) {
                val postId = response.headers()["x-restli-id"]?.firstOrNull() ?: "Unknown"
                return PostCreationResponse(
                    success = true,
                    message = "Post created successfully",
                    postId = postId,
                    author = personUrn
                )
            } else {
                return ErrorResponse("post_creation_failed", "Failed to create post. Status: ${response.status()}")
            }

        } catch (e: Exception) {
            logger.error(e) { "Error creating post" }
            return ErrorResponse("post_creation_error", "Failed to create post: ${e.message}")
        }
    }
}
