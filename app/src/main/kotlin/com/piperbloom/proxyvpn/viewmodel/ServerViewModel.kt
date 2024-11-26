package com.piperbloom.proxyvpn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.piperbloom.proxyvpn.dto.server.Data
import com.piperbloom.proxyvpn.dto.server.Server
import com.piperbloom.proxyvpn.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServerViewModel : ViewModel() {

    private val _serverBatch = MutableLiveData<List<Data>>()
    val serverBatch: LiveData<List<Data>> get() = _serverBatch

    fun fetchServerBatch(token: String) {

        RetrofitClient.apiService.getServerBatch("Bearer $token").enqueue(object : Callback<Server> {
            override fun onResponse(call: Call<Server>, response: Response<Server>) {
                if (response.isSuccessful) {
                    _serverBatch.value = response.body()?.data
                } else {
                    // Handle error case
                }
            }

            override fun onFailure(call: Call<Server>, t: Throwable) {
                // Handle failure case
            }
        })
    }

}