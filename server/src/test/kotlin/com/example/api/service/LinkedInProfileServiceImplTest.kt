package com.example.api.service

import com.example.common.AccessToken
import com.example.api.dto.ErrorResponse
import com.example.api.dto.OrganizationAccessResponse
import com.example.api.dto.PersonUrnResponse
import com.example.api.dto.ProfileInfoResponse
import com.linkedin.api.client.LinkedInProfileClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class LinkedInProfileServiceImplTest {

    @Mock
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    private lateinit var service: LinkedInProfileServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = LinkedInProfileServiceImpl()
        // Use reflection to inject the mock client
        val clientField = LinkedInProfileServiceImpl::class.java.getDeclaredField("linkedInProfileClient")
        clientField.isAccessible = true
        clientField.set(service, linkedInProfileClient)
    }

    @Test
    fun `getProfileInfo should return ProfileInfoResponse when successful`() {
        // Given
        val token = AccessToken("valid_token")
        val mockResponse = """{"sub":"123456","name":"John Doe","email":"john@example.com"}"""
        `when`(linkedInProfileClient.getUserInfo("Bearer valid_token")).thenReturn(mockResponse)

        // When
        val result = service.getProfileInfo(token)

        // Then
        assertTrue(result is ProfileInfoResponse)
        val profileResponse = result as ProfileInfoResponse
        assertEquals("123456", profileResponse.sub)
        assertEquals("John Doe", profileResponse.name)
        assertEquals("john@example.com", profileResponse.email)
    }

    @Test
    fun `getProfileInfo should return ErrorResponse when API call fails`() {
        // Given
        val token = AccessToken("invalid_token")
        `when`(linkedInProfileClient.getUserInfo("Bearer invalid_token")).thenThrow(RuntimeException("API Error"))

        // When
        val result = service.getProfileInfo(token)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("profile_error", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Failed to process profile data"))
    }

    @Test
    fun `getPersonUrn should return PersonUrnResponse when successful`() {
        // Given
        val token = AccessToken("valid_token")
        val mockResponse = """{"sub":"123456","name":"John Doe"}"""
        `when`(linkedInProfileClient.getUserInfo("Bearer valid_token")).thenReturn(mockResponse)

        // When
        val result = service.getPersonUrn(token)

        // Then
        assertTrue(result is PersonUrnResponse)
        val urnResponse = result as PersonUrnResponse
        assertEquals("urn:li:person:123456", urnResponse.personUrn)
    }

    @Test
    fun `getPersonUrn should return ErrorResponse when sub is missing`() {
        // Given
        val token = AccessToken("valid_token")
        val mockResponse = """{"name":"John Doe"}"""
        `when`(linkedInProfileClient.getUserInfo("Bearer valid_token")).thenReturn(mockResponse)

        // When
        val result = service.getPersonUrn(token)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("missing_sub", errorResponse.error)
    }

    @Test
    fun `getOrganizationUrns should return ErrorResponse when API call fails`() {
        // Given
        val token = AccessToken("invalid_token")
        `when`(linkedInProfileClient.getOrganizationAccess("Bearer invalid_token")).thenThrow(RuntimeException("API Error"))

        // When
        val result = service.getOrganizationUrns(token)

        // Then
        assertTrue(result is ErrorResponse)
        val errorResponse = result as ErrorResponse
        assertEquals("organization_access_error", errorResponse.error)
        assertTrue(errorResponse.message!!.contains("Failed to retrieve organization access"))
    }

    @Test
    fun `getCurrentUserUrn should return URN when successful`() {
        // Given
        val token = AccessToken("valid_token")
        val mockResponse = """{"sub":"123456","name":"John Doe"}"""
        `when`(linkedInProfileClient.getUserInfo("Bearer valid_token")).thenReturn(mockResponse)

        // When
        val result = service.getCurrentUserUrn(token)

        // Then
        assertEquals("urn:li:person:123456", result)
    }

    @Test
    fun `getCurrentUserUrn should throw Exception when sub is missing`() {
        // Given
        val token = AccessToken("valid_token")
        val mockResponse = """{"name":"John Doe"}"""
        `when`(linkedInProfileClient.getUserInfo("Bearer valid_token")).thenReturn(mockResponse)

        // When & Then
        val exception = assertThrows(Exception::class.java) {
            service.getCurrentUserUrn(token)
        }
        assertEquals("Could not extract 'sub' field from userinfo response", exception.message)
    }

    @Test
    fun `getCurrentUserUrn should throw Exception when API call fails`() {
        // Given
        val token = AccessToken("invalid_token")
        `when`(linkedInProfileClient.getUserInfo("Bearer invalid_token")).thenThrow(RuntimeException("API Error"))

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            service.getCurrentUserUrn(token)
        }
        assertEquals("API Error", exception.message)
    }

    @Test
    fun `getOrganizationUrns should return OrganizationAccessResponse when successful`() {
        // Given
        val token = AccessToken("valid_token")
        val mockResponse = """{"elements":[{"organization~":{"id":"123","localizedName":"Test Org"}}]}"""
        `when`(linkedInProfileClient.getOrganizationAccess("Bearer valid_token")).thenReturn(mockResponse)

        // When
        val result = service.getOrganizationUrns(token)

        // Then
        assertTrue(result is OrganizationAccessResponse)
    }
}
