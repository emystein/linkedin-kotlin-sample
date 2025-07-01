package com.linkedin.api.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@EnableFeignClients(basePackages = ["com.linkedin.api.client"])
@ComponentScan(basePackages = ["com.linkedin.api.config"])
class LinkedInApiClientAutoConfiguration
