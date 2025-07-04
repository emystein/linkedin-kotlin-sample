package com.example.api

import com.example.server.common.AccessToken
import com.example.api.ErrorResponse
import com.linkedin.service.LinkedInProfileService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LinkedInProfileControllerTest {

    @Mock
    private lateinit var linkedInProfileService: LinkedInProfileService

    private lateinit var controller: LinkedInProfileController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        controller = LinkedInProfileController()
        // Use reflection to inject the mock service
        val serviceField = LinkedInProfileController::class.java.getDeclaredField("linkedInProfileService")
        serviceField.isAccessible = true
        serviceField.set(controller, linkedInProfileService)
    }

    @Test
    fun `getProfileInfo should return ErrorResponse when token is null`() {
        // Given
        LinkedInOAuthController.token = null

        // When
        val result = controller.getProfileInfo()

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("No access token available"))
    }

    @Test
    fun `getPersonUrn should return ErrorResponse when token is null`() {
        // Given
        LinkedInOAuthController.token = null

        // When
        val result = controller.getPersonUrn()

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
    }

    @Test
    fun `getOrganizationUrns should return ErrorResponse when token is null`() {
        // Given
        LinkedInOAuthController.token = null

        // When
        val result = controller.getOrganizationUrns()

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("No access token available"))
    }

    @Test
    fun `getCurrentUserUrn should return error JSON when service returns error`() {
        // Given
        val invalidToken = "invalid_token"
        val expectedError = "{\"error\": \"Failed to retrieve user URN: Invalid token\"}"
        `when`(linkedInProfileService.getCurrentUserUrn(AccessToken(invalidToken))).thenReturn(expectedError)

        // When
        val result = controller.getCurrentUserUrn(invalidToken)

        // Then
        assertTrue(result.contains("error"))
    }
}
