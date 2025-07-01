package com.example.api

import com.linkedin.api.client.LinkedInDataPortabilityClient
import com.linkedin.api.client.LinkedInGenericClient
import org.springframework.boot.web.client.RestTemplateBuilder
import com.linkedin.oauth.builder.ScopeBuilder
import com.linkedin.oauth.pojo.AccessToken
import com.linkedin.oauth.service.LinkedInOAuthService
import com.linkedin.oauth.util.Constants.REQUEST_TOKEN_URL
import com.linkedin.oauth.util.Constants.TOKEN_INTROSPECTION_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/*
 * Getting Started with LinkedIn's OAuth APIs ,
 * Documentation: https://docs.microsoft.com/en-us/linkedin/shared/authentication/authentication?context=linkedin/context
 */

@RestController
class LinkedInOAuthController {

    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    private lateinit var linkedInDataPortabilityClient: LinkedInDataPortabilityClient

    @Autowired
    private lateinit var linkedInGenericClient: LinkedInGenericClient

    private fun getRestTemplate(): org.springframework.web.client.RestTemplate {
        return restTemplateBuilder.build()
    }

    // Configuration values from environment variables
    @Value("\${CLIENT_ID:}")
    private lateinit var clientId: String

    @Value("\${CLIENT_SECRET:}")
    private lateinit var clientSecret: String

    @Value("\${REDIRECT_URI:http://localhost:8080/login}")
    private lateinit var redirectUri: String

    @Value("\${SCOPE:openid,profile,email}")
    private lateinit var scope: String

    @Value("\${CLIENT_URL:http://localhost:8989/}")
    private lateinit var clientUrl: String

    companion object {
        var token: String? = null
    }

    var refresh_token: String? = null
    lateinit var service: LinkedInOAuthService

    private val logger = Logger.getLogger(LinkedInOAuthController::class.java.name)

    /**
     * Make a Login request with LinkedIN Oauth API
     *
     * @param code optional Authorization code
     * @return Redirects to the client UI after successful token creation
     */
    @RequestMapping(value = ["/login"])
    @Throws(Exception::class)
    fun oauth(@RequestParam(name = "code", required = false) code: String?): RedirectView {
        // Construct the LinkedInOAuthService instance for use
        service = LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(clientId)
                .apiSecret(clientSecret)
                .defaultScope(ScopeBuilder().withScopes(*scope.split(",").toTypedArray()))
                .callback(redirectUri)
                .build()

        val secretState = "secret" + Random().nextInt(999_999)
        val authorizationUrl = service.createAuthorizationUrlBuilder()
                .state(secretState)
                .build()

        val redirectView = RedirectView()

        if (code != null && code.isNotEmpty()) {
            logger.log(Level.INFO, "Authorization code not empty, trying to generate a 3-legged OAuth token.")

            val accessToken = arrayOf(AccessToken())
            val request = service.getAccessToken3Legged(code)
            val response = getRestTemplate().postForObject(REQUEST_TOKEN_URL, request, String::class.java)
            if (response != null) {
                accessToken[0] = service.convertJsonTokenToPojo(response)
            }

            token = accessToken[0].accessToken
            refresh_token = accessToken[0].refreshToken

            logger.log(Level.INFO, "Generated Access token and Refresh Token.")

            redirectView.url = clientUrl
        } else {
            redirectView.url = authorizationUrl
        }
        return redirectView
    }

    /**
     * Create 2 legged auth access token
     *
     * @return Redirects to the client UI after successful token creation
     */
    @RequestMapping(value = ["/twoLeggedAuth"])
    @Throws(Exception::class)
    fun two_legged_auth(): RedirectView {
        val redirectView = RedirectView()
        // Construct the LinkedInOAuthService instance for use
        service = LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(clientId)
                .apiSecret(clientSecret)
                .defaultScope(ScopeBuilder().withScopes(*scope.split(",").toTypedArray()))
                .callback(redirectUri)
                .build()

        val accessToken = arrayOf(AccessToken())

        val request = service.getAccessToken2Legged()
        val response = getRestTemplate().postForObject(REQUEST_TOKEN_URL, request, String::class.java)
        if (response != null) {
            accessToken[0] = service.convertJsonTokenToPojo(response)
            token = accessToken[0].accessToken
        }

        logger.log(Level.INFO, "Generated Access token.")

        redirectView.url = clientUrl
        return redirectView
    }

