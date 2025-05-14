package com.linkedin.oauth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.linkedin.oauth.builder.AuthorizationUrlBuilder
import com.linkedin.oauth.builder.ScopeBuilder
import com.linkedin.oauth.pojo.AccessToken
import com.linkedin.oauth.util.Constants.CLIENT_ID
import com.linkedin.oauth.util.Constants.CLIENT_SECRET
import com.linkedin.oauth.util.Constants.CODE
import com.linkedin.oauth.util.Constants.GRANT_TYPE
import com.linkedin.oauth.util.Constants.GrantType
import com.linkedin.oauth.util.Constants.REDIRECT_URI
import com.linkedin.oauth.util.Constants.REFRESH_TOKEN
import com.linkedin.oauth.util.Constants.TOKEN
import com.linkedin.oauth.util.Preconditions
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.IOException

/**
 * LinkedIn 3-Legged OAuth Service
 */
class LinkedInOAuthService private constructor(oauthServiceBuilder: LinkedInOAuthServiceBuilder) {
    val redirectUri: String = oauthServiceBuilder.redirectUri
    val apiKey: String = oauthServiceBuilder.apiKey
    val apiSecret: String = oauthServiceBuilder.apiSecret
    val scope: String = oauthServiceBuilder.scope

    /**
     * @return an instance of [AuthorizationUrlBuilder]
     */
    fun createAuthorizationUrlBuilder(): AuthorizationUrlBuilder {
        return AuthorizationUrlBuilder(this)
    }

    /**
     * @param code authorization code
     * @return response of LinkedIn's 3-legged token flow captured in a POJO [AccessToken]
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getAccessToken3Legged(code: String): HttpEntity<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters.add(GRANT_TYPE, GrantType.AUTHORIZATION_CODE.grantType)
        parameters.add(CODE, code)
        parameters.add(REDIRECT_URI, this.redirectUri)
        parameters.add(CLIENT_ID, this.apiKey)
        parameters.add(CLIENT_SECRET, this.apiSecret)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        return HttpEntity(parameters, headers)
    }

    /**
     * @param refreshToken the refresh token obtained from the authorization code exchange
     * @return response of LinkedIn's refresh token flow captured in a POJO [AccessToken]
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getAccessTokenFromRefreshToken(refreshToken: String): HttpEntity<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters.add(GRANT_TYPE, GrantType.REFRESH_TOKEN.grantType)
        parameters.add(REFRESH_TOKEN, refreshToken)
        parameters.add(CLIENT_ID, this.apiKey)
        parameters.add(CLIENT_SECRET, this.apiSecret)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        return HttpEntity(parameters, headers)
    }

    /**
     * Get access token by LinkedIn's OAuth2.0 Client Credentials flow
     * @return JSON String response
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getAccessToken2Legged(): HttpEntity<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters.add(GRANT_TYPE, GrantType.CLIENT_CREDENTIALS.grantType)
        parameters.add(CLIENT_ID, this.apiKey)
        parameters.add(CLIENT_SECRET, this.apiSecret)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        return HttpEntity(parameters, headers)
    }

    /**
     * Introspect token using LinkedIn's Auth tokenIntrospect API
     * @param token String representation of the access token
     * @return JSON String response
     */
    @Throws(Exception::class)
    fun introspectToken(token: String?): HttpEntity<MultiValueMap<String, String>> {
        val parameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        parameters.add(CLIENT_ID, this.apiKey)
        parameters.add(CLIENT_SECRET, this.apiSecret)
        parameters.add(TOKEN, token)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set(HttpHeaders.USER_AGENT, "java-sample-application (version 1.0, OAuth)")
        return HttpEntity(parameters, headers)
    }

    /**
     * Method to convert JSON String OAuth Token to POJO
     * @param accessToken
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun convertJsonTokenToPojo(accessToken: String): AccessToken {
        return ObjectMapper().readValue(accessToken, AccessToken::class.java)
    }

    /**
     * Builder class for LinkedIn's OAuth Service
     */
    class LinkedInOAuthServiceBuilder {
        lateinit var redirectUri: String
        lateinit var apiKey: String
        lateinit var apiSecret: String
        lateinit var scope: String

        fun apiKey(apiKey: String): LinkedInOAuthServiceBuilder {
            Preconditions.checkEmptyString(apiKey, "Invalid Api key")
            this.apiKey = apiKey
            return this
        }

        fun apiSecret(apiSecret: String): LinkedInOAuthServiceBuilder {
            Preconditions.checkEmptyString(apiSecret, "Invalid Api secret")
            this.apiSecret = apiSecret
            return this
        }

        fun callback(callback: String): LinkedInOAuthServiceBuilder {
            this.redirectUri = callback
            return this
        }

        private fun setScope(scope: String): LinkedInOAuthServiceBuilder {
            Preconditions.checkEmptyString(scope, "Invalid OAuth scope")
            this.scope = scope
            return this
        }

        fun defaultScope(scopeBuilder: ScopeBuilder): LinkedInOAuthServiceBuilder {
            return setScope(scopeBuilder.build())
        }

        fun defaultScope(scope: String): LinkedInOAuthServiceBuilder {
            return setScope(scope)
        }

        fun build(): LinkedInOAuthService {
            return LinkedInOAuthService(this)
        }
    }
}
