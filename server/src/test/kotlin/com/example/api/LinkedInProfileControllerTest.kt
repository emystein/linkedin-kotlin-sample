package com.example.api

import com.example.api.dto.ErrorResponse
import com.example.api.dto.PersonUrnResponse
import com.example.api.dto.ProfileInfoResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class LinkedInProfileControllerTest {

    private val controller = LinkedInProfileController()

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
    fun `getPersonUrn should return ErrorResponse when profile info returns error`() {
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
    fun `getCurrentUserUrn should return error JSON when token is invalid`() {
        // Given
        val invalidToken = "invalid_token"

        // When
        val result = controller.getCurrentUserUrn(invalidToken)

        // Then
        assertTrue(result.contains("error"))
    }
}
