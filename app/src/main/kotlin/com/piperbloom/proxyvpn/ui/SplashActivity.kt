package com.piperbloom.proxyvpn.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.data.SharedPrefManager
import com.piperbloom.proxyvpn.databinding.ActivitySplashBinding
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.viewmodel.ServerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val splashScreenDuration: Long = 2000
    private lateinit var serverViewModel: ServerViewModel

    override fun attachBaseContext(newBase: Context) {
        val overrideConfiguration = Configuration(
            newBase.resources.configuration
        ).apply { fontScale = 1.0f }

        super.attachBaseContext(
            newBase.createConfigurationContext(overrideConfiguration)
        )
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        val permissions = packageInfo.requestedPermissions
        val permissionsList = StringBuilder()
        if (permissions != null) {
            for (permission in permissions) {
                val permissionStatus = if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    "GRANTED"
                } else {
                    "DENIED"
                }
                permissionsList.append("Permission: $permission\nStatus: $permissionStatus\n")
            }
        } else {
            permissionsList.append("No permissions found in the manifest.")
        }
        Log.d("PERMISSION_LIST",permissionsList.toString())


        // Set full-screen mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            val controller = window.insetsController
            controller?.hide(android.view.WindowInsets.Type.statusBars())
            controller?.hide(android.view.WindowInsets.Type.navigationBars())
            // Optional: Set the appearance of the navigation bar
            controller?.setSystemBarsAppearance(
                android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                android.view.WindowInsets.Type.navigationBars()
            )
        } else {
            // For older Android versions
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.brown_900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

//        binding.animationView.imageAssetsFolder = "images/"
//        binding.animationView.setAnimation(R.raw.splash)
        binding.animationView.playAnimation()
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize(applicationContext)
            setupViewModel()
        }
        setTimer()

    }

    private fun setTimer() {
        Handler().postDelayed({
            if (SharedPrefManager.getCode(this@SplashActivity) != null) {
                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            } else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
                finish()
            }
        }, splashScreenDuration)
    }

    private fun setupViewModel() {
        serverViewModel = ViewModelProvider(this)[ServerViewModel::class.java]
        if (SharedPrefManager.getToken(this@SplashActivity) != null) {
            serverViewModel.serverBatch.observe(this) { dataList ->
//                Log.d("SERVER_LIST", dataList.toString())
                dataList?.let {
                    SharedPrefManager.clearServerList(this)
                    SharedPrefManager.saveServerList(this, it)
                }
            }
        }

        SharedPrefManager.getToken(this@SplashActivity)
            ?.let { serverViewModel.fetchServerBatch(it) }

    }

}