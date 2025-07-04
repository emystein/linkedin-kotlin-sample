package com.example.api

import com.example.server.common.AccessToken
import com.example.api.ErrorResponse
import com.linkedin.service.LinkedInProfileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for LinkedIn Profile API operations
 * Base URL: /profile
 */
@RestController
@RequestMapping("/profile")
class LinkedInProfileController {

    @Autowired
    private lateinit var linkedInProfileService: LinkedInProfileService

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
        return linkedInProfileService.getProfileInfo(AccessToken(token))
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
        val token = LinkedInOAuthController.token
        if (token == null) {
            return ErrorResponse("no_token", "No access token available. Please generate a token first.")
        }
        return linkedInProfileService.getPersonUrn(AccessToken(token))
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
        return linkedInProfileService.getOrganizationUrns(AccessToken(token))
    }

    /**
     * Helper method to get the current user's URN using the userinfo endpoint
     * This method is used by other controllers that need the user URN
     *
     * @param token The access token
     * @return The user's URN in the format urn:li:person:{sub}
     */
    fun getCurrentUserUrn(token: String): String {
        return linkedInProfileService.getCurrentUserUrn(AccessToken(token))
    }
}
