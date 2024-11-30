package com.piperbloom.proxyvpn.ui.fragment

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.piperbloom.proxyvpn.AppConfig
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.data.SharedPrefManager
import com.piperbloom.proxyvpn.data.calculateExpirationDays
import com.piperbloom.proxyvpn.databinding.FragmentHomeBinding
import com.piperbloom.proxyvpn.dto.server.Data
import com.piperbloom.proxyvpn.helper.SimpleItemTouchHelperCallback
import com.piperbloom.proxyvpn.service.RetrofitClient
import com.piperbloom.proxyvpn.service.V2RayServiceManager
import com.piperbloom.proxyvpn.ui.CustomerSupportActivity
import com.piperbloom.proxyvpn.ui.MainRecyclerAdapter
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_GMAIL
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_INFO_TEXT
import com.piperbloom.proxyvpn.ui.SocialMediaActivity.Companion.KEY_TELEGRAM
import com.piperbloom.proxyvpn.util.ActivationRepository
import com.piperbloom.proxyvpn.util.AngConfigManager
import com.piperbloom.proxyvpn.util.MmkvManager
import com.piperbloom.proxyvpn.util.Utils
import com.piperbloom.proxyvpn.viewmodel.LoginViewModel
import com.piperbloom.proxyvpn.viewmodel.LoginViewModelFactory
import com.piperbloom.proxyvpn.viewmodel.MainViewModel
import com.piperbloom.proxyvpn.viewmodel.ServerViewModel
import com.piperbloom.proxyvpn.viewmodel.SocialMediaAndSupportViewModel
import com.tbruyelle.rxpermissions3.RxPermissions
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.chromium.base.ThreadUtils.runOnUiThread

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var serverSize = 0

    private var supportTeleLink = ""
    private var supportGmailLink = ""
    private var supportInfoText = ""

    private lateinit var backgroundHandler: Handler
    private lateinit var mainHandler: Handler
    private val checkInterval = 2000L


    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

//    private lateinit var dialog: AlertDialog

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var speedTestJob: Job? = null

    private val adapter by lazy { MainRecyclerAdapter(this, mainViewModel) }
