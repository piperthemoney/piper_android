package com.piperbloom.proxyvpn.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding

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
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.brown_900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        setUpUI()
        setUpListener()

    }

    private fun setUpUI() {
        binding.headerText.isSelected = true

        val privacyInfoHtml = getString(R.string.privacy_info_tv)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.privacyInfoTv.text = Html.fromHtml(privacyInfoHtml, Html.FROM_HTML_MODE_LEGACY)
        } else {
            binding.privacyInfoTv.text = Html.fromHtml(privacyInfoHtml)
        }
    }

    private fun setUpListener() {
        binding.backKey.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}