package com.linkedin.oauth.util

object Constants {
    const val AUTHORIZE_URL = "https://www.linkedin.com/oauth/v2/authorization"
    const val REQUEST_TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken"
    const val TOKEN = "token"
    const val CLIENT_SECRET = "client_secret"
    const val CLIENT_ID = "client_id"
    const val REFRESH_TOKEN = "refresh_token"
    const val CODE = "code"
    const val REDIRECT_URI = "redirect_uri"
    const val GRANT_TYPE = "grant_type"
    const val TOKEN_INTROSPECTION_URL = "https://www.linkedin.com/oauth/v2/introspectToken"
    const val RESPONSE_CODE = 200
    const val PORT = 8000

    enum class GrantType(val grantType: String) {
        CLIENT_CREDENTIALS("client_credentials"),
        AUTHORIZATION_CODE("authorization_code"),
        REFRESH_TOKEN("refresh_token")
    }
}
