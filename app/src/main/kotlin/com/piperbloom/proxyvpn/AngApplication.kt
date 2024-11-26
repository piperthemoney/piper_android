package com.piperbloom.proxyvpn

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.util.SiteChecker
//import cat.ereza.customactivityoncrash.CustomActivityOnCrash
//import cat.ereza.customactivityoncrash.config.CaocConfig
import com.tencent.mmkv.MMKV
import com.piperbloom.proxyvpn.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AngApplication : MultiDexApplication(), Configuration.Provider {

    companion object {
        //const val PREF_LAST_VERSION = "pref_last_version"
        lateinit var application: AngApplication
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }


    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize()
        }

        MMKV.initialize(this)

        Utils.setNightMode(application)

        CaocConfig.Builder.create()
            .trackActivities(true)
            .logErrorOnRestart(true)
            .apply()

        CustomActivityOnCrash.install(this)

    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:bg")
            .build()
    }
}
