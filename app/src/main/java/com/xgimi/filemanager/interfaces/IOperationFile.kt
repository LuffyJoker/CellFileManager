package com.xgimi.filemanager.interfaces

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 15:51
 *    desc   :
 */
interface IOperationFile {
    fun onCopy()
    fun onCutFile()
    fun onDelFile()
    fun onRename()
    fun onPasteFile()
    fun onCreateFile()
}