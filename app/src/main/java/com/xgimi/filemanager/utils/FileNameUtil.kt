package com.xgimi.filemanager.utils

import java.util.regex.Pattern

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 16:27
 *    desc   :
 */
object FileNameUtil {
    /**
     * 检测文件名是否合法，文件命名规则遵循 Linux 文件命名规范
     *
     * @param fileName
     * @return
     */
    fun isValidFileName(fileName: String?): Boolean {
        if (fileName == null) {
            return false
        }
        if (fileName.length == 1) {
            return isNumeric(fileName) || isChar(
                fileName
            ) || isChinese(fileName)
        }
        return if (fileName.length > 255) {
            false
        } else {
            fileName.matches(Regex("[^\\s\\\\/:\\*\\?\\\"<>\\|~!@#\\$%\\^&\\`\\+](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|~!@#\\$%\\^&\\`\\+])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.~!@#\\$%\\^&\\`\\+]$"))
        }
    }

    private fun isNumeric(str: String?): Boolean {
        val pattern = Pattern.compile("[0-9]*")
        val isNum = pattern.matcher(str)
        return isNum.matches()
    }

    private fun isChar(str: String): Boolean {
        val reg = "[a-zA-Z]"
        return str.matches(Regex(reg))
    }

    private fun isChinese(str: String): Boolean {
        return str.matches(Regex("[\\u4e00-\\u9fbb]+"))
    }
}