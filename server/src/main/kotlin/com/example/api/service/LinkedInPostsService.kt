package com.example.api.service

/**
 * Service interface for LinkedIn Posts API operations
 * Provides methods for creating and managing LinkedIn posts
 */
interface LinkedInPostsService {

    /**
     * Create a text-only post on LinkedIn using the Posts API
     * Automatically retrieves the current user's URN and uses it as the author
     *
     * @param token The access token
     * @param content The text content of the post
     * @return PostCreationResponse with post details or ErrorResponse if failed
     */
    fun createPost(token: String?, content: String?): Any
}
