package com.example.api

import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders

import java.util.logging.Level
import java.util.logging.Logger

/*
 * Getting Started with LinkedIn's Marketing APIs ,
 * Documentation: https://docs.microsoft.com/en-us/linkedin/marketing/getting-started
 * The additional scopes required to use these functions are:
 * 'rw_ads, rw_organization_admin'
 * You can invoke these functions independently with valid access token string as a parameter.
 * More Docs: https://docs.microsoft.com/en-us/linkedin/marketing/integrations/ads/account-structure/create-and-manage-account-users
 */

@RestController
class LinkedInMarketingController {

    private val logger = Logger.getLogger(LinkedInMarketingController::class.java.name)
    private val lmsTemplate = RestTemplate()

    /**
     * Find Ad Accounts for a user
     *
     * @return Ad Accounts for a user
     */
    @RequestMapping(value = ["/findAdAccounts"])
    fun findAdAccounts(): String {
        val header = getHeader()
        return try {
            val endpoint = "https://api.linkedin.com/v2/adAccountUsersV2?q=authenticatedUser&oauth2_access_token="
            val token = LinkedInOAuthController.token
            val response = lmsTemplate.exchange(
                endpoint + token,
                HttpMethod.GET,
                HttpEntity<Any>(header),
                String::class.java
            ).body
            logger.log(Level.INFO, "Find Ad Accounts API call successful.")
            response ?: ""
        } catch (e: HttpStatusCodeException) {
            logger.log(Level.SEVERE, "Error finding Ad Accounts: {0}", e.responseBodyAsString)
            e.responseBodyAsString
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error finding Ad Accounts: {0}", e.message)
            "Error finding Ad Accounts: ${e.message}"
        }
    }

    /**
     * Find User Roles
     *
     * @return User Roles
     */
    @RequestMapping(value = ["/getUserOrgAccess"])
    fun getUserOrgAccess(): String {
        val header = getHeader()
        return try {
            val endpoint = "https://api.linkedin.com/v2/organizationAcls?q=roleAssignee&oauth2_access_token="
            val token = LinkedInOAuthController.token
            val response = lmsTemplate.exchange(
                endpoint + token,
                HttpMethod.GET,
                HttpEntity<Any>(header),
                String::class.java
            ).body
            logger.log(Level.INFO, "Find User Roles API call successful.")
            response ?: ""
        } catch (e: HttpStatusCodeException) {
            logger.log(Level.SEVERE, "Error finding User Roles: {0}", e.responseBodyAsString)
            e.responseBodyAsString
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error finding User Roles: {0}", e.message)
            "Error finding User Roles: ${e.message}"
        }
    }

    /**
     * Get Header
     *
     * @return HttpHeaders
     */
    private fun getHeader(): HttpHeaders {
        val header = HttpHeaders()
        header.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, Marketing)")
        return header
    }
}
