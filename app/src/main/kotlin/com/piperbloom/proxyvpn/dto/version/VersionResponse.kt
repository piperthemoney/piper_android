package com.piperbloom.proxyvpn.dto.version

data class VersionResponse(
    val code: Int,
    val `data`: List<VersionData>,
    val message: String,
    val status: String
)