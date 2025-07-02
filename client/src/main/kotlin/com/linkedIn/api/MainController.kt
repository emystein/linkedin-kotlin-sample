package com.linkedIn.api

import com.linkedIn.api.ClientConstants.ACTION_2_LEGGED_TOKEN_GEN
import com.linkedIn.api.ClientConstants.ACTION_CREATE_POST
import com.linkedIn.api.ClientConstants.ACTION_GET_MEMBER_CONNECTIONS
import com.linkedIn.api.ClientConstants.ACTION_GET_ORGANIZATION_URNS
import com.linkedIn.api.ClientConstants.ACTION_GET_PERSON_URN
import com.linkedIn.api.ClientConstants.ACTION_GET_PROFILE
import com.linkedIn.api.ClientConstants.ACTION_TOKEN_INTROSPECTION
import com.linkedIn.api.ClientConstants.ACTION_USE_REFRESH_TOKEN
import com.linkedIn.api.ClientConstants.CREATE_POST_ENDPOINT
import com.linkedIn.api.ClientConstants.CREATE_POST_SUCCESS_MESSAGE
import com.linkedIn.api.ClientConstants.GENERIC_ERROR_MESSAGE
import com.linkedIn.api.ClientConstants.GET_ORGANIZATION_URNS_ENDPOINT
import com.linkedIn.api.ClientConstants.GET_PERSON_URN_ENDPOINT
import com.linkedIn.api.ClientConstants.GET_TOKEN_ENDPOINT
import com.linkedIn.api.ClientConstants.MEMBER_CONNECTIONS_ENDPOINT
import com.linkedIn.api.ClientConstants.MEMBER_CONNECTIONS_MESSAGE
import com.linkedIn.api.ClientConstants.OAUTH_PAGE
import com.linkedIn.api.ClientConstants.ORGANIZATION_URNS_MESSAGE
import com.linkedIn.api.ClientConstants.PERSON_URN_MESSAGE
import com.linkedIn.api.ClientConstants.PROFILE_ENDPOINT
import com.linkedIn.api.ClientConstants.REFRESH_TOKEN_ERROR_MESSAGE
import com.linkedIn.api.ClientConstants.REFRESH_TOKEN_MESSAGE
import com.linkedIn.api.ClientConstants.THREE_LEGGED_TOKEN_GEN_ENDPOINT
import com.linkedIn.api.ClientConstants.TOKEN_EXISTS_MESSAGE
import com.linkedIn.api.ClientConstants.TOKEN_INTROSPECTION_ENDPOINT
import com.linkedIn.api.ClientConstants.TWO_LEGGED_TOKEN_GEN_ENDPOINT
import com.linkedIn.api.ClientConstants.TWO_LEGGED_TOKEN_GEN_SUCCESS_MESSAGE
import com.linkedIn.api.ClientConstants.USE_REFRESH_TOKEN_ENDPOINT
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Main controller called by spring-boot to handle OAuth actions at
 * http://localhost:8989 (Default)
 */

@Controller
class MainController {

    @Bean
    fun Rest_Template(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }

    private val logger = KotlinLogging.logger {}

    @Value("\${SERVER_URL}")
    private lateinit var SERVER_URL: String

