package com.piperbloom.proxyvpn.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.databinding.FragmentAboutBinding
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.ui.CustomerSupportActivity
import com.piperbloom.proxyvpn.ui.HomeActivity
import com.piperbloom.proxyvpn.ui.PrivacyPolicyActivity
import com.piperbloom.proxyvpn.ui.SocialMediaActivity
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_FACEBOOK
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_GMAIL
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_INFO_TEXT
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_TELEGRAM
import com.piperbloom.proxyvpn.viewmodel.SocialMediaAndSupportViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var socialViewModel: SocialMediaAndSupportViewModel

    private var socialInfoText = ""
    private var socialFbLink = ""
    private var socialTeleLink = ""

    private var supportTeleLink = ""
    private var supportGmailLink = ""
    private var supportInfoText = ""

    override fun onAttach(context: Context) {
        val newBase = updateBaseContext(context)
        super.onAttach(newBase)
    }

    private fun updateBaseContext(newBase: Context): Context {
        val overrideConfiguration = Configuration(
            newBase.resources.configuration
        ).apply { fontScale = 1.0f } // Prevent font scaling

        return newBase.createConfigurationContext(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutBinding.inflate(inflater,container,false)
                // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // To prevent memory leaks
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpVersionName()
        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize()
            setUpViewModel()
        }
        setUpListener()
    }

    @SuppressLint("SetTextI18n")
    private fun setUpVersionName() {
        val versionName = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        binding.versionTv.text = "App Version - $versionName"
    }

    private fun setUpViewModel() {
        //Social Media Data
        socialViewModel = ViewModelProvider(requireActivity())[SocialMediaAndSupportViewModel::class.java]
        socialViewModel.platformData.observe(viewLifecycleOwner, Observer { platformList ->
            platformList?.let {
                for (platform in it) {
                    if(platform.name == "telegram"){
                         socialTeleLink = platform.link
                    }else if(platform.name == "facebook"){
                        socialFbLink = platform.link
                    }
                }
            }
        })
        socialViewModel.guideData.observe(viewLifecycleOwner,Observer{ infoData->
            infoData?.let {
                socialInfoText = infoData.toString()
            }
        })
        socialViewModel.fetchSocialMediaData()

        socialViewModel.platformData2.observe(viewLifecycleOwner, Observer { platformList ->
            platformList?.let {
                for (platform in it) {
                    if(platform.name == "telegram"){
                        supportTeleLink = platform.link
                    }else if(platform.name == "gmail"){
                        supportGmailLink = platform.link
                    }
                }
            }
        })
        socialViewModel.guideData2.observe(viewLifecycleOwner,Observer{ infoData->
            infoData?.let {
                supportInfoText = infoData.toString()
            }
        })
        // Fetch the data
        socialViewModel.fetchSupportData()

    }

    private fun setUpListener() {
        binding.privacyView.setOnClickListener {
            val intent = Intent(activity, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        binding.socialMediaView.setOnClickListener {
            val intent = Intent(activity, SocialMediaActivity::class.java)
            intent.putExtra(KEY_INFO_TEXT, socialInfoText)
            intent.putExtra(KEY_TELEGRAM, socialTeleLink)
            intent.putExtra(KEY_FACEBOOK, socialFbLink)
            startActivity(intent)
        }

        binding.customerSupportView.setOnClickListener {
            val intent = Intent(activity, CustomerSupportActivity::class.java)
            intent.putExtra(KEY_TELEGRAM, supportTeleLink)
            intent.putExtra(KEY_GMAIL, supportGmailLink)
            intent.putExtra(KEY_INFO_TEXT, supportInfoText)
            startActivity(intent)
        }

    }

    fun handleBackPress() {
        requireActivity().finishAffinity()
    }

}