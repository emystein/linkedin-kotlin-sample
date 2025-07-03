package com.example.api.service

import com.example.common.AccessToken

interface LinkedInProfileService {
    /**
     * Get public profile information using LinkedIn's userinfo endpoint
     *
     * @param token The access token
     * @return ProfileInfoResponse with user profile data or ErrorResponse if failed
     */
    fun getProfileInfo(token: AccessToken): Any

    /**
     * Get the Person URN for the authenticated user
     *
     * @param token The access token
     * @return PersonUrnResponse with the user's URN or ErrorResponse if failed
     */
    fun getPersonUrn(token: AccessToken): Any

    /**
     * Get the Organization URNs that the authenticated user has access to
     *
     * @param token The access token
     * @return OrganizationAccessResponse with organization data or ErrorResponse if failed
     */
    fun getOrganizationUrns(token: AccessToken): Any

    /**
     * Helper method to get the current user's URN using the userinfo endpoint
     * This method is used by other services that need the user URN
     *
     * @param token The access token
     * @return The user's URN in the format urn:li:person:{sub} or error JSON string
     */
    fun getCurrentUserUrn(token: AccessToken): String
}
