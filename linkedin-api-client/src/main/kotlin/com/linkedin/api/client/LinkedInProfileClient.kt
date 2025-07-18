package com.linkedin.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "linkedin-profile-client",
    url = "https://api.linkedin.com",
    configuration = [com.linkedin.api.config.FeignConfig::class]
)
interface LinkedInProfileClient {

    @GetMapping("/v2/userinfo")
    fun getUserInfo(
        @RequestHeader("Authorization") authorization: String
    ): ProfileInfoResponse

    @GetMapping("/v2/organizationAcls")
    fun getOrganizationAccess(
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("LinkedIn-Version") linkedInVersion: String = "202312",
        @RequestParam("q") q: String = "roleAssignee",
        @RequestParam("role") role: String = "ADMINISTRATOR",
        @RequestParam("projection") projection: String = "(elements*(organization~(id,localizedName)))"
    ): OrganizationAccessResponse
}
