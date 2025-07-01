package com.linkedin.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "linkedin-profile-client",
    url = "https://api.linkedin.com",
    configuration = [com.linkedin.api.config.FeignConfig::class]
)
interface LinkedInProfileClient {

    @GetMapping("/v2/userinfo")
    fun getUserInfo(
        @RequestHeader("Authorization") authorization: String
    ): String

    @GetMapping("/v2/organizationAcls")
    fun getOrganizationAccess(
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("LinkedIn-Version") linkedInVersion: String = "202312",
        @org.springframework.web.bind.annotation.RequestParam("q") q: String = "roleAssignee",
        @org.springframework.web.bind.annotation.RequestParam("role") role: String = "ADMINISTRATOR",
        @org.springframework.web.bind.annotation.RequestParam("projection") projection: String = "(elements*(organization~(id,localizedName)))"
    ): String
}