    /**
     * Make a Token Introspection request with LinkedIN API
     *
     * @return check the Time to Live (TTL) and status (active/expired) for all token
     */
    @RequestMapping(value = ["/tokenIntrospection"])
    @Throws(Exception::class)
    fun token_introspection(): String {
        return if (::service.isInitialized) {
            val request = service.introspectToken(token ?: "")
            val response = getRestTemplate().postForObject(TOKEN_INTROSPECTION_URL, request, String::class.java)
            logger.log(Level.INFO, "Token introspected. Details are {0}", response)
            response ?: ""
        } else {
            "Error introspecting token, service is not initiated"
        }
    }

    /**
     * Make a Refresh Token request with LinkedIN API
     *
     * @return get a new access token when your current access token expire
     */
    @RequestMapping(value = ["/refreshToken"])
    @Throws(IOException::class)
    fun refresh_token(): String? {
        var response: String? = null
        if (refresh_token != null) {
            val refreshTokenCopy = refresh_token ?: ""
            val request = service.getAccessTokenFromRefreshToken(refreshTokenCopy)
            response = getRestTemplate().postForObject(REQUEST_TOKEN_URL, request, String::class.java)
            logger.log(Level.INFO, "Used Refresh Token to generate a new access token successfully.")
            return response
        } else {
            logger.log(Level.INFO, "Refresh Token cannot be empty. Generate 3L Access Token and Retry again.")
            return response
        }
    }



    /**
     * Get the current access token
     *
     * @return The current access token or empty string if none exists
     */
    @RequestMapping(value = ["/getToken"])
    fun getToken(): String {
        return token ?: ""
    }

    /**
     * Get member connections using the Member Data Portability API
     *
     * @return Member connections data in JSON format
     */
    @RequestMapping(value = ["/memberConnections"])
    fun memberConnections(): String {
        if (token == null) {
            return "{\"error\": \"No access token available. Please generate a token first.\"}"
        }

        try {
            // Query the Member Snapshot API for CONNECTIONS domain
            val response = linkedInDataPortabilityClient.getMemberSnapshotData("Bearer $token")

            if (response.isEmpty()) {
                return "{\"error\": \"Empty response from Member Snapshot API\"}"
            }

            // Check if we need to handle pagination
            var allData = response
            var nextPageUrl = extractNextPageUrl(response)

            // Process up to 10 pages of data to avoid infinite loops
            var pageCount = 1
            val maxPages = 10

            while (nextPageUrl.isNotEmpty() && pageCount < maxPages) {
                val fullNextPageUrl = java.net.URI("https://api.linkedin.com$nextPageUrl")

                val nextPageResponse = linkedInGenericClient.getFromUrl(fullNextPageUrl, "Bearer $token")

                if (nextPageResponse.isEmpty()) {
                    break
                }

                // Combine the data (in a real implementation, you would merge the JSON properly)
                allData += "\n--- Page ${pageCount + 1} ---\n$nextPageResponse"

                // Get the next page URL
                nextPageUrl = extractNextPageUrl(nextPageResponse)
                pageCount++
            }

            return allData

        } catch (e: Exception) {
            return "{\"error\": \"${e.message?.replace("\"", "\\\"")}\"}"
        }
    }

    /**
     * Extract the next page URL from the response if it exists
     */
    private fun extractNextPageUrl(response: String): String {
        // Simple regex to extract the next page URL from the JSON response
        val regex = "\"rel\":\\s*\"next\",\\s*\"href\":\\s*\"([^\"]+)\"".toRegex()
        val matchResult = regex.find(response)
        return matchResult?.groupValues?.getOrNull(1)?.replace("\\", "") ?: ""
    }








}
