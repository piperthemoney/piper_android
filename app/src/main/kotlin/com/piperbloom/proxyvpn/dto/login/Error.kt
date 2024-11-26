package com.piperbloom.proxyvpn.dto.login

data class Error(
    val isOperational: Boolean?,
    val status: String?,
    val statusCode: Int?
)