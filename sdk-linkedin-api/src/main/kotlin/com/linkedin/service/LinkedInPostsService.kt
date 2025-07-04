package com.linkedin.service

import com.example.server.common.AccessToken

interface LinkedInPostsService {
    fun createPost(token: AccessToken, content: String?): PostCreationResponse
}
