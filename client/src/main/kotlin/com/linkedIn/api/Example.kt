package com.linkedIn.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Create Spring Boot Application and set a default controller
 */

@SpringBootApplication
class Example {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Example>(*args)
        }
    }
}
