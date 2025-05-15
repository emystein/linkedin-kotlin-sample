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
    const val FIND_AD_ACCOUNTS_ENDPOINT = "findAdAccounts"
    const val GET_USER_ORG_ACCESS_ENDPOINT = "getUserOrgAccess"
    const val PROFILE_ENDPOINT = "profile"
    const val OAUTH_PAGE = "index"
    const val LMS_PAGE = "marketingtemplate"
    const val GENERIC_ERROR_MESSAGE = "Error retrieving the data"
    const val TWO_LEGGED_TOKEN_GEN_SUCCESS_MESSAGE = "2-Legged OAuth token successfully generated via client credentials."
    const val FIND_AD_ACCOUNTS_MESSAGE = "Find Ad Accounts by Authenticated User:- "
    const val FIND_USER_ROLES_MESSAGE = "Find Ad Account roles of Authenticated User:- "
    const val TOKEN_EXISTS_MESSAGE = "Access Token is ready to use!"
    const val ACTION_2_LEGGED_TOKEN_GEN = "Generating 2-legged auth access token..."
    const val ACTION_GET_PROFILE = "Getting public profile..."
    const val ACTION_USE_REFRESH_TOKEN = "Refreshing token..."
    const val ACTION_TOKEN_INTROSPECTION = "Performing token introspection..."
    const val CASE_TWO_LEGGED_TOKEN_GEN = "two_legged_auth=2+Legged+OAuth"
    const val CASE_GET_PROFILE = "profile=Get+Profile"
    const val CASE_USE_REFRESH_TOKEN = "refresh_token=Use+Refresh+Token"
    const val CASE_TOKEN_INTROSPECTION = "token_introspection=Token+Introspection"
    const val CASE_FIND_AD_ACCOUNTS = "Find_ad_account=Find+Ad+Accounts"
    const val CASE_GET_USER_ORG_ROLES = "Get_user_org_access=Find+Org+Access"
    const val DEFAULT_MESSAGE = "No API calls made!"
    const val REFRESH_TOKEN_ERROR_MESSAGE = "Refresh token is empty! Generate 3L Access token again."
    const val REFRESH_TOKEN_MESSAGE = "Generated new access token using refresh token."
}
