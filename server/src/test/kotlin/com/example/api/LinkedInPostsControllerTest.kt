package com.example.api

import com.example.api.dto.ErrorResponse
import com.example.api.dto.PostCreationResponse
import com.example.api.service.AccessToken
import com.example.api.service.LinkedInPostsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LinkedInPostsControllerTest {

    @Mock
    private lateinit var linkedInPostsService: LinkedInPostsService

    private lateinit var controller: LinkedInPostsController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        controller = LinkedInPostsController()
        // Use reflection to inject the mock service
        val serviceField = LinkedInPostsController::class.java.getDeclaredField("linkedInPostsService")
        serviceField.isAccessible = true
        serviceField.set(controller, linkedInPostsService)
    }

    @Test
    fun `createPost should return ErrorResponse when token is null`() {
        // Given
        val content = "Test post content"
        LinkedInOAuthController.token = null

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("No access token available"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is null`() {
        // Given
        val content: String? = null
        LinkedInOAuthController.token = "test_token"
        val expectedError = ErrorResponse("empty_content", "Post content cannot be empty.")
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content)).thenReturn(expectedError)

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is blank`() {
        // Given
        val content = "   "
        LinkedInOAuthController.token = "test_token"
        val expectedError = ErrorResponse("empty_content", "Post content cannot be empty.")
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content)).thenReturn(expectedError)

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is empty`() {
        // Given
        val content = ""
        LinkedInOAuthController.token = "test_token"
        val expectedError = ErrorResponse("empty_content", "Post content cannot be empty.")
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content)).thenReturn(expectedError)

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("empty_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }
}
