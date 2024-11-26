package com.piperbloom.proxyvpn.dto.support

data class SupportResponse(
    val code: Int,
    val `data`: List<SupportData>,
    val message: String,
    val status: String
)