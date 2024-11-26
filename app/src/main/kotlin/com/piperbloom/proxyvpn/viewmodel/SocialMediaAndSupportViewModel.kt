package com.piperbloom.proxyvpn.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.piperbloom.proxyvpn.dto.social.SocialMediaResponse
import com.piperbloom.proxyvpn.dto.support.SupportResponse
import com.piperbloom.proxyvpn.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SocialMediaAndSupportViewModel: ViewModel() {

    private val _platformData = MutableLiveData<List<com.piperbloom.proxyvpn.dto.social.Platform>>()
    val platformData: LiveData<List<com.piperbloom.proxyvpn.dto.social.Platform>> get() = _platformData

    private val _platformData2 = MutableLiveData<List<com.piperbloom.proxyvpn.dto.support.Platform>>()
    val platformData2: LiveData<List<com.piperbloom.proxyvpn.dto.support.Platform>> get() = _platformData2

    private val _guideData = MutableLiveData<String>()
    val guideData: LiveData<String> get() = _guideData

    private val _guideData2 = MutableLiveData<String>()
    val guideData2: LiveData<String> get() = _guideData2

    fun fetchSocialMediaData() {
        RetrofitClient.apiService.getSocialMediaData().enqueue(object :
            Callback<SocialMediaResponse> {
            override fun onResponse(call: Call<SocialMediaResponse>, response: Response<SocialMediaResponse>) {
                Log.d("SERVER_SITE_RESPONSE",response.body().toString())
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.data.isNotEmpty()) {
                            // Extract the platform list and set it to LiveData
                            _platformData.value = it.data[0].platform
                            _guideData.value = it.data[0].guide
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SocialMediaResponse>, t: Throwable) {
                // Handle network failure
                Log.e("SERVER_SITE_RESPONSE",t.message.toString())
            }
        })
    }

    fun fetchSupportData() {
        RetrofitClient.apiService.getSupportData().enqueue(object :
            Callback<SupportResponse> {
            override fun onResponse(call: Call<SupportResponse>, response: Response<SupportResponse>) {
                Log.d("SERVER_SITE_RESPONSE",response.body().toString())
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.data.isNotEmpty()) {
                            // Extract the platform list and set it to LiveData
                            _platformData2.value = it.data[0].platform
                            _guideData2.value = it.data[0].guide
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SupportResponse>, t: Throwable) {
                // Handle network failure
                Log.e("SERVER_SITE_RESPONSE",t.message.toString())
            }
        })
    }

}