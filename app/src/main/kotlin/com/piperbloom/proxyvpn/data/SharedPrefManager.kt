package com.piperbloom.proxyvpn.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.piperbloom.proxyvpn.dto.server.Data

object SharedPrefManager {

    private const val PREF_NAME = "my_app_prefs"
    private const val KEY_CODE = "activation_code"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_DATA = "server_list"
    private const val KEY_USER_CHOOSE_SERVER = "user_choose_server"
    private const val KEY_ACTIVATION_DATE = "activation_date"
    private const val KEY_LIFESPAN = "lifespan"
    private const val KEY_SELECTED = "select_country"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveCode(context: Context, code: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_CODE, code)
        editor.apply()
    }

    fun saveToken(context: Context, token: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    fun getCode(context: Context): String? {
        return getSharedPreferences(context)
            .getString(KEY_CODE, null)
    }

    fun getToken(context: Context): String? {
        return getSharedPreferences(context)
            .getString(KEY_TOKEN, null)
    }

    fun saveServerList(context: Context, dataList: List<Data>) {
        val editor = getSharedPreferences(context).edit()

        val gson = Gson()
        val json = gson.toJson(dataList)

        editor.putString(KEY_DATA, json)
        editor.apply()
    }

    fun getServerList(context: Context): List<Data>? {
        val json = getSharedPreferences(context).getString(KEY_DATA, null)

        return if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Data>>() {}.type
            val serverList: List<Data> = gson.fromJson(json, type)
//            serverList.shuffled()
            serverList
        } else {
            null
        }
    }

    fun saveUserChooseServer(context: Context, server: Data) {
        val editor = getSharedPreferences(context).edit()
        val jsonString = Gson().toJson(server)
        editor.putString(KEY_USER_CHOOSE_SERVER, jsonString)
        editor.apply()
    }

    fun getUserChooseServer(context: Context): Data? {
        val jsonString = getSharedPreferences(context).getString(KEY_USER_CHOOSE_SERVER, null)
        return jsonString?.let {
            Gson().fromJson(it, Data::class.java)
        }
    }

    fun clearServerList(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(KEY_DATA)
        editor.apply()
    }

    fun saveActivationDate(context: Context, date: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_ACTIVATION_DATE, date)
        editor.apply()
    }

    fun getActivationDate(context: Context): String? {
        return getSharedPreferences(context)
            .getString(KEY_ACTIVATION_DATE, null)
    }

    fun saveLifeSpan(context: Context, date: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_LIFESPAN, date)
        editor.apply()
    }

    fun getLifeSpan(context: Context): String? {
        return getSharedPreferences(context)
            .getString(KEY_LIFESPAN, null)
    }

    fun saveSelected(context: Context, server: Data) {
        val editor = getSharedPreferences(context).edit()
        val jsonString = Gson().toJson(server)
        editor.putString(KEY_SELECTED, jsonString)
        editor.apply()
    }

    fun getSelected(context: Context): Data? {
        val jsonString = getSharedPreferences(context).getString(KEY_SELECTED, null)
        return jsonString?.let {
            Gson().fromJson(it, Data::class.java)
        }
    }


    fun clear(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}