    /**
     * Serves a html webpage with operations related to OAuth
     *
     * @param model Spring Boot Model
     * @return the html page to render
     */
    @GetMapping("/")
    fun oauth(model: Model): String {
        var action = "Start with LinkedIn's OAuth API operations..."
        var response = ""
        var output = ""
        var token = ""
        try {
            response = restTemplate.getForObject(SERVER_URL + TOKEN_INTROSPECTION_ENDPOINT, String::class.java)!!
            logger.info { "Validating if a token is already in session. Response from token introspection end point is: $response" }

            if (!response.lowercase().contains("error")) {
                action = TOKEN_EXISTS_MESSAGE
                output = TOKEN_EXISTS_MESSAGE

                // Get the token if it exists
                try {
                    token = restTemplate.getForObject(SERVER_URL + GET_TOKEN_ENDPOINT, String::class.java) ?: ""
                    logger.info { "Retrieved token: $token" }
                } catch (e: Exception) {
                    logger.error(e) { "Error retrieving token: ${e.message}" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { e.message }
        }

        model.addAttribute("auth_url", SERVER_URL + THREE_LEGGED_TOKEN_GEN_ENDPOINT)
        model.addAttribute("output", output)
        model.addAttribute("action", action)
        model.addAttribute("token", token)

        logger.info { "Completed execution for rendering OAuth page. The model values are output: $output, action: $action, token: $token." }
        return OAUTH_PAGE
    }

    /**
     * Handles the post requests of Html page, calls the API endpoints of server URL.
     *
     * @param data string data passed from the UI compoment
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    /**
     * Helper method to prepare the model with common attributes and fetch token if needed
     *
     * @param model The Spring model to populate
     * @param response The response to display
     * @param action The action being performed
     * @param shouldFetchToken Whether to fetch the token
     * @return The token value
     */
    private fun prepareModel(model: Model, response: String, action: String, shouldFetchToken: Boolean): String {
        var token = ""

        // Fetch the token if needed
        if (shouldFetchToken) {
            try {
                token = restTemplate.getForObject(SERVER_URL + GET_TOKEN_ENDPOINT, String::class.java) ?: ""
                logger.info { "Retrieved token: $token" }
            } catch (e: Exception) {
                logger.error(e) { "Error retrieving token: ${e.message}" }
            }
        }

        model.addAttribute("output", response)
        model.addAttribute("auth_url", SERVER_URL + THREE_LEGGED_TOKEN_GEN_ENDPOINT)
        model.addAttribute("action", action)
        model.addAttribute("token", token)

        logger.info { "Completed execution. The output is $response, token: $token" }
        return token
    }

    /**
     * Handles the 2-legged OAuth token generation
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/twoLeggedAuth")
    fun handleTwoLeggedAuth(model: Model): String {
        logger.info { "Handling 2-legged OAuth token generation" }

        var response = ""
        val action = ACTION_2_LEGGED_TOKEN_GEN

        try {
            restTemplate.getForObject(SERVER_URL + TWO_LEGGED_TOKEN_GEN_ENDPOINT, String::class.java)
            response = TWO_LEGGED_TOKEN_GEN_SUCCESS_MESSAGE
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    /**
     * Handles the profile retrieval
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/profile")
    fun handleGetProfile(model: Model): String {
        logger.info { "Handling profile retrieval" }

        var response = ""
        val action = ACTION_GET_PROFILE

        try {
            response = restTemplate.getForObject(SERVER_URL + PROFILE_ENDPOINT, String::class.java)!!
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    /**
     * Handles the refresh token operation
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/refreshToken")
    fun handleRefreshToken(model: Model): String {
        logger.info { "Handling refresh token operation" }

        var response = ""
        val action = ACTION_USE_REFRESH_TOKEN
        var shouldFetchToken = false

        try {
            val tempResponse = restTemplate.getForObject(SERVER_URL + USE_REFRESH_TOKEN_ENDPOINT, String::class.java)
            response = if (tempResponse == null) {
                REFRESH_TOKEN_ERROR_MESSAGE
            } else {
                shouldFetchToken = true
                REFRESH_TOKEN_MESSAGE
            }
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, shouldFetchToken)
        return OAUTH_PAGE
    }

    /**
     * Handles the member connections retrieval
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/memberConnections")
    fun handleGetMemberConnections(model: Model): String {
        logger.info { "Handling member connections retrieval" }

        var response = ""
        val action = ACTION_GET_MEMBER_CONNECTIONS

        try {
            response = restTemplate.getForObject(SERVER_URL + MEMBER_CONNECTIONS_ENDPOINT, String::class.java)!!
            response = MEMBER_CONNECTIONS_MESSAGE + response
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    /**
     * Handles the post creation
     *
     * @param postContent The content of the post
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/createPost")
    fun handleCreatePost(@RequestParam("post_content", required = false) postContent: String?, model: Model): String {
        logger.info { "Handling post creation with content: $postContent" }

        var response = ""
        val action = ACTION_CREATE_POST

        try {
            if (!postContent.isNullOrBlank()) {
                // Create a request with the post content
                val createPostUrl = "$SERVER_URL$CREATE_POST_ENDPOINT?content=${java.net.URLEncoder.encode(postContent, "UTF-8")}"
                response = restTemplate.getForObject(createPostUrl, String::class.java)!!
                response = CREATE_POST_SUCCESS_MESSAGE + "\n" + response
            } else {
                response = "Error: Post content cannot be empty."
            }
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    /**
     * Handles the person URN retrieval
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/getPersonUrn")
    fun handleGetPersonUrn(model: Model): String {
        logger.info { "Handling person URN retrieval" }

        var response = ""
        val action = ACTION_GET_PERSON_URN

        try {
            response = restTemplate.getForObject(SERVER_URL + GET_PERSON_URN_ENDPOINT, String::class.java)!!
            response = PERSON_URN_MESSAGE + response
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    /**
     * Handles the organization URNs retrieval
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/getOrganizationUrns")
    fun handleGetOrganizationUrns(model: Model): String {
        logger.info { "Handling organization URNs retrieval" }

        var response = ""
        val action = ACTION_GET_ORGANIZATION_URNS

        try {
            response = restTemplate.getForObject(SERVER_URL + GET_ORGANIZATION_URNS_ENDPOINT, String::class.java)!!
            response = ORGANIZATION_URNS_MESSAGE + response
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    /**
     * Handles the token introspection
     *
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping("/tokenIntrospection")
    fun handleTokenIntrospection(model: Model): String {
        logger.info { "Handling token introspection" }

        var response = ""
        val action = ACTION_TOKEN_INTROSPECTION

        try {
            response = restTemplate.getForObject(SERVER_URL + TOKEN_INTROSPECTION_ENDPOINT, String::class.java)!!
        } catch (e: Exception) {
            logger.error(e) { e.message }
            response = GENERIC_ERROR_MESSAGE
        }

        prepareModel(model, response, action, true)
        return OAUTH_PAGE
    }

    companion object {
        val restTemplate = RestTemplate()
    }


}
