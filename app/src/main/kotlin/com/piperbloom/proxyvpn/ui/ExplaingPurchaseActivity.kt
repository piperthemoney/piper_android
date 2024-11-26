package com.piperbloom.proxyvpn.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.databinding.ActivityExplaingPurchaseBinding
import java.lang.Exception

class ExplaingPurchaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExplaingPurchaseBinding

    private var telegramLink = ""
    private var fbLink = ""
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
        binding = ActivityExplaingPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        infoText = intent.getStringExtra(SocialMediaActivity.KEY_INFO_TEXT).toString()
        telegramLink = intent.getStringExtra(SocialMediaActivity.KEY_TELEGRAM).toString()
        fbLink = intent.getStringExtra(SocialMediaActivity.KEY_FACEBOOK).toString()

        window.navigationBarColor = ContextCompat.getColor(this, R.color.brown_900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        setUpUI()
        setUpListener()

    }

    private fun setUpUI() {
        binding.infoText.text = infoText
    }

    private fun setUpListener() {
        binding.purchaseFbBtn.setOnClickListener {
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

            openFacebookLink(fbLink)
        }

        binding.purchaseTeleBtn.setOnClickListener {
            val scaleDownX = ObjectAnimator.ofFloat(it, "scaleX", 0.9f)
            val scaleDownY = ObjectAnimator.ofFloat(it, "scaleY", 0.9f)
            scaleDownX.duration = 100
            scaleDownY.duration = 100
            val scaleUpX = ObjectAnimator.ofFloat(it, "scaleX", 1f)
            val scaleUpY = ObjectAnimator.ofFloat(it, "scaleY", 1f)
            scaleUpX.duration = 100
            scaleUpY.duration = 100
            val scaleSet = AnimatorSet()
            scaleSet.playSequentially(scaleDownX, scaleDownY, scaleUpX, scaleUpY)
            scaleSet.start()

            openTelegramLink(telegramLink)

        }

    }

    override fun onBackPressed() {
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
        super.onBackPressed()
    }

    private fun openTelegramLink(telegramLink: String) {
        if(telegramLink.isNotEmpty()){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramLink))

            intent.setPackage("org.telegram.messenger")

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramLink))
                startActivity(webIntent)
                Toast.makeText(this, "Telegram app not installed, opening in browser", Toast.LENGTH_SHORT).show()
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

}