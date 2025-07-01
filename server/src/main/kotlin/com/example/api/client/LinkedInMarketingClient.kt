package com.example.api.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "linkedin-marketing-client",
    url = "https://api.linkedin.com",
    configuration = [com.example.api.config.FeignConfig::class]
)
interface LinkedInMarketingClient {

    @GetMapping("/v2/adAccountUsersV2")
    fun getAdAccounts(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam("q") q: String = "authenticatedUser",
        @RequestParam("oauth2_access_token") accessToken: String
    ): String

    @GetMapping("/v2/organizationAcls")
    fun getOrganizationAcls(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam("q") q: String = "roleAssignee",
        @RequestParam("oauth2_access_token") accessToken: String
    ): String
}
