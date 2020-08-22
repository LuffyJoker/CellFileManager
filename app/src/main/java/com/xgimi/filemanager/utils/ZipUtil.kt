package com.xgimi.filemanager.utils

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 16:33
 *    desc   :
 */
object ZipUtil {
    /**
     * Execute a command
     * @param command command string
     * @return return code
     */
    external fun executeCommand(command: String?): Int

    /**
     * load native library
     */
    init {
        System.loadLibrary("p7zip")
    }
}