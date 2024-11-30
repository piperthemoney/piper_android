package com.piperbloom.proxyvpn.service

import android.util.Log
import com.piperbloom.proxyvpn.BuildConfig
import com.piperbloom.proxyvpn.BuildConfig.BASE_URL1
import com.piperbloom.proxyvpn.BuildConfig.BASE_URL2
import com.piperbloom.proxyvpn.BuildConfig.BASE_URL3
import com.piperbloom.proxyvpn.util.SiteChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var activeBaseUrl: String? = null
    private var retrofitInstance: Retrofit? = null
    private var initializationDeferred: Deferred<Unit>? = null

    // Suspend function to initialize Retrofit
    suspend fun initialize() {
        if (initializationDeferred == null) {
            initializationDeferred = CoroutineScope(Dispatchers.IO).async {
                val urls = listOf(BASE_URL1, BASE_URL2, BASE_URL3)
                // TODO: Fetch url based on connection status
                activeBaseUrl = SiteChecker().getFirstActiveUrl(urls) ?: BASE_URL1
                // Temporary fix to use currently active url [BASE_URL_2]
                activeBaseUrl = BASE_URL1
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(activeBaseUrl!!)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                Log.d("SITE_STATE", activeBaseUrl!!)
            }
        }
        initializationDeferred?.await() // Wait for completion if already initializing
    }

    // Provide the ApiService
    val apiService: ApiService
        get() = retrofitInstance?.create(ApiService::class.java)
            ?: throw UninitializedPropertyAccessException("RetrofitClient must be initialized")

    suspend fun awaitInitialization() {
        initializationDeferred?.await()
    }
}