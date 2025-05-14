package com.linkedin.oauth.builder

import com.linkedin.oauth.service.LinkedInOAuthService
import com.linkedin.oauth.util.Constants.AUTHORIZE_URL
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Builder class for LinkedIn OAuth 2.0 authorization URL.
 */
class AuthorizationUrlBuilder(private val oauth20Service: LinkedInOAuthService) {
    private var state: String? = null
    private var additionalParams: Map<String, String>? = null

    /**
     * Setter for state
     */
    fun state(state: String): AuthorizationUrlBuilder {
        this.state = state
        return this
    }

    /**
     * Setter for additional params
     */
    fun additionalParams(additionalParams: Map<String, String>): AuthorizationUrlBuilder {
        this.additionalParams = additionalParams
        return this
    }

    /**
     * Builds the authorization URL
     */
    @Throws(UnsupportedEncodingException::class)
    fun build(): String {
        val authorizationUrl = AUTHORIZE_URL +
                "?response_type=code&client_id=" +
                oauth20Service.apiKey +
                "&redirect_uri=" +
                oauth20Service.redirectUri +
                "&state=" +
                state +
                "&scope=" +
                URLEncoder.encode(oauth20Service.scope, StandardCharsets.UTF_8.toString())
        return authorizationUrl
    }
}
