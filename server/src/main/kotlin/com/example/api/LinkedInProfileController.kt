package com.example.api

import com.linkedin.api.client.LinkedInProfileClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    /**
     * Make a Public profile request with LinkedIn API using the userinfo endpoint
     * as described in the LinkedIn OpenID Connect documentation
     * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2
     *
     * @return Public profile of user including the 'sub' field
     */
    @RequestMapping(value = ["/info"])
    fun getProfileInfo(): String {
        val token = LinkedInOAuthController.token
        if (token == null) {
            return "{\"error\": \"No access token available. Please generate a token first.\"}"
        }

        try {
            return linkedInProfileClient.getUserInfo("Bearer $token")
        } catch (e: Exception) {
            return "{\"error\": \"Failed to process profile data: ${e.message?.replace("\"", "\\\"")}\"}"
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
    fun getPersonUrn(): String {
        // Get the profile data from the getProfileInfo() method
        val profileResponse = getProfileInfo()
        
        // Check if there was an error getting the profile
        if (profileResponse.contains("\"error\":")) {
            return profileResponse // Return the error message
        }
        
        try {
            // Extract the 'sub' field from the profile response
            val subRegex = "\"sub\":\\s*\"([^\"]+)\"".toRegex()
            val subMatch = subRegex.find(profileResponse)
            val sub = subMatch?.groupValues?.getOrNull(1) ?: ""
            
            if (sub.isNotEmpty()) {
                val personUrn = "urn:li:person:$sub"
                return "{\"personUrn\": \"$personUrn\"}"
            } else {
                return "{\"error\": \"Could not extract 'sub' field from profile response: $profileResponse\"}"
            }
        } catch (e: Exception) {
            return "{\"error\": \"Failed to process profile data: ${e.message?.replace("\"", "\\\"")}\"}"
        }
    }

    /**
     * Get the Organization URNs that the authenticated user has access to
     *
     * @return A list of Organization URNs in the format urn:li:organization:{id}
     */
    @RequestMapping(value = ["/organization-urns"])
    fun getOrganizationUrns(): String {
        val token = LinkedInOAuthController.token
        if (token == null) {
            return "{\"error\": \"No access token available. Please generate a token first.\"}"
        }

        try {
            return linkedInProfileClient.getOrganizationAccess("Bearer $token")
        } catch (e: Exception) {
            return "{\"error\": \"${e.message?.replace("\"", "\\\"")}\"}"
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

            // Extract the 'sub' field from the response
            val subRegex = "\"sub\":\\s*\"([^\"]+)\"".toRegex()
            val subMatch = subRegex.find(response)
            val sub = subMatch?.groupValues?.getOrNull(1) ?: ""

            if (sub.isNotEmpty()) {
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