//    private val adapter = MainRecyclerAdapter(this, mainViewModel)
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                try {
                    startV2Ray()
                    showDisConnectButton()
                    disableRefresh()
                } catch (e: Exception) {
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }else {
                Toast.makeText(context, "VPN permission required", Toast.LENGTH_SHORT).show()
            }
        }
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var serverViewModel: ServerViewModel
    private lateinit var socialViewModel: SocialMediaAndSupportViewModel

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
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        serverViewModel = ViewModelProvider(this)[ServerViewModel::class.java]
        socialViewModel = ViewModelProvider(this)[SocialMediaAndSupportViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        binding.loadingView.playAnimation()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = requireActivity().getSharedPreferences("your_prefs_server", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if(SharedPrefManager.getServerList(requireContext()) == null){
            if (mainViewModel.isRunning.value != true) {
                MmkvManager.removeAllserver()
                firstGetServer()
                mainViewModel.reloadServerList()
            }
        }else{
            setUpSortRecycler()
//            Log.d("SERVER_SIZE",serverSize.toString())
        }


        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        CoroutineScope(Dispatchers.Main).launch {
            RetrofitClient.initialize()
            setupViewModel()
        }
        setUpListener()
        setUpExpireDate()

        mainViewModel.copyAssets(requireContext().assets)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RxPermissions(requireActivity())
                .request(Manifest.permission.POST_NOTIFICATIONS)
                .subscribe {
                    if (!it)
                        Toast.makeText(context,R.string.toast_permission_denied,Toast.LENGTH_SHORT).show()
                }
        }

        val handlerThread = HandlerThread("BackgroundHandlerThread")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
        mainHandler = Handler(Looper.getMainLooper())

        view.isFocusableInTouchMode = true

        view.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B)) {
                requireActivity().moveTaskToBack(false)
                true
            } else {
                false
            }
        }

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpExpireDate() {
        val activationDate = SharedPrefManager.getActivationDate(requireContext()).toString()
        val lifespan = SharedPrefManager.getLifeSpan(requireContext()).toString()

        val daysUntilExpiration = calculateExpirationDays(activationDate, lifespan)

        Log.d("EXPIRE_DAYS",daysUntilExpiration.toString())
        binding.expireTv.text = "Expire in $daysUntilExpiration Days"
    }

    @SuppressLint("NotifyDataSetChanged", "Recycle")
    private fun setUpListener() {
        binding.connectBtn.setOnClickListener {
            if (mainViewModel.isRunning.value == true) {
                Utils.stopVService(requireContext())

                hideDisConnectButton()

                binding.refreshBtn.isEnabled = true
                binding.refreshBtn.isClickable = true
                binding.refreshBtn.isFocusable = true

                activeRefresh()


            } else if ((settingsStorage?.decodeString(AppConfig.PREF_MODE) ?: "VPN") == "VPN") {
                val intent = VpnService.prepare(requireContext())
                if (intent == null) {
                    startV2Ray()

                    showDisConnectButton()

                    binding.refreshBtn.isEnabled = false
                    binding.refreshBtn.isClickable = false
                    binding.refreshBtn.isFocusable = false

                    disableRefresh()

                } else {
                    requestVpnPermission.launch(intent)
                }
            } else {
                startV2Ray()

                showDisConnectButton()

                binding.refreshBtn.isEnabled = false
                binding.refreshBtn.isClickable = false
                binding.refreshBtn.isFocusable = false

                disableRefresh()

            }
        }

        binding.customerSupportBtn.setOnClickListener {
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

            val intent = Intent(requireActivity(), CustomerSupportActivity::class.java)
            intent.putExtra(KEY_TELEGRAM, supportTeleLink)
            intent.putExtra(KEY_GMAIL, supportGmailLink)
            intent.putExtra(KEY_INFO_TEXT, supportInfoText)
            startActivity(intent)
        }

        binding.refreshBtn.setOnClickListener {
//            CoroutineScope(Dispatchers.Main).launch {
//                RetrofitClient.initialize()
//                setupViewModel()
//            }

            val rotateAnimator = ObjectAnimator.ofFloat(binding.refreshIc, "rotation", 0f, 720f).apply {
                duration = 1000

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)

                        disableRefresh()
                    }
                })
            }

            rotateAnimator.start()

            lifecycleScope.launch {
                binding.recyclerView.visibility = View.GONE
                binding.loadingLy.visibility = View.VISIBLE

                binding.refreshBtn.isEnabled = false
                binding.refreshBtn.isClickable = false
                binding.refreshBtn.isFocusable = false

                try {
                    RetrofitClient.initialize()
                    getServer {
                        setUpSortRecycler()
                    }
//                    setUpSortRecycler()
                }catch (e: Exception) {
                    Log.e("RefreshError", "Error refreshing server list", e)
                }
            }
        }

    }

    private fun showDisConnectButton() {

//        half circle
        binding.halfCircle.apply {
            visibility = View.VISIBLE
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
            translationY = -200f
        }
        binding.halfCircle.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .start()


//        Disconnect Text
        binding.disConnectText.apply {
            visibility = View.VISIBLE
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
        }
        binding.disConnectText.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .start()

//        Disconnect Text
        binding.connectText.apply {
            visibility = View.VISIBLE
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }
        binding.connectText.animate()
            .scaleX(1.8f)
            .scaleY(1.8f)
            .alpha(1f)
            .setDuration(300)
            .start()

        rotateRightAndHide(binding.connectIcon)

    }

    private fun hideDisConnectButton() {

//        Disconnect Text
        binding.disConnectText.apply {
            visibility = View.VISIBLE
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }
        binding.disConnectText.animate()
            .scaleX(0.7f)
            .scaleY(0.7f)
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                binding.disConnectText.visibility = View.GONE
            }
            .start()

