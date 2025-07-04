package com.example.api

import com.example.server.common.AccessToken
import com.linkedin.service.PostCreationResponse
import com.linkedin.service.LinkedInPostsService
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
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content))
            .thenThrow(IllegalArgumentException("Post content cannot be empty."))

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("invalid_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is blank`() {
        // Given
        val content = "   "
        LinkedInOAuthController.token = "test_token"
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content))
            .thenThrow(IllegalArgumentException("Post content cannot be empty."))

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("invalid_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return ErrorResponse when content is empty`() {
        // Given
        val content = ""
        LinkedInOAuthController.token = "test_token"
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content))
            .thenThrow(IllegalArgumentException("Post content cannot be empty."))

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("invalid_content", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Post content cannot be empty"))
    }

    @Test
    fun `createPost should return PostCreationResponse when successful`() {
        // Given
        val content = "This is a test post"
        LinkedInOAuthController.token = "test_token"
        val expectedResponse = PostCreationResponse.success(postId = "post123", author = "urn:li:person:123456")
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content)).thenReturn(expectedResponse)

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is PostCreationResponse)
        val postResponse = result as PostCreationResponse
        assertTrue(postResponse.success)
        assertEquals("post123", postResponse.postId)
        assertEquals("urn:li:person:123456", postResponse.author)
    }

    @Test
    fun `createPost should return ErrorResponse when service throws general exception`() {
        // Given
        val content = "This is a test post"
        LinkedInOAuthController.token = "test_token"
        `when`(linkedInPostsService.createPost(AccessToken("test_token"), content))
            .thenThrow(RuntimeException("API Error"))

        // When
        val result = controller.createPost(content)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("service_error", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("API Error"))
    }
}
