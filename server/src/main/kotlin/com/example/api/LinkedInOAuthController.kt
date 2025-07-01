package com.example.api

import com.linkedin.api.client.LinkedInProfileClient
import com.linkedin.api.client.LinkedInDataPortabilityClient
import com.linkedin.api.client.LinkedInPostsClient
import com.linkedin.api.client.LinkedInGenericClient
import com.linkedin.api.dto.LinkedInPostRequest
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
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    @Autowired
    private lateinit var linkedInDataPortabilityClient: LinkedInDataPortabilityClient

    @Autowired
    private lateinit var linkedInPostsClient: LinkedInPostsClient

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
     * Make a Public profile request with LinkedIn API using the userinfo endpoint
     * as described in the LinkedIn OpenID Connect documentation
     * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2
     *
     * @return Public profile of user including the 'sub' field
     */
    @RequestMapping(value = ["/profile"])
    fun profile(): String {
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

    /**
     * Get the Person URN for the authenticated user using the userinfo endpoint
     * as described in the LinkedIn OpenID Connect documentation
     * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2
     *
     * This method reuses the profile() response to extract the 'sub' field
     *
     * @return The Person URN in the format urn:li:person:{sub}
     */
    @RequestMapping(value = ["/getPersonUrn"])
    fun getPersonUrn(): String {
        // Get the profile data from the profile() method
        val profileResponse = profile()
        
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
    @RequestMapping(value = ["/getOrganizationUrns"])
    fun getOrganizationUrns(): String {
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
     * Create a text-only post on LinkedIn using the Posts API
     * Automatically retrieves the current user's URN and uses it as the author
     *
     * @param content The text content of the post
     * @return Response from the LinkedIn Posts API
     */
    @RequestMapping(value = ["/createPost"])
    fun createPost(@RequestParam(required = false) content: String?): String {
        if (token == null) {
            return "{\"error\": \"No access token available. Please generate a token first.\"}"
        }

        if (content.isNullOrBlank()) {
            return "{\"error\": \"Post content cannot be empty.\"}"
        }

        try {
            // First, get the current user's URN
            val personUrn = getCurrentUserUrn(token!!)
            if (personUrn.startsWith("{\"error\"")) {
                return personUrn // Return the error message
            }

            // Create the post request using the data class
            val postRequest = LinkedInPostRequest(
                author = personUrn,
                commentary = content
            )

            // Make the POST request to LinkedIn Posts API using Feign client
            val response = linkedInPostsClient.createPost(
                authorization = "Bearer $token",
                linkedInVersion = "202505",
                protocolVersion = "2.0.0",
                postRequest = postRequest
            )

            // Check if the post was created successfully
            if (response.status() in 200..299) {
                val postId = response.headers()["x-restli-id"]?.firstOrNull() ?: "Unknown"
                return "{\"success\": true, \"message\": \"Post created successfully as $personUrn\", \"postId\": \"$postId\"}"
            } else {
                return "{\"error\": \"Failed to create post. Status: ${response.status()}\"}"
            }

        } catch (e: Exception) {
            return "{\"error\": \"${e.message?.replace("\"", "\\\"")}\"}"
        }
    }

    /**
     * Helper method to get the current user's URN using the userinfo endpoint
     * as described in the LinkedIn OpenID Connect documentation
     * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2
     *
     * @param headers The HTTP headers with authorization token
     * @return The user's URN in the format urn:li:person:{sub}
     */
    private fun getCurrentUserUrn(token: String): String {
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
