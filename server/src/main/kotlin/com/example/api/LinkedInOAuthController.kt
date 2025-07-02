package com.example.api

import com.example.api.dto.ErrorResponse
import com.example.api.dto.MemberConnectionsResponse
import com.example.api.dto.RefreshTokenResponse
import com.example.api.dto.TokenIntrospectionResponse
import com.example.api.dto.TokenResponse
import com.fasterxml.jackson.databind.ObjectMapper
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
    private val objectMapper = ObjectMapper()

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
    fun token_introspection(): Any {
        return if (::service.isInitialized) {
            try {
                val request = service.introspectToken(token ?: "")
                val response = getRestTemplate().postForObject(TOKEN_INTROSPECTION_URL, request, String::class.java)
                logger.log(Level.INFO, "Token introspected. Details are {0}", response)

                if (response != null) {
                    objectMapper.readValue(response, TokenIntrospectionResponse::class.java)
                } else {
                    ErrorResponse("empty_response", "Empty response from token introspection API")
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Error during token introspection", e)
                ErrorResponse("introspection_error", "Failed to introspect token: ${e.message}")
            }
        } else {
            ErrorResponse("service_not_initialized", "Error introspecting token, service is not initiated")
        }
    }

    /**
     * Make a Refresh Token request with LinkedIN API
     *
     * @return get a new access token when your current access token expire
     */
    @RequestMapping(value = ["/refreshToken"])
    @Throws(IOException::class)
    fun refresh_token(): Any {
        return if (refresh_token != null) {
            try {
                val refreshTokenCopy = refresh_token ?: ""
                val request = service.getAccessTokenFromRefreshToken(refreshTokenCopy)
                val response = getRestTemplate().postForObject(REQUEST_TOKEN_URL, request, String::class.java)
                logger.log(Level.INFO, "Used Refresh Token to generate a new access token successfully.")

                if (response != null) {
                    val refreshTokenResponse = objectMapper.readValue(response, RefreshTokenResponse::class.java)
                    // Update the stored tokens
                    token = refreshTokenResponse.accessToken ?: token
                    refresh_token = refreshTokenResponse.refreshToken ?: refresh_token
                    refreshTokenResponse
                } else {
                    ErrorResponse("empty_response", "Empty response from refresh token API")
                }
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Error during token refresh", e)
                ErrorResponse("refresh_error", "Failed to refresh token: ${e.message}")
            }
        } else {
            logger.log(Level.INFO, "Refresh Token cannot be empty. Generate 3L Access Token and Retry again.")
            ErrorResponse("missing_refresh_token", "Refresh Token cannot be empty. Generate 3L Access Token and Retry again.")
        }
    }



    /**
     * Get the current access token
     *
     * @return The current access token or error response if none exists
     */
    @RequestMapping(value = ["/getToken"])
    fun getToken(): Any {
        return if (token != null) {
            TokenResponse(accessToken = token!!)
        } else {
            ErrorResponse("no_token", "No access token available. Please generate a token first.")
        }
    }

    /**
     * Get member connections using the Member Data Portability API
     *
     * @return Member connections data as structured response
     */
    @RequestMapping(value = ["/memberConnections"])
    fun memberConnections(): Any {
        if (token == null) {
            return ErrorResponse("no_token", "No access token available. Please generate a token first.")
        }

        try {
            // Query the Member Snapshot API for CONNECTIONS domain
            val response = linkedInDataPortabilityClient.getMemberSnapshotData("Bearer $token")

            if (response.isEmpty()) {
                return ErrorResponse("empty_response", "Empty response from Member Snapshot API")
            }

            // Try to parse the first page response
            return try {
                val firstPageResponse = objectMapper.readValue(response, MemberConnectionsResponse::class.java)

                // For now, return the first page. In a production implementation,
                // you might want to handle pagination differently
                firstPageResponse
            } catch (parseException: Exception) {
                logger.log(Level.WARNING, "Could not parse response as structured data, returning raw response", parseException)
                // If parsing fails, return a generic response with the raw data
                // This handles cases where the API response format might be different
                MemberConnectionsResponse(
                    data = emptyList(),
                    paging = null,
                    metadata = mapOf("raw_response" to response, "parse_error" to (parseException.message ?: "Unknown parsing error"))
                )
            }

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error retrieving member connections", e)
            return ErrorResponse("api_error", "Failed to retrieve member connections: ${e.message}")
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
