package com.piperbloom.proxyvpn.dto.login

data class CodeEntry(
    val _id: String,
    val activationDate: String,
    val code: String,
    val isActive: Boolean,
    val lastLogin: String
)