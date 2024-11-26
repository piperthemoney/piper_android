package com.piperbloom.proxyvpn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.piperbloom.proxyvpn.dto.login.Login
import com.piperbloom.proxyvpn.dto.server.Data
import com.piperbloom.proxyvpn.dto.server.Server
import com.piperbloom.proxyvpn.dto.version.VersionData
import com.piperbloom.proxyvpn.dto.version.VersionResponse
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.util.ActivationRepository
import com.piperbloom.proxyvpn.util.Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val repository: ActivationRepository) : ViewModel() {

    private val _activationResult = MutableLiveData<Login>()
    val activationResult: LiveData<Login> get() = _activationResult

    private val _loginData = MutableLiveData<Login?>()
    val loginData: LiveData<Login?> get() = _loginData

    private val _activationMessage = MutableLiveData<String?>()
    val activationMessage: LiveData<String?> get() = _activationMessage

    private val _fullActivationResponse = MutableLiveData<Login?>()
    val fullActivationResponse: LiveData<Login?> get() = _fullActivationResponse

    private val _versionData = MutableLiveData<VersionData>()
    val versionData: LiveData<VersionData> get() = _versionData

    fun activateUserCode(code: String) {
        repository.activateUserCode(code).observeForever { response ->
            _activationResult.value = response

            // Check the status and update login data and message accordingly
            if (response.status == "success") {
                _loginData.value = response
                _activationMessage.value = null
                _fullActivationResponse.value = response // Store the full response
            } else {
                _loginData.value = response
                _activationMessage.value = null
                _fullActivationResponse.value = response // Store the full response even on failure
            }
        }
    }

    fun getVersionCheck(){
        RetrofitClient.apiService.getVersionData().enqueue(object :
            Callback<VersionResponse> {
            override fun onResponse(call: Call<VersionResponse>, response: Response<VersionResponse>) {
                if (response.isSuccessful) {
                    _versionData.value = response.body()?.data?.get(0)
                } else {
                    // Handle error case
                }
            }

            override fun onFailure(call: Call<VersionResponse>, t: Throwable) {
                // Handle failure case
            }
        })
    }

}