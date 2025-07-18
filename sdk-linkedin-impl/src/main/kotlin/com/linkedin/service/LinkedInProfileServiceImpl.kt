package com.linkedin.service

import com.example.server.common.AccessToken
import com.linkedin.api.client.LinkedInProfileClient
import com.linkedin.api.client.ProfileInfoResponse
import com.linkedin.api.client.OrganizationAccessResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LinkedInProfileServiceImpl : LinkedInProfileService {

    @Autowired
    private lateinit var linkedInProfileClient: LinkedInProfileClient

    private val logger = KotlinLogging.logger {}

    override fun getProfileInfo(token: AccessToken): ProfileInfoResponse {
        return linkedInProfileClient.getUserInfo("Bearer ${token.value}")
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
        return linkedInProfileClient.getOrganizationAccess("Bearer ${token.value}")
    }

    override fun getCurrentUserUrn(token: AccessToken): String {
        logger.info { "Making request to LinkedIn userinfo API" }
        val profileInfo = linkedInProfileClient.getUserInfo("Bearer ${token.value}")
        val sub = profileInfo.sub
        if (!sub.isNullOrEmpty()) {
            return "urn:li:person:$sub"
        } else {
            throw Exception("Could not extract 'sub' field from userinfo response")
        }
    }
}
