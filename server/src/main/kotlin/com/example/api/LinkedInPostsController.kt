package com.example.api

import com.example.server.common.AccessToken
import com.example.api.ErrorResponse
import com.linkedin.service.LinkedInPostsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for LinkedIn Posts API operations
 * Base URL: /posts
 */
@RestController
@RequestMapping("/posts")
class LinkedInPostsController {

    @Autowired
    private lateinit var linkedInPostsService: LinkedInPostsService

    /**
     * Create a text-only post on LinkedIn using the Posts API
     * Automatically retrieves the current user's URN and uses it as the author
     *
     * @param content The text content of the post
     * @return Response from the LinkedIn Posts API
     */
    @RequestMapping(value = ["/create"])
    fun createPost(@RequestParam(required = false) content: String?): Any {
        val token = LinkedInOAuthController.token
        if (token == null) {
            return ErrorResponse("no_token", "No access token available. Please generate a token first.")
        }
        return try {
            linkedInPostsService.createPost(AccessToken(token), content)
        } catch (e: IllegalArgumentException) {
            ErrorResponse("invalid_content", e.message ?: "Invalid content provided")
        } catch (e: Exception) {
            ErrorResponse("service_error", e.message ?: "An error occurred while creating the post")
        }
    }


}
