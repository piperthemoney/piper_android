package com.piperbloom.proxyvpn.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.piperbloom.proxyvpn.dto.login.ActivationRequest
import com.piperbloom.proxyvpn.dto.login.Login
import com.piperbloom.proxyvpn.dto.server.Server
import com.piperbloom.proxyvpn.service.RetrofitClient
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class ActivationRepository {

    fun activateUserCode(code: String): LiveData<Login> {
        val result = MutableLiveData<Login>() // MutableLiveData to hold the ActivationResponse
        val request = ActivationRequest(code)

        RetrofitClient.apiService.activateCode(request).enqueue(object : Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                if (response.isSuccessful) {
                    // Handle successful response
                    response.body()?.let {
                        result.value = it // Set the successful response body
                    } ?: run {
                        // Handle empty response body
                        result.value = Login(
                            code = 500,
                            status = null,
                            message = "Activation Fail",
                            data = null,
                            error = null,
                            stackTrace = null,
                            token = null
                        )
                    }
                } else {
                    // Handle error response (non-2xx HTTP status code)
                    response.errorBody()?.let { errorBody ->
                        try {
                            // Parse error body using Gson to extract the "status" and "message"
                            val gson = Gson()
                            val jsonObject = gson.fromJson(errorBody.string(), JsonObject::class.java)

                            val status = jsonObject.get("status")?.asString ?: "Unknown status"
                            val message = jsonObject.get("message")?.asString ?: "Unknown message"

                            // Set the result with the parsed status and message
                            result.value = Login(
                                code = response.code(),
                                status = status,
                                message = "Activation Fail",
                                data = null,
                                error = null,
                                stackTrace = null,
                                token = null
                            )
                        } catch (e: Exception) {
                            // Handle any exception while parsing error body
                            result.value = Login(
                                code = response.code(),
                                status = null,
                                message = "Activation Fail",
                                data = null,
                                error = null,
                                stackTrace = e.stackTraceToString(),
                                token = null
                            )
                        }
                    } ?: run {
                        // If the errorBody is null, handle this scenario
                        result.value = Login(
                            code = response.code(),
                            status = null,
                            message = "Activation Fail",
                            data = null,
                            error = null,
                            stackTrace = null,
                            token = null
                        )
                    }
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                // Handle network failure
                result.value = Login(
                    code = 500,
                    status = null,
                    message = "Activation Fail",
                    data = null,
                    error = null,
                    stackTrace = t.stackTraceToString(),
                    token = null
                )
            }
        })


        return result // Return the LiveData containing the ActivationResponse
    }


}