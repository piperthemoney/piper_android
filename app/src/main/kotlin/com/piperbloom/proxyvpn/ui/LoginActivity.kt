package com.piperbloom.proxyvpn.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.piperbloom.proxyvpn.BuildConfig
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.data.SharedPrefManager
import com.piperbloom.proxyvpn.databinding.ActivityLoginBinding
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_FACEBOOK
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_INFO_TEXT
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_TELEGRAM
import com.piperbloom.proxyvpn.ui.fragment.CustomBlurredDialogFragment
import com.piperbloom.proxyvpn.util.ActivationRepository
import com.piperbloom.proxyvpn.util.Result
import com.piperbloom.proxyvpn.util.SiteChecker
import com.piperbloom.proxyvpn.viewmodel.LoginViewModel
import com.piperbloom.proxyvpn.viewmodel.LoginViewModelFactory
import com.piperbloom.proxyvpn.viewmodel.SocialMediaAndSupportViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private val repository = ActivationRepository()
    private lateinit var socialViewModel: SocialMediaAndSupportViewModel

    private var supportTeleLink = ""
    private var supportFbLink = ""
    private var supportGmailLink = ""
    private var supportInfoText = ""

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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.brown_900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize()
            setUpViewModel()
        }
        setUpListener()
    }

    private fun setUpViewModel() {

        viewModel =
            ViewModelProvider(this, LoginViewModelFactory(repository))[LoginViewModel::class.java]
        viewModel.activationResult.observe(this) { activationResponse ->
            activationResponse?.let { it ->
                if (it.status == "success") {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
                    val activationDate = it.data?.user?.codeEntry?.activationDate
                    val lifespan = it.data?.user?.lifespan
                    activationDate?.let { date-> SharedPrefManager.saveActivationDate(this, date) }
                    lifespan?.let { life-> SharedPrefManager.saveLifeSpan(this, life) }

                    it.token?.let { it1 -> passIntent(binding.enterCodeEt.text.toString(), it1) }

                } else {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        viewModel.versionData.observe(this){ data ->
            data?.let {
                if(data.version != versionName){
                    showBlurredDialog(data.playstore,data.externalUrl)
                }
            }
        }
        viewModel.getVersionCheck()



        socialViewModel = ViewModelProvider(this)[SocialMediaAndSupportViewModel::class.java]
        socialViewModel.platformData2.observe(this, Observer { platformList ->
            platformList?.let {
                for (platform in it) {
                    when (platform.name) {
                        "telegram" -> {
                            supportTeleLink = platform.link
                        }
                        "gmail" -> {
                            supportGmailLink = platform.link
                        }
                        "facebook" -> {
                            supportFbLink = platform.link
                        }
                    }
                }
            }
        })
        socialViewModel.guideData2.observe(this,Observer{ infoData->
            infoData?.let {
                supportInfoText = infoData.toString()
            }
        })
        // Fetch the data
        socialViewModel.fetchSupportData()

    }

    private fun passIntent(code: String, token: String) {
        SharedPrefManager.saveCode(this@LoginActivity, code)
        SharedPrefManager.saveToken(this@LoginActivity, token)

        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListener() {
        binding.activateBtn.setOnClickListener {
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

            activate(binding.enterCodeEt.text.toString())
        }

        binding.purchaseBtn.setOnClickListener {
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

            val intent = Intent(this, ExplaingPurchaseActivity::class.java)
            intent.putExtra(KEY_TELEGRAM,supportTeleLink)
            intent.putExtra(KEY_FACEBOOK,supportFbLink)
            intent.putExtra(KEY_INFO_TEXT,supportInfoText)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        binding.enterCodeEt.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.enterCodeEt.right - binding.enterCodeEt.compoundDrawables[2].bounds.width())) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = clipboard.primaryClip

                    if (clipData != null && clipData.itemCount > 0) {
                        val pasteData = clipData.getItemAt(0).text
                        binding.enterCodeEt.setText(pasteData)
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

    }


    private fun activate(code: String) {
        viewModel.activateUserCode(code)
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showBlurredDialog(playstoreUrl: String,externalUrl: String) {
        val dialogFragment = CustomBlurredDialogFragment().apply {
            arguments = Bundle().apply {
                putString("playstoreUrl", playstoreUrl)
                putString("externalUrl", externalUrl)
            }
        }
        dialogFragment.show(supportFragmentManager, "customDialog")
    }
}