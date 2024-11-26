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
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.databinding.ActivityCustomerSupportBinding
import com.piperbloom.proxyvpn.viewmodel.SocialMediaAndSupportViewModel

class CustomerSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerSupportBinding

    private var telegramLink = ""
    private var gmailLink = ""
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
        binding = ActivityCustomerSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        infoText = intent.getStringExtra(SocialMediaActivity.KEY_INFO_TEXT).toString()
        telegramLink = intent.getStringExtra(SocialMediaActivity.KEY_TELEGRAM).toString()
        gmailLink = intent.getStringExtra(SocialMediaActivity.KEY_GMAIL).toString()

        if(gmailLink.isNotEmpty()){
            if(gmailLink == "null"){
                gmailLink = "info@pipermyanmar.com"
            }
        }else{
            gmailLink = "info@pipermyanmar.com"
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.brown_900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        setUpUI()
        setUpListener()
    }

    private fun setUpUI() {
        binding.infoText.text = infoText
        binding.gmailLinkTv.text = gmailLink
    }


    private fun setUpListener() {
        binding.backKey.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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

        binding.gmailBtn.setOnClickListener {
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

            openGmailApp(gmailLink)
        }

    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
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

    private fun openGmailApp(gmailLink: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(gmailLink))
//            putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
//            putExtra(Intent.EXTRA_TEXT, "Body of the email here")
            setPackage("com.google.android.gm")
        }

        // Verify that there is a Gmail app installed
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            // Handle case where the Gmail app is not available
            Toast.makeText(this, "Gmail app is not installed.", Toast.LENGTH_SHORT).show()
        }
    }

}