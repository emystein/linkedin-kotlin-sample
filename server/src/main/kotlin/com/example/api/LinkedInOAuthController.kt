package com.example.api

import com.linkedin.oauth.builder.ScopeBuilder
import com.linkedin.oauth.pojo.AccessToken
import com.linkedin.oauth.service.LinkedInOAuthService
import com.linkedin.oauth.util.Constants.REQUEST_TOKEN_URL
import com.linkedin.oauth.util.Constants.TOKEN_INTROSPECTION_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.view.RedirectView
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
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

    private fun getRestTemplate(): RestTemplate {
        return restTemplateBuilder.build()
    }

    // Define all inputs in the property file
    private val prop = Properties()
    private val propFileName = "config.properties"

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
        loadProperty()

        // Construct the LinkedInOAuthService instance for use
        service = LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(prop.getProperty("clientId"))
                .apiSecret(prop.getProperty("clientSecret"))
                .defaultScope(ScopeBuilder().withScopes(*prop.getProperty("scope").split(",").toTypedArray()))
                .callback(prop.getProperty("redirectUri"))
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

            prop.setProperty("token", accessToken[0].accessToken)
            token = accessToken[0].accessToken
            refresh_token = accessToken[0].refreshToken

            logger.log(Level.INFO, "Generated Access token and Refresh Token.")

            redirectView.url = prop.getProperty("client_url")
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
        loadProperty()

        val redirectView = RedirectView()
        // Construct the LinkedInOAuthService instance for use
        service = LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(prop.getProperty("clientId"))
                .apiSecret(prop.getProperty("clientSecret"))
                .defaultScope(ScopeBuilder().withScopes(*prop.getProperty("scope").split(",").toTypedArray()))
                .callback(prop.getProperty("redirectUri"))
                .build()

        val accessToken = arrayOf(AccessToken())

        val request = service.getAccessToken2Legged()
        val response = getRestTemplate().postForObject(REQUEST_TOKEN_URL, request, String::class.java)
        if (response != null) {
            accessToken[0] = service.convertJsonTokenToPojo(response)
            prop.setProperty("token", accessToken[0].accessToken)
            token = accessToken[0].accessToken
        }

        logger.log(Level.INFO, "Generated Access token.")

        redirectView.url = prop.getProperty("client_url")
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
        
        val headers = HttpHeaders()
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        
        try {
            val userinfoUrl = "https://api.linkedin.com/v2/userinfo"
            return getRestTemplate().exchange(
                userinfoUrl, 
                HttpMethod.GET, 
                HttpEntity<Any>(headers), 
                String::class.java
            ).body ?: ""
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

        val headers = HttpHeaders()
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        headers.set("LinkedIn-Version", "202312") // Required version header
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json")

        try {
            // Query the Member Snapshot API for CONNECTIONS domain
            val snapshotUrl = "https://api.linkedin.com/rest/memberSnapshotData?q=criteria&domain=CONNECTIONS"

            val response = getRestTemplate().exchange(
                snapshotUrl,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                String::class.java
            ).body ?: ""

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
                val fullNextPageUrl = "https://api.linkedin.com$nextPageUrl"

                val nextPageResponse = getRestTemplate().exchange(
                    fullNextPageUrl,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    String::class.java
                ).body ?: ""

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

        val headers = HttpHeaders()
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        headers.set("LinkedIn-Version", "202312")

        try {
            // Call the organizationAcls API to get organizations the user has access to
            val organizationsUrl = "https://api.linkedin.com/v2/organizationAcls?q=roleAssignee&role=ADMINISTRATOR&projection=(elements*(organization~(id,localizedName)))"
            val response = getRestTemplate().exchange(
                organizationsUrl,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                String::class.java
            ).body ?: ""

            return response
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

        val headers = HttpHeaders()
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer $token")
        headers.set("X-Restli-Protocol-Version", "2.0.0")
        headers.set("LinkedIn-Version", "202505") // Use the appropriate version
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json")

        try {
            // First, get the current user's URN
            val personUrn = getCurrentUserUrn(headers)
            if (personUrn.startsWith("{\"error\"")) {
                return personUrn // Return the error message
            }

            // Create the post request body using the user's URN as the author
            val requestBody = "{"
                .plus("\"author\": \"$personUrn\",")
                .plus("\"commentary\": \"$content\",")
                .plus("\"visibility\": \"PUBLIC\",")
                .plus("\"distribution\": {")
                .plus("\"feedDistribution\": \"MAIN_FEED\",")
                .plus("\"targetEntities\": [],")
                .plus("\"thirdPartyDistributionChannels\": []")
                .plus("},")
                .plus("\"lifecycleState\": \"PUBLISHED\",")
                .plus("\"isReshareDisabledByAuthor\": false")
                .plus("}")

            // Make the POST request to LinkedIn Posts API
            val postsApiUrl = "https://api.linkedin.com/rest/posts"
            val requestEntity = HttpEntity(requestBody, headers)

            val response = getRestTemplate().exchange(
                postsApiUrl,
                HttpMethod.POST,
                requestEntity,
                String::class.java
            )

            // Check if the post was created successfully
            if (response.statusCode.is2xxSuccessful) {
                val postId = response.headers["x-restli-id"]?.firstOrNull() ?: "Unknown"
                return "{\"success\": true, \"message\": \"Post created successfully as $personUrn\", \"postId\": \"$postId\"}"
            } else {
                return "{\"error\": \"Failed to create post. Status: ${response.statusCode}\"}"
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
    private fun getCurrentUserUrn(headers: HttpHeaders): String {
        try {
            // Ensure we have the LinkedIn-Version header
            if (!headers.containsKey("LinkedIn-Version")) {
                headers.set("LinkedIn-Version", "202302")
            }

            // Call the userinfo endpoint to get user information
            val userinfoUrl = "https://api.linkedin.com/v2/userinfo"
            logger.info("Making request to LinkedIn userinfo API: $userinfoUrl")
            logger.info("Headers: ${headers.toString()}")

            val responseEntity = getRestTemplate().exchange(
                userinfoUrl,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                String::class.java
            )

            logger.info("Response status: ${responseEntity.statusCode}")
            val response = responseEntity.body ?: ""
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

    @Throws(IOException::class)
    private fun loadProperty() {
        val inputStream: InputStream? = LinkedInOAuthController::class.java.classLoader.getResourceAsStream(propFileName)
        if (inputStream != null) {
            prop.load(inputStream)
        } else {
            throw FileNotFoundException("property file '$propFileName' not found in the classpath")
        }
    }
}
