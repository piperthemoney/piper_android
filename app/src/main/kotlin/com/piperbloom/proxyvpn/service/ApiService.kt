package com.piperbloom.proxyvpn.service

import com.piperbloom.proxyvpn.BuildConfig.ACTIVATION_URL
import com.piperbloom.proxyvpn.BuildConfig.PRE_VERSION_URL
import com.piperbloom.proxyvpn.BuildConfig.SERVER_BATCH_URL
import com.piperbloom.proxyvpn.BuildConfig.SOCIAL_URL
import com.piperbloom.proxyvpn.BuildConfig.SUPPORT_URL
import com.piperbloom.proxyvpn.BuildConfig.VERSION_URL
import com.piperbloom.proxyvpn.dto.login.ActivationRequest
import com.piperbloom.proxyvpn.dto.login.Login
import com.piperbloom.proxyvpn.dto.server.Server
import com.piperbloom.proxyvpn.dto.social.SocialMediaResponse
import com.piperbloom.proxyvpn.dto.support.SupportResponse
import com.piperbloom.proxyvpn.dto.version.VersionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @POST(ACTIVATION_URL)
    @Headers("Content-Type: application/json")
    fun activateCode(
        @Body request: ActivationRequest
    ): Call<Login>

    @GET(SERVER_BATCH_URL)
    fun getServerBatch(
        @Header("Authorization") token: String
    ): Call<Server>

    @GET(SOCIAL_URL)
    fun getSocialMediaData(): Call<SocialMediaResponse>

    @GET(SUPPORT_URL)
    fun getSupportData(): Call<SupportResponse>

//    @GET(VERSION_URL)
//    fun getVersionData(): Call<VersionResponse>

    @GET(PRE_VERSION_URL)
    fun getVersionData(): Call<VersionResponse>
}