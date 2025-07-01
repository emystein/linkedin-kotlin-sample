package com.example.api

import com.example.api.client.LinkedInMarketingClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @Autowired
    private lateinit var linkedInMarketingClient: LinkedInMarketingClient

    /**
     * Find Ad Accounts for a user
     *
     * @return Ad Accounts for a user
     */
    @RequestMapping(value = ["/findAdAccounts"])
    fun findAdAccounts(): String {
        return try {
            val token = LinkedInOAuthController.token ?: return "Error: No access token available"
            val response = linkedInMarketingClient.getAdAccounts("Bearer $token", accessToken = token)
            logger.log(Level.INFO, "Find Ad Accounts API call successful.")
            response
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
        return try {
            val token = LinkedInOAuthController.token ?: return "Error: No access token available"
            val response = linkedInMarketingClient.getOrganizationAcls("Bearer $token", accessToken = token)
            logger.log(Level.INFO, "Find User Roles API call successful.")
            response
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error finding User Roles: {0}", e.message)
            "Error finding User Roles: ${e.message}"
        }
    }


}
