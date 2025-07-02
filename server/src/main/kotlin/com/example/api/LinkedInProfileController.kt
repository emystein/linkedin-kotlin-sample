package com.example.api

import com.example.api.dto.ErrorResponse
import com.example.api.dto.OrganizationAccessResponse
import com.example.api.dto.PersonUrnResponse
import com.example.api.dto.ProfileInfoResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.linkedin.api.client.LinkedInProfileClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Controller for LinkedIn Profile API operations
 * Base URL: /profile
 */
@RestController
@RequestMapping("/profile")
class LinkedInProfileController {

    @Autowired
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    private val logger = Logger.getLogger(LinkedInProfileController::class.java.name)
    private val objectMapper = ObjectMapper()

    /**
     * Make a Public profile request with LinkedIn API using the userinfo endpoint
     * as described in the LinkedIn OpenID Connect documentation
     * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2
     *
     * @return Public profile of user including the 'sub' field
     */
    @RequestMapping(value = ["/info"])
    fun getProfileInfo(): Any {
        val token = LinkedInOAuthController.token
        if (token == null) {
            return ErrorResponse("no_token", "No access token available. Please generate a token first.")
        }

        try {
            val response = linkedInProfileClient.getUserInfo("Bearer $token")
            return objectMapper.readValue(response, ProfileInfoResponse::class.java)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error retrieving profile info", e)
            return ErrorResponse("profile_error", "Failed to process profile data: ${e.message}")
        }
    }

    /**
     * Get the Person URN for the authenticated user using the userinfo endpoint
     * as described in the LinkedIn OpenID Connect documentation
     * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2
     *
     * This method reuses the getProfileInfo() response to extract the 'sub' field
     *
     * @return The Person URN in the format urn:li:person:{sub}
     */
    @RequestMapping(value = ["/person-urn"])
    fun getPersonUrn(): Any {
        // Get the profile data from the getProfileInfo() method
        val profileResponse = getProfileInfo()

        // Check if there was an error getting the profile
        if (profileResponse is ErrorResponse) {
            return profileResponse // Return the error response
        }

        try {
            val profileInfo = profileResponse as ProfileInfoResponse
            val sub = profileInfo.sub

            if (!sub.isNullOrEmpty()) {
                val personUrn = "urn:li:person:$sub"
                return PersonUrnResponse(personUrn = personUrn)
            } else {
                return ErrorResponse("missing_sub", "Could not extract 'sub' field from profile response")
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error extracting person URN", e)
            return ErrorResponse("urn_extraction_error", "Failed to process profile data: ${e.message}")
        }
    }

    /**
     * Get the Organization URNs that the authenticated user has access to
     *
     * @return A list of Organization URNs in the format urn:li:organization:{id}
     */
    @RequestMapping(value = ["/organization-urns"])
    fun getOrganizationUrns(): Any {
        val token = LinkedInOAuthController.token
        if (token == null) {
            return ErrorResponse("no_token", "No access token available. Please generate a token first.")
        }

        try {
            val response = linkedInProfileClient.getOrganizationAccess("Bearer $token")
            return objectMapper.readValue(response, OrganizationAccessResponse::class.java)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error retrieving organization access", e)
            return ErrorResponse("organization_access_error", "Failed to retrieve organization access: ${e.message}")
        }
    }

    /**
     * Helper method to get the current user's URN using the userinfo endpoint
     * This method is used by other controllers that need the user URN
     *
     * @param token The access token
     * @return The user's URN in the format urn:li:person:{sub}
     */
    fun getCurrentUserUrn(token: String): String {
        try {
            // Call the userinfo endpoint to get user information
            logger.info("Making request to LinkedIn userinfo API")

            val response = linkedInProfileClient.getUserInfo("Bearer $token")
            logger.info("Response body: $response")

            // Parse the response to extract the 'sub' field
            val profileInfo = objectMapper.readValue(response, ProfileInfoResponse::class.java)
            val sub = profileInfo.sub

            if (!sub.isNullOrEmpty()) {
                return "urn:li:person:$sub"
            } else {
                return "{\"error\": \"Could not extract 'sub' field from userinfo response\"}"
            }
        } catch (e: Exception) {
            logger.severe("Error retrieving user URN: ${e.message}")
            e.printStackTrace()
            return "{\"error\": \"Failed to retrieve user URN: ${e.message?.replace("\"", "\\\"")}\"}"
        }
    }
}
