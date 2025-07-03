package com.example.api.service

import com.example.api.dto.ErrorResponse
import com.example.api.dto.OrganizationAccessResponse
import com.example.api.dto.PersonUrnResponse
import com.example.api.dto.ProfileInfoResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.linkedin.api.client.LinkedInProfileClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class LinkedInProfileServiceImpl : LinkedInProfileService {

    @Autowired
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    private val logger = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    override fun getProfileInfo(token: AccessToken): Any {
        try {
            val response = linkedInProfileClient.getUserInfo("Bearer ${token.value}")
            return objectMapper.readValue(response, ProfileInfoResponse::class.java)
        } catch (e: Exception) {
            logger.error(e) { "Error retrieving profile info" }
            return ErrorResponse("profile_error", "Failed to process profile data: ${e.message}")
        }
    }

    override fun getPersonUrn(token: AccessToken): Any {
        val profileResponse = getProfileInfo(token)

        if (profileResponse is ErrorResponse) {
            return profileResponse // Return the error response
        }

        try {
            val profileInfo = profileResponse as ProfileInfoResponse
            val sub = profileInfo.sub

            if (!sub.isNullOrEmpty()) {
                val personUrn = "urn:li:person:$sub"
                return PersonUrnResponse(personUrn = personUrn)
            } else {
                return ErrorResponse("missing_sub", "Could not extract 'sub' field from profile response")
            }
        } catch (e: Exception) {
            logger.error(e) { "Error extracting person URN" }
            return ErrorResponse("urn_extraction_error", "Failed to process profile data: ${e.message}")
        }
    }

    override fun getOrganizationUrns(token: AccessToken): Any {
        try {
            val response = linkedInProfileClient.getOrganizationAccess("Bearer ${token.value}")
            return objectMapper.readValue(response, OrganizationAccessResponse::class.java)
        } catch (e: Exception) {
            logger.error(e) { "Error retrieving organization access" }
            return ErrorResponse("organization_access_error", "Failed to retrieve organization access: ${e.message}")
        }
    }

    override fun getCurrentUserUrn(token: AccessToken): String {
        logger.info { "Making request to LinkedIn userinfo API" }
        val response = linkedInProfileClient.getUserInfo("Bearer ${token.value}")
        val profileInfo = objectMapper.readValue(response, ProfileInfoResponse::class.java)
        val sub = profileInfo.sub
        if (!sub.isNullOrEmpty()) {
            return "urn:li:person:$sub"
        } else {
            throw Exception("Could not extract 'sub' field from userinfo response")
        }
    }
}
