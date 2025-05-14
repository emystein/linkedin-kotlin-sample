package com.linkedin.oauth.util

/**
 * Utils for checking preconditions and invariants
 */
object Preconditions {
    private const val DEFAULT_MESSAGE = "Received an invalid parameter"

    /**
     * Checks that an object is not null.
     *
     * @param object any object
     * @param errorMsg error message
     *
     * @throws IllegalArgumentException if the object is null
     */
    fun checkNotNull(`object`: Any?, errorMsg: String) {
        check(`object` != null, errorMsg)
    }

    /**
     * Checks that a string is not null or empty
     *
     * @param string any string
     * @param errorMsg error message
     *
     * @throws IllegalArgumentException if the string is null or empty
     */
    fun checkEmptyString(string: String, errorMsg: String) {
        check(hasText(string), errorMsg)
    }

    fun hasText(str: String): Boolean {
        return !str.trim().isEmpty()
    }

    private fun check(requirement: Boolean, error: String) {
        if (!requirement) {
            throw IllegalArgumentException(if (hasText(error)) error else DEFAULT_MESSAGE)
        }
    }
}
