/*
 * Test cases for Kotlin Sample Application.
 */

package com.example.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestTemplate

@SpringBootTest
class MainApplicationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Test
    fun contextLoads() {
        assertThat(restTemplate).isNotNull()
    }
}
