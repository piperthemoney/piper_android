package com.piperbloom.proxyvpn.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.tencent.mmkv.MMKV
import com.piperbloom.proxyvpn.R
import com.piperbloom.proxyvpn.data.SharedPrefManager
import com.piperbloom.proxyvpn.data.areVlessServersEqual
import com.piperbloom.proxyvpn.data.getCountryName
import com.piperbloom.proxyvpn.data.getCountryResourceId
import com.piperbloom.proxyvpn.databinding.ItemServerBinding
import com.piperbloom.proxyvpn.dto.ServerAffiliationInfo
import com.piperbloom.proxyvpn.extension.toast
import com.piperbloom.proxyvpn.helper.ItemTouchHelperAdapter
import com.piperbloom.proxyvpn.helper.ItemTouchHelperViewHolder
import com.piperbloom.proxyvpn.service.V2RayServiceManager
import com.piperbloom.proxyvpn.ui.fragment.HomeFragment
import com.piperbloom.proxyvpn.util.AngConfigManager
import com.piperbloom.proxyvpn.util.MmkvManager
import com.piperbloom.proxyvpn.util.Utils
import com.piperbloom.proxyvpn.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class MainRecyclerAdapter(val fragment: HomeFragment,private val mainViewModel: MainViewModel) :
    RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>(), ItemTouchHelperAdapter {

    private var selectedPosition: Int = -1

    companion object {
        private const val VIEW_TYPE_ITEM = 1
    }

//    private var mActivity: MainActivity = activity
    val context = fragment.requireContext()
    val mActivity = fragment.requireActivity()
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val subStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_SUB, MMKV.MULTI_PROCESS_MODE) }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val share_method: Array<out String> by lazy {
        mActivity.resources.getStringArray(R.array.share_method)
    }
    var isRunning = false

    override fun getItemCount() = mainViewModel.serversCache.size

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: BaseViewHolder, @SuppressLint("RecyclerView") position: Int) {

//        val item = SharedPrefManager.getServerList(context)?.get(position)

        if (position >= mainViewModel.serversCache.size) return

        if (holder is MainViewHolder) {
            val guid = mainViewModel.serversCache[position].guid
            val config = mainViewModel.serversCache[position].config

            val conf = AngConfigManager.shareConfig(guid)
            Log.d("SERVER_INFO", conf)

            val outbound = config.getProxyOutbound()
            val aff = MmkvManager.decodeServerAffiliationInfo(guid)

            holder.itemMainBinding.countryTv.text = getCountryName(config.remarks)

            val svg =
                getCountryResourceId(config.remarks)?.let {
                    SVG.getFromResource(context.resources,
                        it
                    )
                }
            if (svg != null) {
                holder.itemMainBinding.flagView.setImageDrawable(PictureDrawable(svg.renderToPicture()))
            }



//            val testDelayValue = aff?.getTestDelayString()?.replace("ms", "")?.trim()?.toIntOrNull() ?: 0

            checkSignalStrength(holder,aff,guid,position)


            if (guid == mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
                holder.itemMainBinding.cardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.white_400))
                holder.itemMainBinding.countryTv.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
            } else {
                holder.itemMainBinding.cardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.nav_color))
                holder.itemMainBinding.countryTv.setTextColor(ContextCompat.getColor(mActivity, R.color.white))
            }


            if (position == selectedPosition) {
                holder.itemMainBinding.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white_400))
                holder.itemMainBinding.countryTv.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
            } else {
                holder.itemMainBinding.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.nav_color))
                holder.itemMainBinding.countryTv.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            }


            holder.itemMainBinding.infoContainer.setOnClickListener {
                val selected = mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)

                selectedPosition = position
