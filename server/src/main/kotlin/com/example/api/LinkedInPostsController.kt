package com.example.api

import com.linkedin.api.client.LinkedInPostsClient
import com.linkedin.api.dto.LinkedInPostRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

/**
 * Controller for LinkedIn Posts API operations
 * Base URL: /posts
 */
@RestController
@RequestMapping("/posts")
class LinkedInPostsController {

    @Autowired
    private lateinit var linkedInPostsClient: LinkedInPostsClient

    @Autowired
    private lateinit var linkedInProfileController: LinkedInProfileController

    private val logger = Logger.getLogger(LinkedInPostsController::class.java.name)

    /**
     * Create a text-only post on LinkedIn using the Posts API
     * Automatically retrieves the current user's URN and uses it as the author
     *
     * @param content The text content of the post
     * @return Response from the LinkedIn Posts API
     */
    @RequestMapping(value = ["/create"])
    fun createPost(@RequestParam(required = false) content: String?): String {
        val token = LinkedInOAuthController.token
        if (token == null) {
            return "{\"error\": \"No access token available. Please generate a token first.\"}"
        }

        if (content.isNullOrBlank()) {
            return "{\"error\": \"Post content cannot be empty.\"}"
        }

        try {
            // First, get the current user's URN using the profile controller
            val personUrn = linkedInProfileController.getCurrentUserUrn(token)
            if (personUrn.startsWith("{\"error\"")) {
                return personUrn // Return the error message
            }

            // Create the post request using the data class
            val postRequest = LinkedInPostRequest(
                author = personUrn,
                commentary = content
            )

            // Make the POST request to LinkedIn Posts API using Feign client
            val response = linkedInPostsClient.createPost(
                authorization = "Bearer $token",
                linkedInVersion = "202505",
                protocolVersion = "2.0.0",
                postRequest = postRequest
            )

            // Check if the post was created successfully
            if (response.status() in 200..299) {
                val postId = response.headers()["x-restli-id"]?.firstOrNull() ?: "Unknown"
                return "{\"success\": true, \"message\": \"Post created successfully as $personUrn\", \"postId\": \"$postId\"}"
            } else {
                return "{\"error\": \"Failed to create post. Status: ${response.status()}\"}"
            }

        } catch (e: Exception) {
            return "{\"error\": \"${e.message?.replace("\"", "\\\"")}\"}"
        }
    }


}
