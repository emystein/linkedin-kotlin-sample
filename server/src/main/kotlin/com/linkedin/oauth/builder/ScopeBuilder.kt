package com.linkedin.oauth.builder

/**
 * Builder for LinkedIn OAuth 2.0 scopes.
 */
class ScopeBuilder {
    private val scopes = HashSet<String>()

    /**
     * public constructor
     * @param scope that needs to be requested
     */
    constructor(scope: String) {
        withScope(scope)
    }

    /**
     * public constructor
     * @param scopes array of scopes to be requested
     */
    constructor(vararg scopes: String) {
        withScopes(*scopes)
    }

    /**
     * public constructor
     * @param scopes collection of scopes to be requested
     */
    constructor(scopes: Collection<String>) {
        withScopes(scopes)
    }

    /**
     * Default constructor
     */
    constructor()

    /**
     * Setter for a single scope
     */
    fun withScope(scope: String): ScopeBuilder {
        scopes.add(scope)
        return this
    }

    /**
     * Setter for an array of scopes
     */
    fun withScopes(vararg scopes: String): ScopeBuilder {
        this.scopes.addAll(scopes)
        return this
    }

    /**
     * Setter for setting a collection of scopes
     */
    fun withScopes(scopes: Collection<String>): ScopeBuilder {
        this.scopes.addAll(scopes)
        return this
    }

    /**
     * builds all the scopes set into a single String for requesting
     */
    fun build(): String {
        val scopeBuilder = StringBuilder()
        for (scope in scopes) {
            scopeBuilder.append(' ').append(scope)
        }
        return scopeBuilder.substring(1)
    }
}
