package com.piperbloom.proxyvpn.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.databinding.ActivitySocialMediaBinding
import com.piperbloom.proxyvpn.viewmodel.SocialMediaAndSupportViewModel
import java.lang.Exception

class SocialMediaActivity : AppCompatActivity() {

    companion object{
        const val KEY_INFO_TEXT = "KEY_INFO_TEXT"
        const val KEY_TELEGRAM = "KEY_TELEGRAM"
        const val KEY_FACEBOOK = "KEY_FACEBOOK"
        const val KEY_GMAIL = "KEY_GMAIL"
    }

    private lateinit var binding: ActivitySocialMediaBinding

    private var telegramLink = ""
    private var facebookLink = ""
    private var infoText = ""

    override fun attachBaseContext(newBase: Context) {
        val overrideConfiguration = Configuration(
            newBase.resources.configuration
        ).apply { fontScale = 1.0f }

        super.attachBaseContext(
            newBase.createConfigurationContext(overrideConfiguration)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        infoText = intent.getStringExtra(KEY_INFO_TEXT).toString()
        facebookLink = intent.getStringExtra(KEY_FACEBOOK).toString()
        telegramLink = intent.getStringExtra(KEY_TELEGRAM).toString()

        window.navigationBarColor = ContextCompat.getColor(this, R.color.brown_900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        setUpListener()
        setUpUI()

    }

    private fun setUpUI() {
        binding.infoText.text = infoText
    }

//    private fun setUpViewModel() {
//        viewModel = ViewModelProvider(this)[SocialMediaAndSupportViewModel::class.java]
//        viewModel.platformData.observe(this, Observer { platformList ->
//            platformList?.let {
//                Log.d("SERVER_SITE_RESPONSE",platformList.toString())
//                for (platform in it) {
//                    if(platform.name == "telegram"){
//                        telegramLink = platform.link
//                    }else if(platform.name == "facebook"){
//                        facebookLink = platform.link
//                    }
//                }
//            }
//        })
//
//        viewModel.guideData.observe(this,Observer{ infoData->
//            infoData?.let {
//                binding.infoText.text = infoData.toString()
//            }
//        })
//
//        // Fetch the data
//        viewModel.fetchSocialMediaData()
//    }

    private fun setUpListener() {
        binding.backKey.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        binding.fbBtn.setOnClickListener {
            val scaleDownX = ObjectAnimator.ofFloat(it, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(it, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100
            val scaleUpX = ObjectAnimator.ofFloat(it, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(it, "scaleY", 1f)
            scaleUpX.duration = 150
            scaleUpY.duration = 150
            val scaleSet = AnimatorSet()
            scaleSet.playSequentially(scaleDownX, scaleDownY, scaleUpX, scaleUpY)
            scaleSet.start()

            openFacebookLink(facebookLink)
        }

        binding.teleBtn.setOnClickListener {
            val scaleDownX = ObjectAnimator.ofFloat(it, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(it, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100
            val scaleUpX = ObjectAnimator.ofFloat(it, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(it, "scaleY", 1f)
            scaleUpX.duration = 150
            scaleUpY.duration = 150
            val scaleSet = AnimatorSet()
            scaleSet.playSequentially(scaleDownX, scaleDownY, scaleUpX, scaleUpY)
            scaleSet.start()

            openTelegramLink(telegramLink)
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun openTelegramLink(telegramLink: String) {
        if(telegramLink.isNotEmpty()){
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramLink))

                intent.setPackage("org.telegram.messenger")

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramLink))
                    startActivity(webIntent)
                    Toast.makeText(this, "Telegram app not installed, opening in browser", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Log.d("Clicked",e.message.toString())
            }
        }
    }

    private fun openFacebookLink(messengerLink: String) {
        if(messengerLink.isNotEmpty()){
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(messengerLink))

                intent.setPackage("com.facebook.orca")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {

                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(messengerLink))
                startActivity(webIntent)

                Toast.makeText(this, "Messenger app not installed, opening in browser", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.d("Clicked", e.message.toString())
            }
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

}