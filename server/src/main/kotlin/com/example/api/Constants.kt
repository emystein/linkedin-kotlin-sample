package com.example.api

/**
 * Constants file
 */
object ServerConstants {

    const val LI_ME_ENDPOINT = "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))&oauth2_access_token="
    const val TOKEN_INTROSPECTION_ERROR_MESSAGE = "Error introspecting token, service is not initiated"
    const val SAMPLE_APP_BASE = "java-sample-application"
    const val SAMPLE_APP_VERSION = "version 1.0"

    enum class AppName {
        OAuth
    }

    val USER_AGENT_OAUTH_VALUE = String.format("%s (%s, %s)", SAMPLE_APP_BASE, SAMPLE_APP_VERSION, AppName.OAuth.name)
}
