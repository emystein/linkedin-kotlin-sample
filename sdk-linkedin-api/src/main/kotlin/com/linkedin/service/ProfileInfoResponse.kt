package com.linkedin.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

/**
 * Custom deserializer for locale field that can handle both string and object formats
 */
class LocaleDeserializer : JsonDeserializer<String?>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
        val node: JsonNode = p.codec.readTree(p)

        return when {
            node.isTextual -> node.asText()
            node.isObject -> {
                // Handle object format like {"country": "US", "language": "en"}
                val language = node.get("language")?.asText() ?: "en"
                val country = node.get("country")?.asText() ?: "US"
                "$language-$country"
            }
            else -> null
        }
    }
}

/**
 * Data class representing the response from LinkedIn's userinfo API
 * Based on LinkedIn OpenID Connect userinfo specification
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfileInfoResponse(
    @JsonProperty("sub")
    val sub: String? = null,

    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("given_name")
    val givenName: String? = null,

    @JsonProperty("family_name")
    val familyName: String? = null,

    @JsonProperty("picture")
    val picture: String? = null,

    @JsonProperty("locale")
    @JsonDeserialize(using = LocaleDeserializer::class)
    val locale: String? = null,

    @JsonProperty("email")
    val email: String? = null,

    @JsonProperty("email_verified")
    val emailVerified: Boolean? = null
)
