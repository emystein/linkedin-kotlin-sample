package com.linkedin.oauth.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * POJO to encapsulate access token from 2/3-legged LinkedIn OAuth 2.0 flow.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class AccessToken {
    @JsonProperty(value = "access_token")
    var accessToken: String? = null

    @JsonProperty(value = "expires_in")
    var expiresIn: String? = null

    @JsonProperty(value = "refresh_token")
    var refreshToken: String? = null

    @JsonProperty(value = "refresh_token_expires_in")
    var refreshTokenExpiresIn: String? = null
}
