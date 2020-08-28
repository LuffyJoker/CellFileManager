package com.xgimi.filemanager.page

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.storage.StorageManager
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.xgimi.dialog.XDialog
import com.xgimi.dialog.XGMDialog
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.FileInfo
import com.xgimi.filemanager.bean.XFile
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.event.Event
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.filemanager.helper.FileOperationHelper
import com.xgimi.filemanager.helper.ResourceHelper
import com.xgimi.filemanager.interfaces.IOperationFile
import com.xgimi.filemanager.listerners.IOperationListener
import com.xgimi.filemanager.listerners.RequestAuthorizationListener
import com.xgimi.filemanager.services.DeviceLoadService
import com.xgimi.filemanager.utils.DocumentsUtil
import com.xgimi.filemanager.utils.FileUtil
import com.xgimi.samba.ShareItem
import com.xgimi.view.cell.Cell
import org.simple.eventbus.EventBus
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 16:27
 *    desc   :
 */
abstract class FileOperationPage(var context: Activity) : IOperationFile, IOperationListener,
    OperationConfigure.OnOperationConfigureChangeListener {

    companion object {
        private const val TAG = "FileOperationPage"

        /**
         * 弹窗消失延时，避免界面闪频
         */
        const val DELAY_DISPLAY_TIMER = 800L
        const val MAX_CHARACTER = 255
    }


    /**
     * progress 组件
     */
    var progress: Cell = CellCreateHelper.getProgressCell().setTag("progress")

    /**
     * 空描述
     */
    var emptyTips: Cell = CellCreateHelper.textCell(
        context.resources.getString(R.string.empty),
        R.style.font_crosshead_medium_2
    ).setTag("tips")

    /**
     * 根路径
     */
    var rootPath: String? = null

    /**
     * 当前路径
     */
    var currentPath: String? = null

    /**
     * 粘贴弹窗
     */
    var pasteDialog: XGMDialog? = null
    /**
     * 复制信息，进度的 content view
     */
//        val mPasteContentView: InuiDialogPasteViewHelper? = null
    /**
     * 是否在前台
     */
    var isActive = false

    /**
     * 选中等操作文件列表
     */
    var mOperationList = ArrayList<Any>()

    /**
     * 文件操作帮助类
     */
    var mFileOperationHelper: FileOperationHelper? = null


    private var mRequestRootPath: String? = null
    private var mRequestAuthorizationListener: RequestAuthorizationListener? = null

    protected var mHandler = Handler(Handler.Callback { msg ->
        handleMessageCallback(msg)
        false
    })

    /**
     * hander处理回调
     *
     * @param msg
     */
    protected open fun handleMessageCallback(msg: Message) {

    }

    protected var NAME: String? = null

    open fun initName(name: String) {
        NAME = name
    }

    init {
        OperationConfigure.addOnOperationConfigureChangeListener(this)
        EventBus.getDefault().register(this)
    }

    open fun selectOperationFile(name: String) {}

    override fun onCopy() {
        if (!operationFile(OperationEvent.CopyMode)) {
            requestAuthorization(object : RequestAuthorizationListener {
                override fun onRequestFail() {
                }

                override fun onRequestSuccess() {
                    refreshOperationPage()
                }
            }, currentPath)
        }
    }

    /**
     * 刷新操作界面(复制剪切文件需要添加类似window复制时的效果)
     */
    protected open fun refreshOperationPage() {}

    /**
     * 抛出文件操作事件
     *
     * @param operationMode
     */
    open fun operationFile(operationMode: Int): Boolean {
        val fileOperationEvent = OperationEvent(operationMode)
        val baseDataList: ArrayList<BaseData>? = getSelectedBaseData()
        if (baseDataList != null && baseDataList.size > 0) {
            fileOperationEvent.mOperationList.clear()
            fileOperationEvent.mOperationList.addAll(baseDataList)
        }
        OperationConfigure.setFileOperationEvent(fileOperationEvent)
        if (operationMode != OperationEvent.NewFile) {
            if (OperationConfigure.getCurrentOperationEvent() == null || OperationConfigure
                    .getCurrentOperationEvent()?.mOperationList == null || OperationConfigure
                    .getCurrentOperationEvent()?.mOperationList?.size === 0
            ) {
                ToastUtils.showShort(R.string.please_select_file)
                return true
            }
        }
        if (OperationConfigure.isSelectOperationMode) {
            setChooseMode(false)
        }
        return false
    }

    open fun setChooseMode(isSelected: Boolean) {}

    /**
     * 返回选中的BaseData
     *
     * @return
     */
    open fun getSelectedBaseData(): ArrayList<BaseData>? {
        val dataList = ArrayList<BaseData>()
        if (mOperationList != null && mOperationList.size > 0) {
            for (i in mOperationList.indices) {
                if (mOperationList[i] is BaseData) {
                    dataList.add(mOperationList[i] as BaseData)
                }
                if (mOperationList[i] is CatalogInfo) {
                    dataList.addAll((mOperationList[i] as CatalogInfo).datas)
                }
            }
        }
        return dataList
    }


    open fun requestAuthorization(
        requestAuthorizationListener: RequestAuthorizationListener,
        destDir: String?
    ) {
        var destDir = destDir
        var rootPath: String? = null
        if (destDir == null) {
            try {
                val filePath: String? =
                    OperationConfigure.getCurrentOperationEvent()?.mOperationList?.get(0)?.path
                destDir = filePath?.substring(0, filePath.lastIndexOf('/'))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (TextUtils.isEmpty(destDir)) {
            requestAuthorizationListener.onRequestSuccess()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && DocumentsUtil.isOnExtSdCard(
                Utils.getApp(),
                File(destDir)
            )
            && DocumentsUtil.checkWritableRootPath(
                Utils.getApp(),
                DocumentsUtil.getExtSdCardFolder(Utils.getApp(), destDir!!).also {
                    rootPath = it!!
                }
            )
        ) {
            onRequestAuthorization(rootPath!!, requestAuthorizationListener)
            showOpenDocumentTree(context as Activity, rootPath!!)
        } else {
            requestAuthorizationListener.onRequestSuccess()
        }
    }

    open fun showOpenDocumentTree(
        activity: Activity,
        rootPath: String
    ) {
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val sm = activity.getSystemService(
                StorageManager::class.java
            )
            val volume = sm.getStorageVolume(File(rootPath))
            if (volume != null) {
                intent = volume.createAccessIntent(null)
            }
        }
        if (intent == null) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        }
        activity.startActivityForResult(intent, DocumentsUtil.OPEN_DOCUMENT_TREE_CODE)
    }


    open fun onRequestAuthorization(rootPath: String, listener: RequestAuthorizationListener) {
        mRequestRootPath = rootPath
        mRequestAuthorizationListener = listener
    }

    var mCompositeSubscription: CompositeSubscription? = CompositeSubscription()

    open fun addSubscribe(subscription: Subscription?) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = CompositeSubscription()
        }
        mCompositeSubscription?.add(subscription)
    }

    open fun unSubscribe() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription?.unsubscribe()
            mCompositeSubscription = null
        }
    }

    open fun getCategory(): Int {
        return 0
    }

    open fun startActivity(intent: Intent) {
        context.startActivity(intent)
    }

    fun getString(resId: Int): String = context.resources.getString(resId)

    /**
     * 操作文件结束
     *
     * @param operationType 操作类型
     * @param dest          操作文件
     */
    override fun onOperationFinish(operationType: Int, dest: String?) {
        if (pasteDialog != null && pasteDialog?.isShowing!!) {
            pasteDialog?.dismiss()
        }
        OperationConfigure.setFileOperationEvent(null)
    }

    /**
     * 重命名成功
     *
     * @param original 原路径
     * @param current  现路径
     */
    override fun onRenameFileSuccess(original: String, current: String) {
        val file = File(current)
        if (file.isDirectory) {
            ResourceHelper.deleteDir(original)
            val intent = Intent(DeviceLoadService.NEW_FILE)
            val rootPath: String? = FileUtil.getRootPath(original)
            intent.putExtra(DeviceLoadService.EXTRA_VOLUME, rootPath)
            intent.putExtra(DeviceLoadService.EXTRA_FILE_PATH, current)
            Log.e(TAG, "Volume:" + rootPath + "filePath:" + current)
            context.sendBroadcast(intent)
        } else if (file.isFile && !original.startsWith("/mnt/samba")) {
            ResourceHelper.deleteFile(original)
            ResourceHelper.insertFile(rootPath, current)
        }
        EventBus.getDefault().post(Event.Normal(original), Event.tag_OperationRename)
        OperationConfigure.setFileOperationEvent(null)
    }

    /**
     * 创建文件成功
     *
     * @param path
     */
    override fun onCreateNewDirectory(path: String?) {
        if (OperationConfigure.getCurrentOperationEvent() != null && OperationConfigure.getCurrentOperationEvent()?.operationType === OperationEvent.NewFile) {
            OperationConfigure.setFileOperationEvent(null)
        }
    }

    /**
     * 解压成功
     *
     * @param path
     */
    override fun onDecompressSuccess(path: String?) {}

    /**
     * 是否存在待粘贴的文件
     *
     * @return
     */
    open fun hasPasteFile(): Boolean {
        val event: OperationEvent? = OperationConfigure.getCurrentOperationEvent()
        return event != null && (event.operationType == OperationEvent.CopyMode || event.operationType == OperationEvent.CutMode)
    }

    override fun onOperationConfigureChange(configureType: Int) {}

    /**
     * 剪切文件
     */
    override fun onCutFile() {
        if (!operationFile(OperationEvent.CutMode)) {
            requestAuthorization(object : RequestAuthorizationListener {
                override fun onRequestSuccess() {
                    refreshOperationPage()
                }

                override fun onRequestFail() {}
            }, currentPath)
        }
    }

    /**
     * 删除文件
     */
    override fun onDelFile() {
        if (!operationFile(OperationEvent.DelFile)) {
            requestAuthorization(object : RequestAuthorizationListener {
                override fun onRequestSuccess() {
                    XGMDialog(context).Builder()
                        .setTitle(getString(R.string.del_history_dialog_notice))
                        .setDesc(getString(R.string.file_operation_del_file))
                        .addButton(
                            getString(R.string.dialog_right),
                            R.style.btn_commonly_medium_default,
                            0
                        ) { integer: Int?, xDialog: XDialog? ->
                            if (OperationConfigure.getCurrentOperationEvent() != null) {
                                val selectedList: ArrayList<BaseData>? =
                                    OperationConfigure.getCurrentOperationEvent()?.mOperationList
                                if (selectedList != null && selectedList.size > 0) {
                                    val fileInfoList: ArrayList<FileInfo> =
                                        ArrayList<FileInfo>()
                                    for (selectedItem in selectedList) {
                                        val path = selectedItem.path
                                        if (path != null) {
                                            val fileInfo = FileInfo(path)
                                            if (!StringUtils.isEmpty(fileInfo.filePath)) {
                                                fileInfoList.add(fileInfo)
                                            }
                                        }
                                    }
                                    val fileOperationHelper: FileOperationHelper =
                                        getFileOperationHelper()
                                    fileOperationHelper.deleteFiles(
                                        fileInfoList,
                                        currentPath
                                    )
                                }
                            }
                            xDialog!!.dismiss()
                            null
                        }
                        .addButton(
                            getString(R.string.dialog_left),
                            R.style.btn_commonly_medium_default,
                            0
                        ) { integer: Int?, xDialog: XDialog? ->
                            xDialog!!.dismiss()
                            null
                        }.show()
                }

                override fun onRequestFail() {}
            }, currentPath)
        }
    }

    /**
     * 粘贴文件
     */
    override fun onPasteFile() { //判断内容是否重复
        val operationEvent: OperationEvent? = OperationConfigure.getCurrentOperationEvent()
        OperationConfigure.setFileOperationEvent(null)
        if (operationEvent == null) {
            ToastUtils.showShort(R.string.notice_copy_file)
            return
        }
        val destDir = currentPath
        //判断当前磁盘是否可用
        if (StringUtils.isEmpty(destDir)) {
            ToastUtils.showShort(R.string.illegal_path)
            return
        }
        requestAuthorization(object : RequestAuthorizationListener {
            override fun onRequestSuccess() { //判断当前磁盘是否可用
                val file = File(destDir)
                if (!DocumentsUtil.canWrite(context, file)) {
                    ToastUtils.showShort(R.string.read_only_file_system)
                    return
                }
                val selectedList = operationEvent.mOperationList
                if (selectedList == null || selectedList.size == 0) {
                    ToastUtils.showShort(R.string.notice_copy_file)
                    return
                }
                val notExist =
                    ArrayList<String?>()
                val fileInfoList = ArrayList<FileInfo>()
                val item: ShareItem? = selectedList[0].shareItem
                if (item != null) {
                    onPasteSambaFile(item, destDir!!)
                    return
                }
                for (selectedItem in selectedList) {
                    try {
                        val path = selectedItem.path
                        val fileInfo = FileInfo(path)
                        //复制文件是否存在
                        if (StringUtils.isEmpty(fileInfo.filePath)) {
                            notExist.add(path)
                            continue
                        }
                        //是否是递归复制
                        if (destDir!!.contains(fileInfo.filePath!!)) {
                            continue
                        }
                        fileInfoList.add(fileInfo)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                //显示提示对话框
                if (notExist.size == 0) {
                    if (fileInfoList.size == 0) {
                        return
                    }
                    var title = 0
                    if (operationEvent.operationType == OperationEvent.CopyMode) {
                        title = R.string.copying
                    } else if (operationEvent.operationType == OperationEvent.CutMode) {
                        title = R.string.cutting
                    }
                    onPasteStart(context.getResources().getString(title))
                    val fileOperationHelper = getFileOperationHelper()
                    if (operationEvent.operationType == OperationEvent.CopyMode) {
                        Constants.CancelCopy = false
                        fileOperationHelper.pasteFiles(fileInfoList, destDir!!)
                    }
                    if (operationEvent.operationType == OperationEvent.CutMode) {
                        Constants.CancelCopy = false
                        fileOperationHelper.moveFiles(fileInfoList, destDir)
                    }
                } else { //显示错误提示
                    notExist.size
                    ToastUtils.showShort(R.string.file_operation_not_exist)
                }
            }

            override fun onRequestFail() {}
        }, destDir)
    }

    open fun onPasteSambaFile(item: ShareItem, path: String) {
        onPasteStart(getString(R.string.copying))
        val fileOperationHelper = getFileOperationHelper()
        Constants.CancelCopy = false
        fileOperationHelper.pasteSambaFile(item, path)
    }

    override fun onPasteStart(title: String?) {
        pasteDialog = XGMDialog(context)
//        mPasteContentView = InuiDialogPasteViewHelper()
//        pasteDialog.Builder()
//            .setTitle(title)
//            .setContentViewStub(R.layout.operation_file_paste) { view: View? ->
//                mPasteContentView.initINUIPasteHelper(view)
//                null
//            }
//            .addButton(
//                getString(R.string.hidedialog),
//                R.style.btn_commonly_medium_default,
//                0
//            ) { integer: Int?, xDialog: XDialog? ->
//                xDialog!!.dismiss()
//                null
//            }
//            .addButton(
//                getString(R.string.dialog_left),
//                R.style.btn_commonly_medium_default,
//                0
//            ) { integer: Int?, xDialog: XDialog? ->
//                Constants.CancelCopy = true
//                xDialog!!.dismiss()
//                null
//            }.show()
    }

    /**
     * 粘贴进度变化
     *
     * @param orginal
     * @param dest
     * @param count
     * @param length
     */
    override fun onPasteProgressChange(
        orginal: String?, dest: String?, count: Long,
        length: Long
    ) {
        if (pasteDialog != null && pasteDialog!!.isShowing) {
            context.runOnUiThread(Runnable {
                Log.d(TAG, "count :$count,length:$length")
                // TODO: 2020/8/20 此处需考虑处于后台的粘贴情况 
//                mPasteContentView.updateProgressInfo(
//                    context,
//                    orginal,
//                    dest,
//                    count,
//                    length
//                )
            })
        }
    }

    /**
     * 粘贴文件成功
     *
     * @param dir
     * @param current
     * @param length
     */
    override fun onPasteFileSuccess(
        dir: Boolean,
        current: String,
        length: Long
    ) {
        if (!dir && !current.startsWith("/mnt/samba")) {
            ResourceHelper.insertFile(rootPath, current)
        }
    }

    /**
     * 粘贴文件失败
     *
     * @param dir
     * @param path
     */
    override fun onPasteFileFailure(
        dir: Boolean,
        path: String?,
        errorCode: Int
    ) {
        if (pasteDialog != null && pasteDialog!!.isShowing) {
            pasteDialog!!.dismiss()
        }
        val runnable = Runnable {
            try {
                if (errorCode == IOperationListener.ON_LACK_STORAGE_SPACE) {
                    ToastUtils.showShort(R.string.lack_storage_space)
                } else if (errorCode == IOperationListener.ON_FILE_NOT_EXIST) {
                    ToastUtils.showShort(R.string.file_not_exist)
                } else {
                    ToastUtils.showShort(R.string.error_in_file_paste)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable)
            mHandler.post(runnable)
        }
    }

    /**
     * 删除文件成功
     *
     * @param dir
     * @param orginal
     * @param length
     */
    override fun onDelFileSuccess(dir: Boolean, orginal: String, length: Long) {
        if (!dir && !orginal.startsWith("/mnt/samba")) {
            ResourceHelper.deleteFile(orginal)
            EventBus.getDefault()
                .post(Event.Normal(orginal), Event.tag_OperationRename)
        }
    }

    /**
     * 删除文件失败
     *
     * @param dir
     * @param path
     */
    override fun onDelFileFailure(dir: Boolean, path: String?) {}

    /**
     * 创建文件
     */
    override fun onCreateFile() {
        operationFile(OperationEvent.NewFile)
        val destDir = currentPath
        if (destDir == null || "" == destDir) {
            ToastUtils.showShort(R.string.illegal_path)
            return
        }
        requestAuthorization(object : RequestAuthorizationListener {
            override fun onRequestSuccess() {
                val file = XFile(context, destDir)
                if (!file.canWrite()) {
                    ToastUtils.showShort(R.string.read_only_file_system)
                    return
                }
//                InuiIconEditTextDialogBuilder(context)
//                    .setTitle(getString(R.string.new_dir))
//                    .setBlurBgView(context.window.decorView)
//                    .setIcon(R.drawable.ic_icon_explorer_sharing_folder)
//                    .setGMUIEditListener(object : GMUIEditListener() {
//                        fun onComplete(
//                            dialog: GMUIDialog,
//                            vararg texts: String
//                        ) {
//                            val text = texts[0]
//                            if (text.length > MAX_CHARACTER) {
//                                ToastUtils.showShort(R.string.name_too_long)
//                                return
//                            }
//                            val fileOperationHelper = getFileOperationHelper()
//                            if (fileOperationHelper.createNewDir(context, texts[0], destDir)) {
//                                KeyboardUtils.hideSoftInput(dialog.getCurrentFocus())
//                                selectOperationFile(texts[0])
//                                if (mHandler != null) {
//                                    mHandler.postDelayed(
//                                        { dialog.dismiss() },
//                                        DELAY_DISPLAY_TIMER
//                                    )
//                                }
//                            }
//                        }
//                    })
//                    .show()
            }

            override fun onRequestFail() {}
        }, destDir)
    }

    /**
     * 重命名
     */
    override fun onRename() { // TODO Auto-generated method stub
        operationFile(OperationEvent.Rename)
        val currentOperationEvent: OperationEvent? = OperationConfigure.getCurrentOperationEvent()
        if (currentOperationEvent?.mOperationList == null || currentOperationEvent.mOperationList.size == 0) {
            return
        }
        val filePath = currentOperationEvent.mOperationList[0].path
        requestAuthorization(object : RequestAuthorizationListener {
            override fun onRequestSuccess() {
                val fileOperationHelper = getFileOperationHelper()
//                InuiIconEditTextDialogBuilder(context)
//                    .setTitle(R.string.rename)
//                    .setText(currentOperationEvent.mOperationList[0].name)
//                    .setIcon(
//                        IconHelper.getCategoryIcon(
//                            currentOperationEvent.mOperationList[0].category,
//                            currentOperationEvent.mOperationList[0].path
//                        )
//                    )
//                    .setBlurBgView(context.window.decorView)
//                    .setGMUIEditListener(object : GMUIEditListener() {
//                        fun onComplete(
//                            dialog: GMUIDialog,
//                            vararg texts: String?
//                        ) {
//                            if (fileOperationHelper.renameFile(context, filePath!!, texts[0]!!)) {
//                                selectOperationFile(texts[0]!!)
//                                if (mHandler != null) {
//                                    mHandler.postDelayed(
//                                        { dialog.dismiss() },
//                                        DELAY_DISPLAY_TIMER
//                                    )
//                                }
//                            }
//                        }
//                    })
//                    .show()
            }

            override fun onRequestFail() {}
        }, filePath)
    }

    /**
     * 重命名失败
     *
     * @param orginal 原始路径
     */
    override fun onRenameFileFailure(orginal: String?) {}

    open fun getFileOperationHelper(): FileOperationHelper {
        mFileOperationHelper?.destroy()
        mFileOperationHelper = FileOperationHelper(context, this)
        return mFileOperationHelper!!
    }

}