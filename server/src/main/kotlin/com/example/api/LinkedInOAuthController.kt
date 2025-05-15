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
     * Make a Public profile request with LinkedIN API
     *
     * @return Public profile of user
     */
    @RequestMapping(value = ["/profile"])
    fun profile(): String {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        val endpoint = "https://api.linkedin.com/v2/userinfo?oauth2_access_token="
        return getRestTemplate().exchange(endpoint + token, HttpMethod.GET, HttpEntity<Any>(headers), String::class.java).body ?: ""
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
