package com.example.api.service

import com.example.api.dto.ErrorResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class LinkedInPostsServiceImplTest {

    private val service = LinkedInPostsServiceImpl()

    @Test
    fun `createPost should return ErrorResponse when content is null`() {
        // Given
        val token = AccessToken("test_token")
        val content: String? = null

        // When
        val result = service.createPost(token, content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is blank`() {
        // Given
        val token = AccessToken("test_token")
        val content = "   "

        // When
        val result = service.createPost(token, content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is empty`() {
        // Given
        val token = AccessToken("test_token")
        val content = ""

        // When
        val result = service.createPost(token, content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }
}
