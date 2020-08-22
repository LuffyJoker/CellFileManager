package com.xgimi.filemanager.page

import android.app.Activity
import android.os.Message
import android.view.KeyEvent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.filemanager.helper.MountHelper
import com.xgimi.gimiskin.sdk.SkinEngine.getColor
import com.xgimi.samba.SmbDevice
import com.xgimi.samba.search.SmbSearcher
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.component.ColorComponent
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout
import com.xgimi.view.cell.utils.SimpleFocusAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rx.functions.Action1

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/22 11:44
 *    desc   :
 */
class AddSambaDevicePage(context: Activity) : BasePage(context), SmbSearcher.OnSmbSearchListener {

    val root: Cell = Cell(LinearLayout(LinearLayout.VERTICAL, true))
    var container: Cell =
        Cell(LinearLayout(LinearLayout.VERTICAL, Constants.GROUP_SIZE, 72, 8)).apply {
            addComponent(ColorComponent().setColor(getColor(R.color.color_bg_pure_0)))
        }.setPadding(96)
            .setMask(true)
            .setFocusAdapter(SimpleFocusAdapter().serial(true, false, true, false))

    private val smbSearcher = SmbSearcher(context, this)
    private var isSearching = false
    private val mSearchResult = mutableListOf<DeviceInfo>()
    private val localIp: String = NetworkUtils.getIPAddress(true)

    private val onClickListener: CellEvent.OnClickListener = CellEvent.OnClickListener { p0 ->
        var des = p0.holder as String
        if (des == context.resources.getString(R.string.refresh)) { // 刷新
            ToastUtils.showShort(context.resources.getString(R.string.refresh))

            if (!isSearching) {
                mSearchResult.clear()
                // 清空列表
                if (!container.isEmpty) {
                    container.removeAllCells()
                }
                isSearching = true
                searchSambaDevice()
            } else {
                ToastUtils.showShort(R.string.scanning_device)
            }

        } else { // 手动连接
            ToastUtils.showShort(context.resources.getString(R.string.manual_connect))
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showShort(R.string.please_link_network)
            } else {
                val msg: Message = mHandler.obtainMessage()
                msg.what = Constants.ToConnectSamba
                msg.arg1 = 0
                mHandler.sendMessage(msg)
            }
        }
    }

    private val longPressListener = object : CellEvent.OnLongPressListener {
        override fun onLongPress(cell: Cell, event: Int): Boolean {
            if (event == KeyEvent.KEYCODE_DPAD_CENTER) {
                return true
            }
            return false
        }
    }


    init {
        root.addCell(
            CellCreateHelper.getTitleCell(
                context.resources.getString(R.string.add_network_device),
                onClickListener
            ),
            LinearLayout.Params(
                LinearLayout.Params.FULL,
                LinearLayout.Params.WRAP,
                Gravity.TOP
            ).setMargin(96, 96, 96, 0)
        )
        root.addCell(
            container,
            LinearLayout.Params(
                LinearLayout.Params.FULL,
                LinearLayout.Params.FULL
            ).setMarginTop(190)
        )
    }

    fun loadData() {
        isSearching = true
        searchSambaDevice()
    }

    private fun searchSambaDevice() {
        if (!NetworkUtils.isConnected()) {
            // 显示无网络  R.string.network_notlink
        } else {
            smbSearcher.startSearchSmbDevice()
            // 显示设备扫描中 progress R.string.searching_device
        }
    }

    override fun onSearch(status: Int, device: SmbDevice?) {
        mHandler.post(Runnable {
            when {
                SmbSearcher.OnSmbSearchListener.UPDATE_DEVICE_ADD === status -> {
                    val deviceInfo = DeviceInfo(device!!)
                    val ip = deviceInfo.ip
                    LogUtils.e("ping onDeviceAdd=$ip")
                    if (!StringUtils.isEmpty(localIp) && localIp == ip) {
                        return@Runnable
                    }
                    mSearchResult.add(deviceInfo)
                    // insertData , 更新列表
                    container.addCell(
                        CellCreateHelper.getItemCell(
                            deviceInfo,
                            CellEvent.OnClickListener {
                                if (it.holder is DeviceInfo) {
                                    val currentChoose = (it.holder as DeviceInfo).ip
                                    val msg: Message = mHandler.obtainMessage()
                                    msg.what = Constants.ToConnectSamba
                                    msg.obj = currentChoose
                                    msg.arg1 = 1
                                    mHandler.sendMessage(msg)
                                }
                            },
                            CellEvent.OnLongPressListener { p0, p1 ->


                                false
                            },
                            false
                        )
                    )
                }
                SmbSearcher.OnSmbSearchListener.UPDATE_DEVICE_DONE === status -> {
                    isSearching = false
                    // 隐藏加载 progress
                    ToastUtils.showShort(R.string.samba_scan_over)
                }
                else -> {
                    isSearching = false
                    // 隐藏加载 progress
                }
            }
        })
    }

    override fun handleMessageCallback(msg: Message) {
        super.handleMessageCallback(msg)
        when (msg.what) {
            Constants.ToConnectSamba ->  //如果ip不为空尝试自动连接
                if (msg.obj != null) {
                    LogUtils.i("JSmb", "try to mount")
                    mountSamba(msg.obj as String, "", "", true)
                } else {
//                    showAddDeviceView(null)
                }
            else -> {
            }
        }
    }

    fun stop() {
        smbSearcher.stop()
    }

    /**
     * 挂载samba
     *
     * @param ip
     * @param user
     * @param passworld
     */
    private fun mountSamba(
        ip: String,
        user: String,
        passworld: String,
        isTry: Boolean
    ) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            withContext(Dispatchers.Main) {
//
//            }
//        }
    }

}