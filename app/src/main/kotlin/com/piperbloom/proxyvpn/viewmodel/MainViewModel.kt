package com.piperbloom.proxyvpn.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.piperbloom.proxyvpn.AngApplication
import com.piperbloom.proxyvpn.AppConfig
import com.piperbloom.proxyvpn.AppConfig.ANG_PACKAGE
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.databinding.DialogConfigFilterBinding
import com.piperbloom.proxyvpn.dto.EConfigType
import com.piperbloom.proxyvpn.dto.ServerConfig
import com.piperbloom.proxyvpn.dto.ServersCache
import com.piperbloom.proxyvpn.dto.V2rayConfig
import com.piperbloom.proxyvpn.extension.toast
import com.piperbloom.proxyvpn.util.MessageUtil
import com.piperbloom.proxyvpn.util.MmkvManager
import com.piperbloom.proxyvpn.util.MmkvManager.KEY_ANG_CONFIGS
import com.piperbloom.proxyvpn.util.SpeedtestUtil
import com.piperbloom.proxyvpn.util.Utils
import com.piperbloom.proxyvpn.util.V2rayConfigUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val serverRawStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SERVER_RAW,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }

    var serverList = MmkvManager.decodeServerList()
    var subscriptionId: String = settingsStorage.decodeString(AppConfig.CACHE_SUBSCRIPTION_ID, "")?:""
    var keywordFilter: String = settingsStorage.decodeString(AppConfig.CACHE_KEYWORD_FILTER, "")?:""
        private set
    val serversCache = mutableListOf<ServersCache>()
    val isRunning by lazy { MutableLiveData<Boolean>() }
    val updateListAction by lazy { MutableLiveData<Int>() }
    val updateTestResultAction by lazy { MutableLiveData<String>() }

    private val tcpingTestScope by lazy { CoroutineScope(Dispatchers.IO) }

    fun startListenBroadcast() {
        isRunning.value = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<AngApplication>().registerReceiver(
                mMsgReceiver,
                IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY),
                Context.RECEIVER_EXPORTED
            )
        } else {
            getApplication<AngApplication>().registerReceiver(
                mMsgReceiver,
                IntentFilter(AppConfig.BROADCAST_ACTION_ACTIVITY)
            )
        }
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_REGISTER_CLIENT, "")
    }

    override fun onCleared() {
        getApplication<AngApplication>().unregisterReceiver(mMsgReceiver)
        tcpingTestScope.coroutineContext[Job]?.cancelChildren()
        SpeedtestUtil.closeAllTcpSockets()
        Log.i(ANG_PACKAGE, "Main ViewModel is cleared")
        super.onCleared()
    }

    fun reloadServerList() {
        serverList = MmkvManager.decodeServerList()
        updateCache()
        updateListAction.value = -1
    }

    fun clearData(){
        serversCache.clear()
    }

    fun removeServer(guid: String) {
        serverList.remove(guid)
        MmkvManager.removeServer(guid)
        val index = getPosition(guid)
        if (index >= 0) {
            serversCache.removeAt(index)
        }
    }

    fun appendCustomConfigServer(server: String): Boolean {
        if (server.contains("inbounds")
            && server.contains("outbounds")
            && server.contains("routing")
        ) {
            try {
                val config = ServerConfig.create(com.piperbloom.proxyvpn.dto.EConfigType.CUSTOM)
                config.subscriptionId = subscriptionId
                config.fullConfig = Gson().fromJson(server, V2rayConfig::class.java)
                config.remarks = config.fullConfig?.remarks ?: System.currentTimeMillis().toString()
                val key = MmkvManager.encodeServerConfig("", config)
                serverRawStorage?.encode(key, server)
                serverList.add(0, key)
                serversCache.add(0, ServersCache(key, config))
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun swapServer(fromPosition: Int, toPosition: Int) {
        Collections.swap(serverList, fromPosition, toPosition)
        Collections.swap(serversCache, fromPosition, toPosition)
        mainStorage?.encode(KEY_ANG_CONFIGS, Gson().toJson(serverList))
    }

    @Synchronized
    fun updateCache() {
        serversCache.clear()
        for (guid in serverList) {
            val config = MmkvManager.decodeServerConfig(guid) ?: continue
            if (subscriptionId.isNotEmpty() && subscriptionId != config.subscriptionId) {
                continue
            }

            if (keywordFilter.isEmpty() || config.remarks.contains(keywordFilter)) {
                serversCache.add(ServersCache(guid, config))
            }
        }
    }

    fun testAllTcping() {
        tcpingTestScope.coroutineContext[Job]?.cancelChildren()
        SpeedtestUtil.closeAllTcpSockets()
        MmkvManager.clearAllTestDelayResults(serversCache.map { it.guid }.toList())
        updateListAction.value = -1 // update all

        getApplication<AngApplication>().toast(R.string.connection_test_testing)
        for (item in serversCache) {
            item.config.getProxyOutbound()?.let { outbound ->
                val serverAddress = outbound.getServerAddress()
                val serverPort = outbound.getServerPort()
                if (serverAddress != null && serverPort != null) {
                    tcpingTestScope.launch {
                        val testResult = SpeedtestUtil.tcping(serverAddress, serverPort)
                        launch(Dispatchers.Main) {
                            MmkvManager.encodeServerTestDelayMillis(item.guid, testResult)
                            updateListAction.value = getPosition(item.guid)
                        }
                    }
                }
            }
        }
    }

    fun testAllRealPing() {
        MessageUtil.sendMsg2TestService(getApplication(), AppConfig.MSG_MEASURE_CONFIG_CANCEL, "")
        MmkvManager.clearAllTestDelayResults(serversCache.map { it.guid }.toList())
        updateListAction.value = -1 // update all

        val serversCopy = serversCache.toList() // Create a copy of the list

//        getApplication<AngApplication>().toast(R.string.connection_test_testing)
        viewModelScope.launch(Dispatchers.Default) { // without Dispatchers.Default viewModelScope will launch in main thread
            for (item in serversCopy) {
                val config = V2rayConfigUtil.getV2rayConfig(getApplication(), item.guid)
                if (config.status) {
                    MessageUtil.sendMsg2TestService(
                        getApplication(),
                        AppConfig.MSG_MEASURE_CONFIG,
                        Pair(item.guid, config.content)
                    )
                }
            }
        }
    }

    fun testCurrentServerRealPing() {
        MessageUtil.sendMsg2Service(getApplication(), AppConfig.MSG_MEASURE_DELAY, "")
    }

    fun filterConfig(context: Context) {
        val subscriptions = MmkvManager.decodeSubscriptions()
        val listId = subscriptions.map { it.first }.toList().toMutableList()
        val listRemarks = subscriptions.map { it.second.remarks }.toList().toMutableList()
        listRemarks += context.getString(R.string.filter_config_all)
        val checkedItem = if (subscriptionId.isNotEmpty()) {
            listId.indexOf(subscriptionId)
        } else {
            listRemarks.count() - 1
        }

        val ivBinding = DialogConfigFilterBinding.inflate(LayoutInflater.from(context))
        ivBinding.spSubscriptionId.adapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            listRemarks
        )
        ivBinding.spSubscriptionId.setSelection(checkedItem)
        ivBinding.etKeyword.text = Utils.getEditable(keywordFilter)
        val builder = AlertDialog.Builder(context).setView(ivBinding.root)
        builder.setTitle(R.string.title_filter_config)
        builder.setPositiveButton(R.string.tasker_setting_confirm) { dialogInterface: DialogInterface?, _: Int ->
            try {
                val position = ivBinding.spSubscriptionId.selectedItemPosition
                subscriptionId = if (listRemarks.count() - 1 == position) {
                    ""
                } else {
                    subscriptions[position].first
                }
                keywordFilter = ivBinding.etKeyword.text.toString()
                settingsStorage?.encode(AppConfig.CACHE_SUBSCRIPTION_ID, subscriptionId)
                settingsStorage?.encode(AppConfig.CACHE_KEYWORD_FILTER, keywordFilter)
                reloadServerList()

                dialogInterface?.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        builder.show()
//        AlertDialog.Builder(context)
//            .setSingleChoiceItems(listRemarks.toTypedArray(), checkedItem) { dialog, i ->
//                try {
//                    subscriptionId = if (listRemarks.count() - 1 == i) {
//                        ""
//                    } else {
//                        subscriptions[i].first
//                    }
//                    reloadServerList()
//                    dialog.dismiss()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }.show()
    }

    fun getPosition(guid: String): Int {
        serversCache.forEachIndexed { index, it ->
            if (it.guid == guid)
                return index
        }
        return -1
    }

    fun removeDuplicateServer() {
        val deleteServer = mutableListOf<String>()
        serversCache.forEachIndexed { index, it ->
            val outbound = it.config.getProxyOutbound()
            serversCache.forEachIndexed { index2, it2 ->
                if (index2 > index) {
                    val outbound2 = it2.config.getProxyOutbound()
                    if (outbound == outbound2 && !deleteServer.contains(it2.guid)) {
                        deleteServer.add(it2.guid)
                    }
                }
            }
        }
        for (it in deleteServer) {
            MmkvManager.removeServer(it)
        }
        getApplication<AngApplication>().toast(
            getApplication<AngApplication>().getString(
                R.string.title_del_duplicate_config_count,
                deleteServer.count()
            )
        )
    }

    fun copyAssets(assets: AssetManager) {
        val extFolder = Utils.userAssetPath(getApplication<AngApplication>())
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val geo = arrayOf("geosite.dat", "geoip.dat")
                assets.list("")
                    ?.filter { geo.contains(it) }
                    ?.filter { !File(extFolder, it).exists() }
                    ?.forEach {
                        val target = File(extFolder, it)
                        assets.open(it).use { input ->
                            FileOutputStream(target).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.i(
                            ANG_PACKAGE,
                            "Copied from apk assets folder to ${target.absolutePath}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(ANG_PACKAGE, "asset copy failed", e)
            }
        }
    }

    private val mMsgReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.getIntExtra("key", 0)) {
                AppConfig.MSG_STATE_RUNNING -> {
                    isRunning.value = true
                }

                AppConfig.MSG_STATE_NOT_RUNNING -> {
                    isRunning.value = false
                }

                AppConfig.MSG_STATE_START_SUCCESS -> {
//                    getApplication<AngApplication>().toast(R.string.toast_services_success)
                    isRunning.value = true
                }

                AppConfig.MSG_STATE_START_FAILURE -> {
//                    getApplication<AngApplication>().toast(R.string.toast_services_failure)
                    isRunning.value = false
                }

                AppConfig.MSG_STATE_STOP_SUCCESS -> {
                    isRunning.value = false
                }

                AppConfig.MSG_MEASURE_DELAY_SUCCESS -> {
                    updateTestResultAction.value = intent.getStringExtra("content")
                }

                AppConfig.MSG_MEASURE_CONFIG_SUCCESS -> {
                    val resultPair = intent.getSerializableExtra("content") as Pair<String, Long>
                    MmkvManager.encodeServerTestDelayMillis(resultPair.first, resultPair.second)
                    updateListAction.value = getPosition(resultPair.first)
                }
            }
        }
    }
}
