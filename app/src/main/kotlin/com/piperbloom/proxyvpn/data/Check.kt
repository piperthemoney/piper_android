package com.piperbloom.proxyvpn.data

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
fun calculateExpirationDays(activationDateStr: String, lifespan: String): Long {
    // Parse the activation date
    val dateFormat = DateTimeFormatter.ISO_DATE_TIME
    val activationDate = LocalDateTime.parse(activationDateStr, dateFormat)

    // Determine lifespan in months
    val lifespanMonths = when (lifespan) {
        "1month" -> 1
        "2months" -> 2
        "3months" -> 3
        "4months" -> 4
        "5months" -> 5
        "6months" -> 6
        "7months" -> 7
        "8months" -> 8
        "9months" -> 9
        "10months" -> 10
        "11months" -> 11
        "12months" -> 12
        else -> 0
    }

    // Calculate the expiration date
    val expirationDate = activationDate.plusMonths(lifespanMonths.toLong())

    // Get the current date
    val currentDate = LocalDateTime.now(ZoneId.of("UTC"))

    // Calculate the difference in days
    return ChronoUnit.DAYS.between(currentDate, expirationDate)
}

fun areVlessServersEqual(conf: String, serverUri: String): Boolean {
    val uriConf = Uri.parse(conf)
    val uriServer = Uri.parse(serverUri)

    // Compare key components (IP address, encryption, security, path, etc.)
    return (uriConf.host == uriServer.host)
}