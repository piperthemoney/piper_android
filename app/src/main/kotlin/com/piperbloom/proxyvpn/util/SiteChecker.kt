package com.piperbloom.proxyvpn.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SiteChecker {

    // Make isSiteActive a suspending function
    suspend fun isSiteActive(urlString: String): Boolean {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 3000 // 5 seconds timeout
                connection.readTimeout = 3000
                connection.connect()
                // If we get a response, consider it active
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                connection?.disconnect()
            }
        }
    }

    suspend fun getFirstActiveUrl(urls: List<String>): String? {
        return withContext(Dispatchers.IO) {
            urls.forEach { url ->
                if (isSiteActive(url)) return@withContext url
            }
            null // No active URL found
        }
    }
}
