package com.example.api.service

import com.example.server.common.AccessToken

interface LinkedInPostsService {
    fun createPost(token: AccessToken, content: String?): Any
}
