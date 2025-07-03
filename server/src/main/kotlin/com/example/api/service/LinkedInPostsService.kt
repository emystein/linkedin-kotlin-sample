package com.example.api.service

import com.example.api.dto.PostCreationResponse
import com.example.server.common.AccessToken

interface LinkedInPostsService {
    fun createPost(token: AccessToken, content: String?): PostCreationResponse
}
