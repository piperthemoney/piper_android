package com.piperbloom.proxyvpn.service

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

object InternetSpeedTest {

    private val client = OkHttpClient()

    fun downloadSpeedTest(callback: (speed: Double) -> Unit) {
        val url = "https://speed.hetzner.de/100MB.bin" // A large file for testing download speed
        val request = Request.Builder().url(url).build()
        val startTime = System.currentTimeMillis()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val endTime = System.currentTimeMillis()
                val timeTaken = endTime - startTime
                val contentLength = response.body?.contentLength() ?: 0L
                val speed = contentLength / (timeTaken / 1000.0) // bytes per second
                callback(speed)
            }
        })
    }

    fun uploadSpeedTest(callback: (speed: Double) -> Unit) {
        val url = "https://httpbin.org/post" // A public service for testing upload
        val data = ByteArray(1024 * 1024) // 1 MB of data
        val requestBody = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), data)
        val request = Request.Builder().url(url).post(requestBody).build()
        val startTime = System.currentTimeMillis()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val endTime = System.currentTimeMillis()
                val timeTaken = endTime - startTime
                val contentLength = data.size.toLong()
                val speed = contentLength / (timeTaken / 1000.0) // bytes per second
                callback(speed)
            }
        })
    }
}