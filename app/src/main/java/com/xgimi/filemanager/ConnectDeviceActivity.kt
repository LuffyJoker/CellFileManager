package com.xgimi.filemanager

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.autoutils.AutoAdaptation
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.contentprovider.ContentData
import com.xgimi.filemanager.exceptions.ShareException
import com.xgimi.filemanager.samba.ShareClientController
import com.xgimi.samba.SmbDevice
import kotlinx.android.synthetic.main.activity_connect_device.*
import rx.Subscription
import rx.functions.Action1
import java.util.regex.Pattern

class ConnectDeviceActivity : AppCompatActivity(), View.OnFocusChangeListener {

    private var mIp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_device)
        mIp = intent.getStringExtra(EXTRA_IP)
        initView()
    }

    private fun initView() {
        AutoAdaptation.auto(this)
        pl_edit_first.onFocusChangeListener = this
        pl_edit_first.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val ip = s.toString()
                if (matchIP(ip)) {
                    getDeviceInfo(this@ConnectDeviceActivity, ip)
                }
            }

            override fun afterTextChanged(s: Editable) { //do noting
            }
        })
        pl_edit_second.onFocusChangeListener = this
        pl_edit_third.onFocusChangeListener = this
        pl_edit_third.setOnEditorActionListener(mEditorListener)
        if (mIp != null) {
            pl_edit_first.visibility = View.GONE
            title_des.text = resources.getString(R.string.connect_share_device) + ":" + mIp
        } else {
            title_des.text = getString(R.string.connect_share_device)
            val loaclip: String = NetworkUtils.getIPAddress(true)
            if (!TextUtils.isEmpty(loaclip)) {
                val ips = loaclip.split("\\.".toRegex()).toTypedArray()
                if (ips.size == 4) {
                    val str = ips[0] + "." + ips[1] + "." + ips[2] + "."
                    pl_edit_first.setText(str)
                    pl_edit_first.setSelection(str.length)
                }
            }
        }
    }

    /**
     * @param ip
     * @return
     */
    private fun matchIP(ip: String): Boolean {
        val regex =
            "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(ip)
        return matcher.matches()
    }

    private fun getDeviceInfo(
        context: Context,
        ip: String
    ) {
        var cursor: Cursor? = null
        var username = ""
        var pwd = ""
        try {
            cursor = context.contentResolver
                .query(
                    ContentData.CONTENT_URI_DEVICE,
                    null,
                    DeviceInfo.IP + "=? ",
                    arrayOf(ip),
                    null
                )
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(6)
                pwd = cursor.getString(7)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        pl_edit_second!!.setText(username)
        pl_edit_third!!.setText(pwd)
        if (!StringUtils.isEmpty(username)) {
            pl_edit_second!!.setSelection(username.length)
        }
        if (!StringUtils.isEmpty(pwd)) {
            pl_edit_third!!.setSelection(pwd.length)
        }
    }

    var isFirst = true

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val version = Build.VERSION.SDK_INT
            if (isFirst && version >= Build.VERSION_CODES.M && version < Build.VERSION_CODES.P) {
                isFirst = false
                object : Thread() {
                    override fun run() {
                        try {
                            val inst = Instrumentation()
                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }.start()
            }
        }
    }

    private val mEditorListener = OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!NetworkUtils.isConnected()) {
                    ToastUtils.showShort(R.string.please_link_network)
                } else {
                    mountSamba(
                        if (StringUtils.isEmpty(mIp)) pl_edit_first!!.text.toString() else mIp!!,
                        pl_edit_second!!.text.toString(),
                        pl_edit_third!!.text.toString()
                    )
                }
                return@OnEditorActionListener true
            }
            false
        }
    private var connectSubscription: Subscription? = null
    

    val RESULT_CODE = 1000
    val EXTRA_DEVICE = "device"
    val EXTRA_IP = "ip"

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus && v is EditText) {
            val editText = v
            val str = editText.text.toString()
            if (!TextUtils.isEmpty(str)) {
                editText.setSelection(str.length)
            }
        }
    }

    /**
     * 挂载 samba
     *
     * @param ip
     * @param user
     * @param passWorld
     */
    private fun mountSamba(ip: String, user: String, passWorld: String) {
        LogUtils.i("JSmb", "--------mountSamba--------")
        if (connectSubscription != null && connectSubscription!!.isUnsubscribed) {
            connectSubscription!!.unsubscribe()
        }
        connectSubscription = ShareClientController
            .signInSamba(ip, user, passWorld)
            ?.subscribe({ sambaDevice ->
                LogUtils.i("JSmb", "mountSamba onSuccess")
                Toast.makeText(
                    this@ConnectDeviceActivity,
                    R.string.mount_samba_success,
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent()
                intent.putExtra(EXTRA_DEVICE, DeviceInfo(sambaDevice!!))
                setResult(RESULT_CODE, intent)
                finish()
            }, { throwable ->
                var errorCode = 0
                if (throwable is ShareException) {
                    errorCode = throwable.code
                }
                when (errorCode) {
                    ShareClientController.STATUS_LOGON_FAILURE -> {
                        Toast.makeText(
                            this@ConnectDeviceActivity,
                            R.string.login_pwd_wrong,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ShareClientController.STATUS_CONNECT_TIMEOUT -> {
                        Toast.makeText(
                            this@ConnectDeviceActivity,
                            R.string.connect_timeout,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            this@ConnectDeviceActivity,
                            R.string.login_samba_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (connectSubscription != null) {
            connectSubscription!!.unsubscribe()
        }
    }
}