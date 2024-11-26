package com.piperbloom.proxyvpn.dto.social

data class SocialMediaResponse(
    val code: Int,
    val `data`: List<SocialMediaData>,
    val message: String,
    val status: String
)