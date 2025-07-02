package com.example.api.service

/**
 * Service interface for LinkedIn Profile API operations
 * Provides methods for retrieving user profile information, person URNs, and organization access
 */
interface LinkedInProfileService {

    /**
     * Get public profile information using LinkedIn's userinfo endpoint
     * 
     * @return ProfileInfoResponse with user profile data or ErrorResponse if failed
     */
    fun getProfileInfo(): Any

    /**
     * Get the Person URN for the authenticated user
     * 
     * @return PersonUrnResponse with the user's URN or ErrorResponse if failed
     */
    fun getPersonUrn(): Any

    /**
     * Get the Organization URNs that the authenticated user has access to
     * 
     * @return OrganizationAccessResponse with organization data or ErrorResponse if failed
     */
    fun getOrganizationUrns(): Any

    /**
     * Helper method to get the current user's URN using the userinfo endpoint
     * This method is used by other services that need the user URN
     * 
     * @param token The access token
     * @return The user's URN in the format urn:li:person:{sub} or error JSON string
     */
    fun getCurrentUserUrn(token: String): String
}
