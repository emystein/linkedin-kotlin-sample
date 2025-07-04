package com.linkedin.service

import com.example.server.common.AccessToken
import com.linkedin.api.client.LinkedInPostsClient
import com.linkedin.api.dto.LinkedInPostRequest
import feign.Response
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LinkedInPostsServiceImplTest {

    @Mock
    private lateinit var linkedInPostsClient: LinkedInPostsClient

    @Mock
    private lateinit var linkedInProfileService: LinkedInProfileService

    @Mock
    private lateinit var mockResponse: Response

    private lateinit var service: LinkedInPostsServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = LinkedInPostsServiceImpl(linkedInPostsClient, linkedInProfileService)
    }

    @Test
    fun `createPost should throw IllegalArgumentException when content is null`() {
        // Given
        val token = AccessToken("test_token")
        val content: String? = null

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.createPost(token, content)
        }
        assertEquals("Post content cannot be empty.", exception.message)
    }

    @Test
    fun `createPost should throw IllegalArgumentException when content is blank`() {
        // Given
        val token = AccessToken("test_token")
        val content = "   "

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.createPost(token, content)
        }
        assertEquals("Post content cannot be empty.", exception.message)
    }

    @Test
    fun `createPost should throw IllegalArgumentException when content is empty`() {
        // Given
        val token = AccessToken("test_token")
        val content = ""

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.createPost(token, content)
        }
        assertEquals("Post content cannot be empty.", exception.message)
    }

    @Test
    fun `createPost should throw Exception when profile service fails`() {
        // Given
        val token = AccessToken("test_token")
        val content = "This is a test post"

        `when`(linkedInProfileService.getCurrentUserUrn(token)).thenThrow(RuntimeException("Profile service error"))

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            service.createPost(token, content)
        }
        assertEquals("Profile service error", exception.message)
    }

    @Test
    fun `createPost should return PostCreationResponse when successful with valid response`() {
        // Given
        val token = AccessToken("test_token")
        val content = "This is a test post"
        val personUrn = "urn:li:person:123456"
        val postId = "post123"

        `when`(linkedInProfileService.getCurrentUserUrn(token)).thenReturn(personUrn)
        `when`(mockResponse.status()).thenReturn(201)
        `when`(mockResponse.headers()).thenReturn(mapOf("x-restli-id" to listOf(postId)))
        `when`(linkedInPostsClient.createPost(
            authorization = "Bearer test_token",
            postRequest = LinkedInPostRequest(author = personUrn, commentary = content)
        )).thenReturn(mockResponse)

        // When
        val result = service.createPost(token, content)

        // Then
        assertTrue(result is PostCreationResponse)
        val response = result as PostCreationResponse
        assertTrue(response.success)
        assertEquals("Post created successfully", response.message)
        assertEquals(postId, response.postId)
        assertEquals(personUrn, response.author)
    }

    @Test
    fun `createPost should throw Exception when API returns error status`() {
        // Given
        val token = AccessToken("test_token")
        val content = "This is a test post"
        val personUrn = "urn:li:person:123456"

        `when`(linkedInProfileService.getCurrentUserUrn(token)).thenReturn(personUrn)
        `when`(mockResponse.status()).thenReturn(400)
        `when`(linkedInPostsClient.createPost(
            authorization = "Bearer test_token",
            postRequest = LinkedInPostRequest(author = personUrn, commentary = content)
        )).thenReturn(mockResponse)

        // When & Then
        val exception = assertThrows(Exception::class.java) {
            service.createPost(token, content)
        }
        assertTrue(exception.message!!.contains("Failed to create post. Status: 400"))
    }

    @Test
    fun `createPost should handle missing post ID header gracefully`() {
        // Given
        val token = AccessToken("test_token")
        val content = "This is a test post"
        val personUrn = "urn:li:person:123456"

        `when`(linkedInProfileService.getCurrentUserUrn(token)).thenReturn(personUrn)
        `when`(mockResponse.status()).thenReturn(201)
        `when`(mockResponse.headers()).thenReturn(emptyMap())
        `when`(linkedInPostsClient.createPost(
            authorization = "Bearer test_token",
            postRequest = LinkedInPostRequest(author = personUrn, commentary = content)
        )).thenReturn(mockResponse)

        // When
        val result = service.createPost(token, content)

        // Then
        assertTrue(result is PostCreationResponse)
        val response = result as PostCreationResponse
        assertTrue(response.success)
        assertEquals("Post created successfully", response.message)
        assertEquals("Unknown", response.postId)
        assertEquals(personUrn, response.author)
    }
}
