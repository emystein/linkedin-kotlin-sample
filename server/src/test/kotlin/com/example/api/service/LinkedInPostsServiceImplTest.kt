package com.example.api.service

import com.example.api.LinkedInOAuthController
import com.example.api.dto.ErrorResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class LinkedInPostsServiceImplTest {

    private val service = LinkedInPostsServiceImpl()

    @Test
    fun `createPost should return ErrorResponse when token is null`() {
        // Given
        LinkedInOAuthController.token = null
        val content = "Test post content"

        // When
        val result = service.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("No access token available"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is null`() {
        // Given
        LinkedInOAuthController.token = "test_token"
        val content: String? = null

        // When
        val result = service.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is blank`() {
        // Given
        LinkedInOAuthController.token = "test_token"
        val content = "   "

        // When
        val result = service.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is empty`() {
        // Given
        LinkedInOAuthController.token = "test_token"
        val content = ""

        // When
        val result = service.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }
}
