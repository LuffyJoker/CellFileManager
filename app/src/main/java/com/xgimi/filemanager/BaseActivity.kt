package com.xgimi.filemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.xgimi.collectionsdk.DataReporter.appop
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.listerners.OnMenuClickListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.popwin.XgimiMenuPop
import com.xgimi.view.cell.CellView
import kotlinx.coroutines.cancel
import org.simple.eventbus.EventBus
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 10:56
 *    desc   :
 */
abstract class BaseActivity : IBaseActivity() {

    companion object {
        const val FROM = "filemanager"
    }

    val cellView: CellView by lazy {
        CellView(this)
    }

    var isShowing = false
    var hoverContainer: View? = null
    protected var mXgimiMenuPopWindow: XgimiMenuPop? = null
    protected var mXgimiMenuItem: XgimiMenuItem? = null
    private var isActive = false

    /**
     * 配置菜单弹窗是否可显示
     */
    private var canShowMenu = false
    var rootView: FrameLayout? = null
    var mContext: Context? = null
    /**
     * 上一次响应按键时间
     */
    var mOldTime: Long = 0
    /**
     * 按键重复次数
     */
    val KEY_REPEAT_COUNT = 1
    /**
     * 按键时间间隔
     */
    val KEY_TIME_INTERVAL = 150

    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        setWindowSkinBackgroundColor(R.color.color_bg_pure_0)
        mContext = this
        registerBroadcast()
        EventBus.getDefault().register(this)
        cellView.init(this)
        setContentView(cellView)
        createCell()
        initView(arg0)
        initData()
    }

    abstract fun createCell()

    abstract fun initData()

    abstract fun initView(savedInstanceState: Bundle?)

    private fun registerBroadcast() {
        val intentFilter = IntentFilter("com.xgimi.mXgimiMenuView.mutex")
        registerReceiver(mutexReceiver, intentFilter)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction("com.xgimi.settings.changelanguage")
        registerReceiver(netStateChange, filter)
    }

    private fun sendShowMenuBroadcast() {
        val sIntent = Intent("com.xgimi.mXgimiMenuView.mutex")
        sIntent.putExtra("from", FROM)
        sendBroadcast(sIntent)
    }

    override fun onResume() {
        appop.onResume(this)
        isActive = true
        super.onResume()
    }

    open fun initMenu(isEdit: Boolean) {
        mXgimiMenuItem = XgimiMenuItem()
    }

    fun isMenuShowing(): Boolean {
        return mXgimiMenuPopWindow?.isShowing!!
    }

    fun setEnableMenu(enable: Boolean) {
        canShowMenu = enable
        if (mXgimiMenuPopWindow == null && enable) {
            mXgimiMenuPopWindow = XgimiMenuPop(this)
        }
    }

    /**
     * 菜单栏点击监听
     *
     * @param listener
     */
    fun setOnMenuClickListener(listener: OnMenuClickListener?) {
        mXgimiMenuPopWindow?.setOnMenuClickListener(listener)
    }

    fun showMenu() {
        showMenu(false)
    }

    private fun showMenu(isEdit: Boolean) {
        if (mXgimiMenuPopWindow == null || mXgimiMenuPopWindow?.isShowing!! || !canShowMenu) {
            return
        }
        sendShowMenuBroadcast()
        initMenu(isEdit)
        mXgimiMenuPopWindow?.setMenus(mXgimiMenuItem)
        mXgimiMenuPopWindow?.show()
    }

    fun hideMenu() {
        if (mXgimiMenuPopWindow != null && mXgimiMenuPopWindow?.isShowing()!!) {
            mXgimiMenuPopWindow?.hide()
        }
    }


    override fun onStop() {
        super.onStop()
        isActive = false
        appop.onStop(this)
    }

    fun fixInputMethodManagerLeak(destContext: Context?) {
        if (destContext == null) {
            return
        }
        val imm =
            destContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
        val arr =
            arrayOf("mCurRootView", "mServedView", "mNextServedView")
        var f: Field? = null
        var obj_get: Any? = null
        for (i in arr.indices) {
            val param = arr[i]
            try {
                f = imm.javaClass.getDeclaredField(param)
                if (f.isAccessible == false) {
                    f.isAccessible = true
                } // author: sodino mail:sodino@qq.com
                obj_get = f[imm]
                if (obj_get != null && obj_get is View) {
                    if (obj_get.context === destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f[imm] = null // 置空，破坏掉path to gc节点
                    } else {
                        break
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        mXgimiMenuPopWindow?.destroy()
        unregisterReceiver(netStateChange)
        unregisterReceiver(mutexReceiver)
        EventBus.getDefault().unregister(this)
//        ImgLoadUtil.clearMemory(this)
        fixInputMethodManagerLeak(this)
        super.onDestroy()
        cancel()
    }

    private val mutexReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.xgimi.mXgimiMenuView.mutex") {
                val from = intent.getStringExtra("from")
                //如果不是自己发的广播，就隐藏菜单
                if (!TextUtils.isEmpty(from) && from != "filemanager") { //隐藏菜单
                    hideMenu()
                }
            }
        }
    }
    private val netStateChange: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent == null) {
                return
            }
            val action = intent.action
            if (ConnectivityManager.CONNECTIVITY_ACTION == action) { // onNetWorkChange(NetworkUtils.isAvailable());
            } else if ("com.xgimi.settings.changelanguage" == action) {
                finish()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_MENU) {
                if (!isMenuShowing() && canShowMenu) {
                    showMenu(OperationConfigure.isSelectOperationMode)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun onNetWorkChange(available: Boolean) {}

    fun getLayout(): Int {
        return R.layout.new_activity_usb_detail
    }
}