package com.xgimi.filemanager.utils

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.xgimi.filemanager.R
import com.xgimi.filemanager.interfaces.OnDepressInfo

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 16:30
 *    desc   :
 */
class ZipProcess {
    /*
		0 No error
		1 Warning (Non fatal error(s)). For example, one or more files were locked by some other application,
		  so they were not compressed.
		2 Fatal error
		7 Command line error
		8 Not enough memory for operation
		255 User stopped the process
	*/
    companion object {
        private val RET_SUCCESS = 0
        private val RET_WARNING = 1
        private val RET_FAULT = 2
        private val RET_COMMAND = 7
        private val RET_MEMORY = 8
        private val RET_USER_STOP = 255
    }

    var context: Context? = null
    var command: String? = null
    var destPath: String? = null
    var mListener: OnDepressInfo? = null
//    var dialog: JustProgressDialog? = null

    constructor(context: Context?, command: String?, dest: String?, listener: OnDepressInfo?) {
        this.context = context
        this.command = command
        destPath = dest
        mListener = listener
//        dialog = JustProgressDialog(context)
    }

    fun start() {
        thread.start()
//        dialog.show()
    }

    var thread: Thread = object : Thread() {
        override fun run() { // TODO Auto-generated method stub
            val ret: Int = ZipUtil.executeCommand(command)
            handler.sendEmptyMessage(ret) //send back return code
            if (ret == RET_SUCCESS) mListener?.onDepressSuccess(destPath) else mListener?.onDepressFailure()
            super.run()
        }
    }

    private val handler =
        Handler(Handler.Callback { msg ->
            // TODO Auto-generated method stub
            var retMsgId = R.string.msg_ret_success
            when (msg.what) {
                RET_SUCCESS -> retMsgId = R.string.msg_ret_success
                RET_WARNING -> retMsgId = R.string.msg_ret_warning
                RET_FAULT -> retMsgId = R.string.msg_ret_fault
                RET_COMMAND -> retMsgId = R.string.msg_ret_command
                RET_MEMORY -> retMsgId = R.string.msg_ret_memmory
                RET_USER_STOP -> retMsgId = R.string.msg_ret_user_stop
                else -> {
                }
            }
//            dialog.dismiss()
            if (msg.what == RET_SUCCESS) Toast.makeText(
                context,
                R.string.msg_ret_success,
                Toast.LENGTH_SHORT
            ).show() else Toast.makeText(
                context,
                R.string.msg_ret_failure,
                Toast.LENGTH_SHORT
            ).show()
            false
        })
}