//                notifyDataSetChanged()

                val serverList = SharedPrefManager.getServerList(context)?.toMutableList()
                val clickedServer = serverList?.find { areVlessServersEqual(conf, it.vlessServers) }

                if (clickedServer != null) {
                    serverList.remove(clickedServer)
                    serverList.add(0, clickedServer)
                    SharedPrefManager.saveServerList(context,serverList)
                    SharedPrefManager.saveUserChooseServer(context, clickedServer)
                } else {
                    Log.d("SERVER_INFO", "NO Match")
                }


                if (guid != selected) {
                    mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
                    if (!TextUtils.isEmpty(selected)) {
                        notifyItemChanged(mainViewModel.getPosition(selected ?: ""))
                    }
                    notifyItemChanged(mainViewModel.getPosition(guid))
                    if (isRunning) {
                        Utils.stopVService(mActivity)
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                V2RayServiceManager.startV2Ray(mActivity)
                            }
                    }
                }
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedPosition(position: Int) {
        selectedPosition = position // Update selection
//        notifyDataSetChanged() // Notify the adapter to refresh views
        notifyItemChanged(selectedPosition)
    }

    private fun shareFullContent(guid: String) {
        if (AngConfigManager.shareFullContent2Clipboard(mActivity, guid) == 0) {
            mActivity.toast(R.string.toast_success)
        } else {
            mActivity.toast(R.string.toast_failure)
        }
    }


    private fun removeServer(guid: String, position: Int) {
        mainViewModel.removeServer(guid)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, mainViewModel.serversCache.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        // Always return MainViewHolder
        return MainViewHolder(
            ItemServerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
//            itemView.setBackgroundColor(0)
        }
    }

    class MainViewHolder(val itemMainBinding: ItemServerBinding) :
        BaseViewHolder(itemMainBinding.root), ItemTouchHelperViewHolder

    override fun onItemDismiss(position: Int) {
//        val guid = mainViewModel.serversCache.getOrNull(position)?.guid ?: return
//        if (guid != mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
////            mActivity.alert(R.string.del_config_comfirm) {
////                positiveButton(android.R.string.ok) {
//            mainViewModel.removeServer(guid)
//            notifyItemRemoved(position)
////                }
////                show()
////            }
//        }

        if (position < mainViewModel.serversCache.size) {
            val guid = mainViewModel.serversCache.getOrNull(position)?.guid ?: return
            if (guid != mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
                mainViewModel.removeServer(guid)
                notifyItemRemoved(position)
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        mainViewModel.swapServer(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        // position is changed, since position is used by click callbacks, need to update range
        if (toPosition > fromPosition)
            notifyItemRangeChanged(fromPosition, toPosition - fromPosition + 1)
        else
            notifyItemRangeChanged(toPosition, fromPosition - toPosition + 1)
        return true
    }

    override fun onItemMoveCompleted() {
        // do nothing
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun removeAllItems() {
//        val itemCount = mainViewModel.serversCache.size // Get the current item count
//
//        if (itemCount > 0) {
//            mainViewModel.serversCache.clear() // Clear the cache
//
//            // Notify the RecyclerView that the items have been removed
//            notifyItemRangeRemoved(0, itemCount)
//        }
//    }


    private fun checkSignalStrength(holder: MainViewHolder, aff: ServerAffiliationInfo?,guid: String,position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            // Perform signal strength checks on a background thread
            val testDelayMillis = aff?.testDelayMillis ?: 0L
            val drawableResId = when {
                testDelayMillis < 0L -> R.drawable.bad_signal
                testDelayMillis > 800 -> R.drawable.middle_signal
                else -> R.drawable.good_signal
            }

            // Set the color filter for the drawable in a way that can be accessed on the main thread
            val drawable = ContextCompat.getDrawable(holder.itemView.context, drawableResId)
            val colorFilter = when (drawableResId) {
                R.drawable.bad_signal -> ContextCompat.getColor(holder.itemView.context, R.color.bad_signal_color)
                R.drawable.middle_signal -> ContextCompat.getColor(holder.itemView.context, R.color.middle_signal_color)
                R.drawable.good_signal -> ContextCompat.getColor(holder.itemView.context, R.color.good_signal_color)
                else -> Color.TRANSPARENT // Default case
            }

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                drawable?.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN)
                holder.itemMainBinding.cellularView.setImageDrawable(drawable)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        mainViewModel.serversCache.clear()
        notifyDataSetChanged()
    }

}
