package com.linkedIn.api

/**
 * Constants for use
 */
object ClientConstants {
    const val TOKEN_INTROSPECTION_ENDPOINT = "tokenIntrospection"
    const val THREE_LEGGED_TOKEN_GEN_ENDPOINT = "login"
    const val TWO_LEGGED_TOKEN_GEN_ENDPOINT = "twoLeggedAuth"
    const val USE_REFRESH_TOKEN_ENDPOINT = "refreshToken"
    const val GET_TOKEN_ENDPOINT = "getToken"

    const val PROFILE_ENDPOINT = "profile/info"
    const val MEMBER_CONNECTIONS_ENDPOINT = "memberConnections"
    const val CREATE_POST_ENDPOINT = "posts/create"
    const val GET_PERSON_URN_ENDPOINT = "profile/person-urn"
    const val GET_ORGANIZATION_URNS_ENDPOINT = "profile/organization-urns"
    const val OAUTH_PAGE = "index"
    const val GENERIC_ERROR_MESSAGE = "Error retrieving the data"
    const val TWO_LEGGED_TOKEN_GEN_SUCCESS_MESSAGE = "2-Legged OAuth token successfully generated via client credentials."

    const val MEMBER_CONNECTIONS_MESSAGE = "Member Connections:- "
    const val PERSON_URN_MESSAGE = "Person URN:- "
    const val ORGANIZATION_URNS_MESSAGE = "Organization URNs:- "
    const val TOKEN_EXISTS_MESSAGE = "Access Token is ready to use!"
    const val ACTION_2_LEGGED_TOKEN_GEN = "Generating 2-legged auth access token..."
    const val ACTION_GET_PROFILE = "Getting public profile..."
    const val ACTION_USE_REFRESH_TOKEN = "Refreshing token..."
    const val ACTION_TOKEN_INTROSPECTION = "Performing token introspection..."
    const val ACTION_GET_MEMBER_CONNECTIONS = "Getting member connections..."
    const val ACTION_CREATE_POST = "Creating LinkedIn post..."
    const val ACTION_GET_PERSON_URN = "Getting person URN..."
    const val ACTION_GET_ORGANIZATION_URNS = "Getting organization URNs..."
    const val CASE_TWO_LEGGED_TOKEN_GEN = "two_legged_auth=2+Legged+OAuth"
    const val CASE_GET_PROFILE = "profile=Get+Profile"
    const val CASE_USE_REFRESH_TOKEN = "refresh_token=Use+Refresh+Token"
    const val CASE_TOKEN_INTROSPECTION = "token_introspection=Token+Introspection"
    const val CASE_GET_MEMBER_CONNECTIONS = "member_connections=Get+Member+Connections"
    const val CASE_CREATE_POST = "create_post=Create+Post"
    const val CASE_GET_PERSON_URN = "get_person_urn=Get+Person+URN"
    const val CASE_GET_ORGANIZATION_URNS = "get_organization_urns=Get+Organization+URNs"

    const val DEFAULT_MESSAGE = "No API calls made!"
    const val REFRESH_TOKEN_ERROR_MESSAGE = "Refresh token is empty! Generate 3L Access token again."
    const val REFRESH_TOKEN_MESSAGE = "Generated new access token using refresh token."
    const val CREATE_POST_SUCCESS_MESSAGE = "Successfully created LinkedIn post."
}
