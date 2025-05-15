package com.linkedIn.api

import com.linkedIn.api.ClientConstants.CASE_FIND_AD_ACCOUNTS
import com.linkedIn.api.ClientConstants.CASE_GET_USER_ORG_ROLES
import com.linkedIn.api.ClientConstants.CASE_TOKEN_INTROSPECTION
import com.linkedIn.api.ClientConstants.DEFAULT_MESSAGE
import com.linkedIn.api.ClientConstants.FIND_AD_ACCOUNTS_ENDPOINT
import com.linkedIn.api.ClientConstants.FIND_AD_ACCOUNTS_MESSAGE
import com.linkedIn.api.ClientConstants.FIND_USER_ROLES_MESSAGE
import com.linkedIn.api.ClientConstants.GENERIC_ERROR_MESSAGE
import com.linkedIn.api.ClientConstants.GET_USER_ORG_ACCESS_ENDPOINT
import com.linkedIn.api.ClientConstants.LMS_PAGE
import com.linkedIn.api.ClientConstants.THREE_LEGGED_TOKEN_GEN_ENDPOINT
import com.linkedIn.api.ClientConstants.TOKEN_EXISTS_MESSAGE
import com.linkedIn.api.ClientConstants.TOKEN_INTROSPECTION_ENDPOINT
import com.linkedIn.api.ClientConstants.GET_TOKEN_ENDPOINT
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Map
import java.util.logging.Logger
import java.util.logging.Level

/**
 * LMS controller for handling the actions on the marketing page at
 * http://localhost:8989/marketing (Default)
 */
@Controller
class LinkedInMarketingController {

    @Value("\${SERVER_URL}")
    private lateinit var SERVER_URL: String

    private val logger = Logger.getLogger(LinkedInMarketingController::class.java.name)
    private val lmsTemplate = RestTemplate()
    private val mapper = ObjectMapper()

    /**
     * Serves a html webpage with operations related to LinkedIn Marketing Solutions
     *
     * @param model Spring Boot Model
     * @return the html page to render
     */
    @GetMapping("/marketing")
    fun marketing(model: Model): String {
        var action = "Start with LinkedIn's Marketing API operations..."
        var response = ""
        var output = ""
        var token = ""
        try {
            response = lmsTemplate.getForObject(SERVER_URL + TOKEN_INTROSPECTION_ENDPOINT, String::class.java)!!
            logger.log(Level.INFO, "Validating if a token is already in session. Response from token introspection end point is: {0}", response)

            if (!response.lowercase().contains("error")) {
                action = TOKEN_EXISTS_MESSAGE
                output = TOKEN_EXISTS_MESSAGE

                // Get the token if it exists
                try {
                    token = lmsTemplate.getForObject(SERVER_URL + GET_TOKEN_ENDPOINT, String::class.java) ?: ""
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

        logger.log(Level.INFO, "Completed execution for rendering Marketing page. The model values are output: {0}, action: {1}, token: {2}.", arrayOf(output, action, token))
        return LMS_PAGE
    }

    /**
     * Handles the post requests of Html page, calls the API endpoints of server URL.
     *
     * @param data string data passed from the UI component
     * @param model Spring Boot Model
     * @return a page to render on UI
     */
    @PostMapping(path = ["/marketing"], produces = ["application/json", "application/xml"], consumes = ["application/x-www-form-urlencoded"])
    fun postBody(@RequestBody data: String, model: Model): String {
        var response = ""
        var action = ""
        var token = ""
        var shouldFetchToken = false

        logger.log(Level.INFO, "Handling on click of marketing page buttons. Button clicked is {0}", data)

        when (data) {
            CASE_TOKEN_INTROSPECTION -> {
                try {
                    response = lmsTemplate.getForObject(SERVER_URL + TOKEN_INTROSPECTION_ENDPOINT, String::class.java)!!
                    shouldFetchToken = true
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
            CASE_FIND_AD_ACCOUNTS -> {
                try {
                    response = lmsTemplate.getForObject(SERVER_URL + FIND_AD_ACCOUNTS_ENDPOINT, String::class.java)!!
                    val map = mapper.readValue(response, object : TypeReference<Map<String, Any>>() {})
                    if (map.containsKey("elements")) {
                        response = FIND_AD_ACCOUNTS_MESSAGE + response
                    }
                    shouldFetchToken = true
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
            CASE_GET_USER_ORG_ROLES -> {
                try {
                    response = lmsTemplate.getForObject(SERVER_URL + GET_USER_ORG_ACCESS_ENDPOINT, String::class.java)!!
                    val map = mapper.readValue(response, object : TypeReference<Map<String, Any>>() {})
                    if (map.containsKey("elements")) {
                        response = FIND_USER_ROLES_MESSAGE + response
                    }
                    shouldFetchToken = true
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, e.message, e)
                    response = GENERIC_ERROR_MESSAGE
                }
            }
            else -> {
                response = DEFAULT_MESSAGE
            }
        }

        // Fetch the token if needed
        if (shouldFetchToken) {
            try {
                token = lmsTemplate.getForObject(SERVER_URL + GET_TOKEN_ENDPOINT, String::class.java) ?: ""
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
        return LMS_PAGE
    }
}
