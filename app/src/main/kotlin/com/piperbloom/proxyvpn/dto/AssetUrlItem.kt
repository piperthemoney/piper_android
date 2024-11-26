package com.piperbloom.proxyvpn.dto

data class AssetUrlItem(
    var remarks: String = "",
    var url: String = "",
    val addedTime: Long = System.currentTimeMillis(),
    var lastUpdated: Long = -1
)