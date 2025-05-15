package com.linkedIn.api

import com.linkedIn.api.ClientConstants.ACTION_2_LEGGED_TOKEN_GEN
import com.linkedIn.api.ClientConstants.ACTION_GET_PROFILE
import com.linkedIn.api.ClientConstants.ACTION_TOKEN_INTROSPECTION
import com.linkedIn.api.ClientConstants.ACTION_USE_REFRESH_TOKEN
import com.linkedIn.api.ClientConstants.CASE_GET_PROFILE
import com.linkedIn.api.ClientConstants.CASE_TOKEN_INTROSPECTION
import com.linkedIn.api.ClientConstants.CASE_TWO_LEGGED_TOKEN_GEN
import com.linkedIn.api.ClientConstants.CASE_USE_REFRESH_TOKEN
import com.linkedIn.api.ClientConstants.DEFAULT_MESSAGE
import com.linkedIn.api.ClientConstants.FIND_AD_ACCOUNTS_MESSAGE
import com.linkedIn.api.ClientConstants.FIND_USER_ROLES_MESSAGE
import com.linkedIn.api.ClientConstants.GENERIC_ERROR_MESSAGE
import com.linkedIn.api.ClientConstants.OAUTH_PAGE
import com.linkedIn.api.ClientConstants.PROFILE_ENDPOINT
import com.linkedIn.api.ClientConstants.REFRESH_TOKEN_ERROR_MESSAGE
import com.linkedIn.api.ClientConstants.REFRESH_TOKEN_MESSAGE
import com.linkedIn.api.ClientConstants.THREE_LEGGED_TOKEN_GEN_ENDPOINT
import com.linkedIn.api.ClientConstants.TOKEN_EXISTS_MESSAGE
import com.linkedIn.api.ClientConstants.TOKEN_INTROSPECTION_ENDPOINT
import com.linkedIn.api.ClientConstants.TWO_LEGGED_TOKEN_GEN_ENDPOINT
import com.linkedIn.api.ClientConstants.TWO_LEGGED_TOKEN_GEN_SUCCESS_MESSAGE
import com.linkedIn.api.ClientConstants.USE_REFRESH_TOKEN_ENDPOINT
import com.linkedIn.api.ClientConstants.GET_TOKEN_ENDPOINT
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.client.RestTemplate
import java.util.logging.Level
import java.util.logging.Logger

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

    private val logger = Logger.getLogger(MainController::class.java.name)

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
            logger.log(Level.INFO, "Validating if a token is already in session. Response from token introspection end point is: {0}", response)

            if (!response.lowercase().contains("error")) {
                action = TOKEN_EXISTS_MESSAGE
                output = TOKEN_EXISTS_MESSAGE

                // Get the token if it exists
                try {
                    token = restTemplate.getForObject(SERVER_URL + GET_TOKEN_ENDPOINT, String::class.java) ?: ""
                    logger.log(Level.INFO, "Retrieved token: {0}", token)
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "Error retrieving token: {0}", e.message)
                }
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, e.message, e)
        }

        model.addAttribute("auth_url", SERVER_URL + THREE_LEGGED_TOKEN_GEN_ENDPOINT)
        model.addAttribute("output", output)
        model.addAttribute("action", action)
        model.addAttribute("token", token)

        logger.log(Level.INFO, "Completed execution for rendering OAuth page. The model values are output: {0}, action: {1}, token: {2}.", arrayOf(output, action, token))
        return OAUTH_PAGE
    }

    /**
     * Handles the post requests of Html page, calls the API endpoints of server URL.
     *
     * @param data string data passed from the UI compoment
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping(path = ["/"], produces = ["application/json", "application/xml"], consumes = ["application/x-www-form-urlencoded"])
    fun postBody(@RequestBody data: String, model: Model): String {
        var response = ""
        var action = ""
        var token = ""
        var shouldFetchToken = false

        logger.log(Level.INFO, "Handling on click of marketing page buttons. Button clicked is {0}", data)

        when (data) {
            CASE_TWO_LEGGED_TOKEN_GEN -> {
                action = ACTION_2_LEGGED_TOKEN_GEN
                try {
                    restTemplate.getForObject(SERVER_URL + TWO_LEGGED_TOKEN_GEN_ENDPOINT, String::class.java)
                    response = TWO_LEGGED_TOKEN_GEN_SUCCESS_MESSAGE
                    shouldFetchToken = true
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
            CASE_GET_PROFILE -> {
                action = ACTION_GET_PROFILE
                try {
                    response = restTemplate.getForObject(SERVER_URL + PROFILE_ENDPOINT, String::class.java)!!
                    shouldFetchToken = true
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
            CASE_USE_REFRESH_TOKEN -> {
                action = ACTION_USE_REFRESH_TOKEN
                try {
                    val tempResponse = restTemplate.getForObject(SERVER_URL + USE_REFRESH_TOKEN_ENDPOINT, String::class.java)
                    response = if (tempResponse == null) {
                        REFRESH_TOKEN_ERROR_MESSAGE
                    } else {
                        REFRESH_TOKEN_MESSAGE
                    }
                    if (tempResponse != null) {
                        shouldFetchToken = true
                    }
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
            else -> {
                action = ACTION_TOKEN_INTROSPECTION
                try {
                    response = restTemplate.getForObject(SERVER_URL + TOKEN_INTROSPECTION_ENDPOINT, String::class.java)!!
                    shouldFetchToken = true
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
        }

        // Fetch the token if needed
        if (shouldFetchToken) {
            try {
                token = restTemplate.getForObject(SERVER_URL + GET_TOKEN_ENDPOINT, String::class.java) ?: ""
                logger.log(Level.INFO, "Retrieved token: {0}", token)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Error retrieving token: {0}", e.message)
            }
        }

        model.addAttribute("output", response)
        model.addAttribute("auth_url", SERVER_URL + THREE_LEGGED_TOKEN_GEN_ENDPOINT)
        model.addAttribute("action", action)
        model.addAttribute("token", token)

        logger.log(Level.INFO, "Completed execution on button click. The output is {0}, token: {1}", arrayOf(response, token))
        return OAUTH_PAGE
    }

    companion object {
        val restTemplate = RestTemplate()
    }
}
