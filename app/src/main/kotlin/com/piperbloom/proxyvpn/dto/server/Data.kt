package com.piperbloom.proxyvpn.dto.server

data class Data(
    val _id: String,
    val geoLocation: String,
    val hostname: String,
    val serverAddress: String,
    val vlessServers: String
)