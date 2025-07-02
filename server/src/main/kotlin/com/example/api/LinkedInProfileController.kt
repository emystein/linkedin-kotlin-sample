package com.example.api

import com.example.api.service.LinkedInProfileService
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
        return linkedInProfileService.getProfileInfo()
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
        return linkedInProfileService.getPersonUrn()
    }

    /**
     * Get the Organization URNs that the authenticated user has access to
     *
     * @return A list of Organization URNs in the format urn:li:organization:{id}
     */
    @RequestMapping(value = ["/organization-urns"])
    fun getOrganizationUrns(): Any {
        return linkedInProfileService.getOrganizationUrns()
    }

    /**
     * Helper method to get the current user's URN using the userinfo endpoint
     * This method is used by other controllers that need the user URN
     *
     * @param token The access token
     * @return The user's URN in the format urn:li:person:{sub}
     */
    fun getCurrentUserUrn(token: String): String {
        return linkedInProfileService.getCurrentUserUrn(token)
    }
}
