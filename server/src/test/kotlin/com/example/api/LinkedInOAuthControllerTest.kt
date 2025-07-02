package com.example.api

import com.example.api.dto.ErrorResponse
import com.example.api.dto.TokenResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class LinkedInOAuthControllerTest {

    private val controller = LinkedInOAuthController()

    @Test
    fun `getToken should return TokenResponse when token exists`() {
        // Given
        LinkedInOAuthController.token = "test_access_token"

        // When
        val result = controller.getToken()

        // Then
        assertTrue(result is TokenResponse)
        val tokenResponse = result as TokenResponse
        assertEquals("test_access_token", tokenResponse.accessToken)
        assertEquals("Bearer", tokenResponse.tokenType)
    }

    @Test
    fun `getToken should return ErrorResponse when token is null`() {
        // Given
        LinkedInOAuthController.token = null

        // When
        val result = controller.getToken()

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("No access token available"))
    }

    @Test
    fun `memberConnections should return ErrorResponse when token is null`() {
        // Given
        LinkedInOAuthController.token = null

        // When
        val result = controller.memberConnections()

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("no_token", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("No access token available"))
    }

}
