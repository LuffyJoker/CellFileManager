package com.xgimi.filemanager

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.filemanager.constants.Constants.NAME
import com.xgimi.filemanager.constants.Constants.PATH
import com.xgimi.filemanager.helper.MountHelper
import com.xgimi.filemanager.listerners.OnMenuClickListener
import com.xgimi.filemanager.page.LocalDetailPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UsbDetailActivity : BaseActivity() {

    private val mScanStack = mutableMapOf<String, Int>()

    private var isOpenDir = false

    companion object {
        const val TAG = "UsbDetailActivity"
    }

    private var mNotBack = false
    private lateinit var mRootPath: String
    private lateinit var mCurrentPath: String
    private lateinit var mDeviceName: String

    lateinit var page: LocalDetailPage

    override fun initView(savedInstanceState: Bundle?) {
        setEnableMenu(true)
    }

    override fun initData() {
        mRootPath = intent.getStringExtra(PATH)
        mDeviceName = intent.getStringExtra(NAME)
        mNotBack = intent.getBooleanExtra("NOTBACK", false)
        val file = File(mRootPath)
        if (file == null || !file.exists()) {
            ToastUtils.showShort(R.string.u_out)
            finish()
            return
        }
        loadDeviceInfo()
    }

    override fun createCell() {
        cellView.root = LocalDetailPage(this, mRootPath, mCurrentPath, mDeviceName).apply {
            page = this
        }.root

        // 初始化页面UI及数据
        page.initEventAndData()

        cellView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                cellView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                cellView.root.requestFocus()
            }
        })
    }

    /**
     * 加载挂载设备的信息
     */
    private fun loadDeviceInfo() {
        if (mRootPath.startsWith("/virtualsdcard")) {
            mRootPath = mRootPath.replace("/virtualsdcard", "/mnt/sdcard")
        }
        mCurrentPath = mRootPath
        var hasRootPath = false
        val mountList = MountHelper.getMountPathList()
        if (mountList.isNullOrEmpty()) {
            LogUtils.e(TAG, "mountList==null")
            return
        }
        for (mountPoint in mountList) {
            if (mCurrentPath.startsWith(mountPoint)) {
                if (!mNotBack) {
                    mRootPath = mountPoint
                }
                hasRootPath = true
                LogUtils.e(TAG, "no mountPoint$mCurrentPath")
                break
            }
        }

        if (!hasRootPath) {
            LogUtils.e(TAG, "no data show$mCurrentPath")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var localDeviceInfo = MountHelper.getDeviceInfo(this@UsbDetailActivity, mRootPath)
            // 耗时操作
            withContext(Dispatchers.Main) {
                localDeviceInfo?.deviceName?.apply {
                    mDeviceName = this
                }
            }
        }
    }

    /**
     * 初始化菜单
     */
    override fun initMenu(isEdit: Boolean) {
        super.initMenu(isEdit)
        if (page != null) {
            mXgimiMenuItem?.subMenus?.clear()
            if (!isEdit) {
                page.initMenus()?.subMenus?.let { mXgimiMenuItem?.addAllMenus(it) }
            } else {
                mXgimiMenuItem?.addAllMenus(page.getEditMenu().subMenus)
            }
        }
        setOnMenuClickListener(OnMenuClickListener { view, xgimiMenuItem ->
            val isClose: Boolean = page.onMenuClicked(xgimiMenuItem!!)
            if (isClose) {
                hideMenu()
            }
        })
//        mXgimiMenuView.setXgimiMenuListener(object : XgimiMenuListener {
//            fun onMenuStartShowing() {
//                mLocalDetailFragment.setMenuState(true)
//            }
//
//            fun onMenuStartHiding() {
//                mLocalDetailFragment.setMenuState(false)
//            }
//        })
    }

    override fun onBackPressed() {
        if (page != null && page.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

}