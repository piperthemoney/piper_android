package com.piperbloom.proxyvpn.dto.login

data class User(
    val batch: String,
    val codeEntry: CodeEntry,
    val lifespan: String
)