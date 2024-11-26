package com.piperbloom.proxyvpn.dto.login

data class Login(
    val code: Int?,
    val `data`: Data?,
    val error: Error?,
    val message: String?,
    val stackTrace: String?,
    val status: String?,
    val token: String?
)