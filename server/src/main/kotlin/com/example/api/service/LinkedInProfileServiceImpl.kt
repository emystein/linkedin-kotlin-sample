package com.example.api.service

import com.example.api.dto.OrganizationAccessResponse
import com.example.api.dto.PersonUrnResponse
import com.example.api.dto.ProfileInfoResponse
import com.example.common.AccessToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.linkedin.api.client.LinkedInProfileClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LinkedInProfileServiceImpl : LinkedInProfileService {

    @Autowired
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    private val logger = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    override fun getProfileInfo(token: AccessToken): ProfileInfoResponse {
        val response = linkedInProfileClient.getUserInfo("Bearer ${token.value}")
        return objectMapper.readValue(response, ProfileInfoResponse::class.java)
    }

    override fun getPersonUrn(token: AccessToken): PersonUrnResponse {
        val profileResponse = getProfileInfo(token)
        val sub = profileResponse.sub
        if (!sub.isNullOrEmpty()) {
            val personUrn = "urn:li:person:$sub"
            return PersonUrnResponse(personUrn = personUrn)
        } else {
            throw Exception("Could not extract 'sub' field from profile response")
        }
    }

    override fun getOrganizationUrns(token: AccessToken): OrganizationAccessResponse {
        val response = linkedInProfileClient.getOrganizationAccess("Bearer ${token.value}")
        return objectMapper.readValue(response, OrganizationAccessResponse::class.java)
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