//        Connect Text
        binding.connectText.apply {
            visibility = View.VISIBLE
            scaleX = 2f
            scaleY = 2f
            alpha = 1f
        }
        binding.connectText.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .start()

        binding.halfCircle.apply {
            visibility = View.VISIBLE
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
            translationY = 200f
        }
        binding.halfCircle.animate()
            .scaleX(0.3f)
            .scaleY(0.3f)
            .alpha(0f)
            .translationY(-200f)
            .setDuration(600)
            .withEndAction {
                binding.halfCircle.visibility = View.GONE
            }
            .start()

        rotateLeftAndHide(binding.disConnectIcon)
        rotateRightAndShow(binding.connectIcon)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpSortRecycler() {
        binding.connectBtn.isClickable = false
        binding.connectBtn.isFocusable = false
        binding.connectBtn.isEnabled = false

        val clickedServer: Data? = SharedPrefManager.getUserChooseServer(requireContext())
        if(SharedPrefManager.getUserChooseServer(requireContext()) != null){
            val serverList = SharedPrefManager.getServerList(requireContext())?.toMutableList()
            if (serverList != null) {
                serverSize = serverList.size
//                Log.d("SERVER_SIZE",serverSize.toString())
            }
            if (clickedServer != null) {
                serverList?.remove(clickedServer)
                serverList?.add(0, clickedServer)
                if (serverList != null) {
                    SharedPrefManager.clearServerList(requireContext())
                    SharedPrefManager.saveServerList(requireContext(), serverList)
                }
            } else {
                Log.d("SERVER_INFO", "NO Match")
            }

        }

        MmkvManager.removeAllserver()
        setLetStoredServers(SharedPrefManager.getServerList(requireContext()))
        mainViewModel.reloadServerList()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // To prevent memory leaks
        _binding = null
    }

    private fun getServer(onComplete: () -> Unit) {
        serverViewModel.serverBatch.observe(viewLifecycleOwner) { dataList ->
            dataList?.let {

                SharedPrefManager.clearServerList(requireContext())
                SharedPrefManager.saveServerList(requireContext(), it)
                serverSize = dataList.size
//                Log.d("SERVER_SIZE", dataList.toString())

                for(data in dataList){
                    Log.d("SERVER_SIZE",data.vlessServers)
                }

            }
        }
        val token = SharedPrefManager.getToken(requireContext())
        if (token != null) {
            serverViewModel.fetchServerBatch(token)
        }

        onComplete()
    }

    private fun firstGetServer() {
        serverViewModel.serverBatch.observe(viewLifecycleOwner) { dataList ->
            dataList?.let {

                SharedPrefManager.clearServerList(requireContext())
                SharedPrefManager.saveServerList(requireContext(),it)
                serverSize = dataList.size

                for (data in dataList){
                    Log.d("SERVER_SIZE",data.vlessServers)
                }


                runOnUiThread {
                    runOnUiThread {
                        setStoredServers(SharedPrefManager.getServerList(requireContext()))
                    }
                }
            }
        }

        val token = SharedPrefManager.getToken(requireContext())
        if (token != null) {
            serverViewModel.fetchServerBatch(token)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setStoredServers(storedServer: List<Data>?) {
        val storedServers = storedServer?.reversed()
//        val lastIndex = (storedServers?.size ?: 1) - 1
        var num = 0

        storedServers?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                for (data in storedServers){
                    num += 1
                    importServer(data.vlessServers)
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            adapter.notifyDataSetChanged()
                            mainViewModel.reloadServerList()
                        }
                    }
                    if(num == storedServer.size){
                        withContext(Dispatchers.Main) {
                            if (isAdded) {
                                binding.loadingLy.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                            }
                        }
                    }
                    delay(200)
                }
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        adapter.setSelectedPosition(0)
                        binding.recyclerView.scrollToPosition(0)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setLetStoredServers(storedServer: List<Data>?) {
        val storedServers = storedServer?.reversed()
        var num = 0

        storedServers?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                for (data in storedServers){
                    num += 1
                    importServer(data.vlessServers)
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            adapter.notifyDataSetChanged()
                            mainViewModel.reloadServerList()
                        }
                    }
                    if(num == storedServer.size){
                        withContext(Dispatchers.Main) {
                            if (isAdded) {
//                                delay(5000)
                                binding.loadingLy.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                binding.connectBtn.isClickable = true
                                binding.connectBtn.isFocusable = true
                                binding.connectBtn.isEnabled = true

                            }
                        }
                    }
                    delay(30)
                }

                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        adapter.setSelectedPosition(0)
                        binding.recyclerView.scrollToPosition(0)
                        adapter.notifyDataSetChanged()

                        delay(5000)
                        binding.refreshBtn.isEnabled = true
                        binding.refreshBtn.isClickable = true
                        binding.refreshBtn.isFocusable = true

                        activeRefresh()
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {

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


        mainViewModel.updateListAction.observe(requireActivity()) { index ->
            if (index >= 0) {
                adapter.notifyItemChanged(index)
            } else {
                adapter.notifyDataSetChanged()
            }
        }
//        mainViewModel.updateTestResultAction.observe(requireActivity()) { setTestState(it) }
        mainViewModel.isRunning.observe(requireActivity()) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {

                binding.disConnectIcon.visibility = View.VISIBLE
                binding.halfCircle.visibility = View.VISIBLE
                binding.disConnectText.visibility = View.VISIBLE

                binding.connectIcon.visibility = View.INVISIBLE
                binding.connectText.visibility = View.INVISIBLE

            } else {

                binding.disConnectIcon.visibility = View.INVISIBLE
                binding.halfCircle.visibility = View.INVISIBLE
                binding.disConnectText.visibility = View.INVISIBLE

                binding.connectIcon.visibility = View.VISIBLE
                binding.connectText.visibility = View.VISIBLE

            }
        }
        mainViewModel.startListenBroadcast()
    }

    private fun startV2Ray() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            return
        }
        V2RayServiceManager.startV2Ray(requireContext())
    }

    public override fun onResume() {
        super.onResume()
        mainViewModel.reloadServerList()
    }

    public override fun onPause() {
        super.onPause()
    }


    private fun importServer(server: String)
            : Boolean {
        try {

            Handler(Looper.getMainLooper()).post {
                importBatchConfig(server)
                Log.d("server_token", server)
            }
        } catch (e: Exception) {
            Log.d("server_token", e.toString())
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun importBatchConfig(server: String?) {

        lifecycleScope.launch(Dispatchers.IO) {
            val count =
                AngConfigManager.importBatchConfig(server, mainViewModel.subscriptionId, true)
            delay(500L)
            withContext(Dispatchers.Main) {
                if (count > 0) {
                    mainViewModel.reloadServerList()
                    mainViewModel.testAllRealPing()
                } else {
                    Toast.makeText(context,R.string.toast_failure,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun rotateLeftAndHide(imageView: View) {
        val rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, -180f)
        rotateAnimator.duration = 300
        rotateAnimator.interpolator = AccelerateDecelerateInterpolator()

        rotateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {

//                binding.halfCircle.apply {
//                    visibility = View.VISIBLE
//                    scaleX = 1f
//                    scaleY = 1f
//                    alpha = 1f
//                    translationY = 200f
//                }
//                binding.halfCircle.animate()
//                    .scaleX(0.3f)
//                    .scaleY(0.3f)
//                    .alpha(0f)
//                    .translationY(-200f)
//                    .setDuration(300)
//                    .withEndAction {
//                        binding.halfCircle.visibility = View.GONE
//                    }
//                    .start()

//                binding.connectIcon.visibility = View.VISIBLE
                imageView.rotation = 0f
                imageView.visibility = View.INVISIBLE

            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

        rotateAnimator.start()
    }

    private fun rotateRightAndHide(imageView: View) {
        val rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 180f)
        rotateAnimator.duration = 300
        rotateAnimator.interpolator = AccelerateDecelerateInterpolator()

        rotateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding.disConnectIcon.visibility = View.VISIBLE
                imageView.rotation = 0f
                imageView.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

        rotateAnimator.start()
    }

    private fun rotateRightAndShow(imageView: View) {
        imageView.visibility = View.VISIBLE
        val rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 180f, 0f)
        rotateAnimator.duration = 400
        rotateAnimator.interpolator = AccelerateDecelerateInterpolator()

        rotateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding.disConnectIcon.visibility = View.INVISIBLE
                imageView.rotation = 0f
                imageView.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

        rotateAnimator.start()
    }

    fun handleBackPress() {
        requireActivity().finishAffinity()
    }

    fun disableRefresh(){
        val disIcColor = ContextCompat.getColor(requireContext(), R.color.disable_ic)
        val disBgColor = ContextCompat.getColor(requireContext(), R.color.disable_bg)
        binding.refreshIc.imageTintList = ColorStateList.valueOf(disIcColor)
        binding.refreshBtn.backgroundTintList = ColorStateList.valueOf(disBgColor)
    }

    private fun activeRefresh(){
        val acIcColor = ContextCompat.getColor(requireContext(), R.color.brown_900)
        val acBgColor = ContextCompat.getColor(requireContext(), R.color.colorSelected)
        binding.refreshIc.imageTintList = ColorStateList.valueOf(acIcColor)
        binding.refreshBtn.backgroundTintList = ColorStateList.valueOf(acBgColor)
    }
}