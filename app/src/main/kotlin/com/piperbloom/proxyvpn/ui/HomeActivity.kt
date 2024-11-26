package com.piperbloom.proxyvpn.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.piperbloom.proxyvpn.BuildConfig.BASE_URL
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.data.SharedPrefManager
import com.piperbloom.proxyvpn.databinding.ActivityHomeBinding
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.ui.fragment.AboutFragment
import com.piperbloom.proxyvpn.ui.fragment.CustomBlurredDialogFragment
import com.piperbloom.proxyvpn.ui.fragment.HomeFragment
import com.piperbloom.proxyvpn.util.ActivationRepository
import com.piperbloom.proxyvpn.util.Result
import com.piperbloom.proxyvpn.util.SiteChecker
import com.piperbloom.proxyvpn.util.Utils
import com.piperbloom.proxyvpn.viewmodel.LoginViewModel
import com.piperbloom.proxyvpn.viewmodel.LoginViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: LoginViewModel
    private val repository = ActivationRepository()

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
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize()
            setUpViewModel()
        }

        window.navigationBarColor = ContextCompat.getColor(this, R.color.nav_color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        saveInstance(savedInstanceState)
        setUpFragment()
//        showBlurredDialog()
//        throw RuntimeException("Boom!");
    }

    private fun saveInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            loadFragment(HomeFragment(), "HOME_FRAGMENT")
        }
    }

    private fun setUpViewModel() {

        viewModel =
            ViewModelProvider(this, LoginViewModelFactory(repository))[LoginViewModel::class.java]
        viewModel.activationResult.observe(this) { activationResponse ->
            activationResponse?.let { it ->
                if (it.status == "success") {
                    SharedPrefManager.getCode(this)
                        ?.let { it1 -> saveData(it1,it.token.toString()) }

                    val activationDate = it.data?.user?.codeEntry?.activationDate
                    val lifespan = it.data?.user?.lifespan
                    activationDate?.let { date-> SharedPrefManager.saveActivationDate(this, date) }
                    lifespan?.let { life-> SharedPrefManager.saveLifeSpan(this, life) }

                } else {
                    if(it.status == "fail"){
                        Toast.makeText(this, "Activation ${it.status}", Toast.LENGTH_LONG).show()
//                        Log.d("SERVER_RESPONSE",it.toString())
                        Utils.stopVService(this@HomeActivity)
                        clearData()
                    }
                }
            }
        }

        SharedPrefManager.getCode(this@HomeActivity)?.let { viewModel.activateUserCode(it) }
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName

        viewModel.versionData.observe(this){ data ->
            data?.let {
//                Log.d("VERSION_DATA",data.version)
                if(data.version != versionName){
                    showBlurredDialog(data.playstore,data.externalUrl)
                }
            }
        }
        viewModel.getVersionCheck()
    }

    private fun clearData() {
        SharedPrefManager.clear(this@HomeActivity)
        val intent = Intent(this@HomeActivity,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveData(code: String, token: String) {
        SharedPrefManager.saveCode(this@HomeActivity, code)
        SharedPrefManager.saveToken(this@HomeActivity, token)
    }

    private fun setUpFragment() {
        loadFragment(HomeFragment(), "HOME_FRAGMENT")
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    loadFragment(HomeFragment(), "HOME_FRAGMENT")
                    true
                }

                R.id.about -> {
                    loadFragment(AboutFragment(), "ABOUT_FRAGMENT")
                    true
                }

                else -> {
                    loadFragment(HomeFragment(), "HOME_FRAGMENT")
                    true
                }
            }
        }
    }

//    private fun loadFragment(fragment: Fragment){
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.container,fragment)
//        transaction.commit()
//    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // Get the currently active fragment (the one currently shown)
        val currentFragment = fragmentManager.findFragmentById(R.id.container)

        // Check if the fragment to load is already added
        val existingFragment = fragmentManager.findFragmentByTag(tag)

        if (existingFragment == null) {
            // If the fragment doesn't exist, add it
            transaction.add(R.id.container, fragment, tag)
        } else {
            // If the fragment already exists, show it
            transaction.show(existingFragment)
        }

        // Hide the currently active fragment, if it's not the same fragment
        if (currentFragment != null && currentFragment != existingFragment) {
            transaction.hide(currentFragment)
        }

        // Commit the transaction
        transaction.commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            val fragment = supportFragmentManager.findFragmentById(R.id.container)
            if (fragment is HomeFragment) {
                fragment.handleBackPress()
                return true
            }else if (fragment is AboutFragment){
                fragment.handleBackPress()
                return true
            }
//            moveTaskToBack(false)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is HomeFragment) {
            fragment.handleBackPress()
        } else {
            super.onBackPressed()
        }
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

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        // Save the current fragment's tag, so you can restore it later
//        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
//        currentFragment?.let {
//            outState.putString("current_fragment_tag", currentFragment.tag)
//        }
//    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize()
            setUpViewModel()
            SharedPrefManager.getCode(this@HomeActivity)?.let { viewModel.activateUserCode(it) }
        }
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
        if (currentFragment == null) {
            loadFragment(HomeFragment(), "HOME_FRAGMENT")
        }
    }

}