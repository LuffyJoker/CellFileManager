package com.xgimi.filemanager.page

import android.app.Activity
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.filemanager.FileManagerApplication
import com.xgimi.filemanager.R
import com.xgimi.filemanager.UsbDetailActivity
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.GMUIOperation
import com.xgimi.filemanager.config.LayoutType
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.filemanager.helper.Comparators
import com.xgimi.filemanager.helper.IntentBuilder
import com.xgimi.filemanager.listerners.IOperationListener.OnIOperationFinish
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.searcher.LocalFileSearcher
import com.xgimi.filemanager.utils.*
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.component.TextComponent
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 16:24
 *    desc   :
 */
class LocalDetailPage(
    context: Activity,
    private var mRootPath: String,
    private var mCurrentPath: String,
    private var mDeviceName: String
) : FileDetailPage(context) {

    companion object {
        private const val TAG = "LocalDetailPage"
        /**
         * 显示加载
         */
        private const val SHOW_LOADING_VIEW_DELAY = 1000

        private const val INTERNAL_STORAGE_ROOT = "/storage/emulated/0"
    }

    init {
        rootPath = mRootPath
    }

    /**
     * 搜索类
     */
    private lateinit var mUsbFileLoader: LocalFileSearcher

    private var mOperationFileName: String? = null

    /**
     * 打开的文件夹缓存，用于返回
     */
    private val mScanStack = mutableMapOf<String, Int>()

    private val showDialog = Runnable {
        if (isLoadData) {
            // todo 显示加载 progress
        }
    }

    fun isFileStackEmpty(): Boolean {
        return mScanStack.isEmpty()
    }

    override fun initEventAndData() {
        super.initEventAndData()
        loadData()
    }

    override fun fillFileContent() {

    }

    override fun getTitle(): String = mDeviceName
    override fun getPath(): String = mCurrentPath

    private fun loadData() {
        upDateCurrentPathView(mCurrentPath)
        startScan(mCurrentPath)
    }

    override fun handleMessageCallback(msg: Message) {
        super.handleMessageCallback(msg)
        when (msg.what) {
            OnIOperationFinish -> {
                val dest = msg.obj as String
                if (!StringUtils.isEmpty(dest) && dest == getCurrentPath()) {
                    updateList()
                }
            }
            LocalFileSearcher.SearchLocalFileDone -> onSearchLocalFileDone()
            else -> {
            }
        }
    }

    override fun selectOperationFile(name: String) {
        super.selectOperationFile(name)
        mOperationFileName = name
        if (!TextUtils.isEmpty(mOperationFileName) && fileLists != null) {
            for (i in 0 until fileLists.size) {
                if (name == fileLists[i].name) {
//                    setSelection(i, true) 选中状态
                    mOperationFileName = null
                    return
                }
            }
        }
    }

    /**
     * 加载数据完成
     */
    private fun onSearchLocalFileDone() {
        hideLoadingView()
        isLoadData = false
        if (mUsbFileLoader.getDataList() == null || mUsbFileLoader.getDataList().size === 0) {
            // todo 此处显示内容为空 getString(R.string.emptycontentnotice)
        }

        if (fileLists != null && mUsbFileLoader.getDataList() != null && fileLists.size === mUsbFileLoader.getDataList().size && fileLists == mUsbFileLoader.getDataList()) {
            return
        }
        clearRefreshOperationPage()
        //        isResumeToStartScan = false;
        mCurrentPath = mUsbFileLoader.getCurrentPath()!!
        upDateCurrentPathView(mCurrentPath) // 更新路径
        fileLists.clear()
        fileLists.addAll(mUsbFileLoader.getDataList())
        if (fileLists.isNotEmpty()) { //过滤samba文件
            filterSambaFile()
            updateCellContainer(fileLists) // 更新容器内容
            when {
                !TextUtils.isEmpty(mOperationFileName) -> {
                    selectOperationFile(mOperationFileName!!)
                    mOperationFileName = null
                }
                mScanStack.containsKey(mCurrentPath) -> {
                    val position = mScanStack[mCurrentPath]
                    setInitSelection(position!!)
                    mScanStack.remove(mCurrentPath)
                }
                isOpenDir -> {
                    setInitSelection(-2)
                }
            }
        } else {
            updateCellContainer(fileLists)
        }
        isOpenDir = false
    }

    /**
     * 过滤 samba 中的文件
     */
    private fun filterSambaFile() {
        if (fileLists.isNotEmpty() && mCurrentPath!!.startsWith("/mnt/samba")) {
            val subPath = mCurrentPath!!.substring("/mnt/samba/".length)
            if (!subPath.contains("/")) {
                var hideBaseData: BaseData? = null
                for (baseData in fileLists) {
                    if (baseData.category != FileCategory.Folder.ordinal) {
                        continue
                    }
                    val childPath = baseData.path
                    val childFile = File(childPath)
                    if (!childFile.exists()) {
                        continue
                    }
                    val fileList =
                        childFile.list { dir, filename ->
                            val file = File(dir, filename)
                            if ("TJ" == filename && file.isDirectory) {
                                true
                            } else "name-id.txt" == filename && file.isFile
                        }
                    if (fileList != null && fileList.size == 2) {
                        hideBaseData = baseData
                        break
                    }
                }
                if (hideBaseData != null) {
                    fileLists.remove(hideBaseData)
                }
            }
        }
    }

    /**
     * 获取设备名称
     *
     * @return
     */
    fun getDeviceName(): String? {
        return mDeviceName
    }

    /**
     * 开启线程，扫描指定文件夹
     *
     * @param scanPath
     */
    private fun startScan(scanPath: String) {
        if (isLoadData) {
            return
        }
        LogUtils.e(UsbDetailActivity.TAG, "scanPath: $scanPath")
        mUsbFileLoader = LocalFileSearcher.Builder(context)
            .searchRootPath(rootPath)
            .searchPath(scanPath)
            .setHandler(mHandler)
            .sortMode(OperationConfigure.getSortOrder())
            .searchMode(LocalFileSearcher.SearchMode.Layer)
            .build()
        ThreadManager.execute(mUsbFileLoader)
        isLoadData = true
        if (mHandler != null) {
            mHandler.removeCallbacks(showDialog)
            mHandler.postDelayed(showDialog, SHOW_LOADING_VIEW_DELAY.toLong())
        }
    }

    /**
     * 更新当前列表
     */
    fun updateList() {
        startScan(mCurrentPath!!)
    }

    fun showFolder(folderPath: String?) {
        startScan(mCurrentPath!!)
    }

    fun onDestroy() {
        if (mUsbFileLoader != null) {
            mUsbFileLoader.stopSearch()
        }
    }

    override fun onOperationFinish(operationType: Int, dest: String?) {
        super.onOperationFinish(operationType, dest)
        Log.e(TAG, "onIOperationFinish:" + operationType + "dest:" + dest)
        if (mHandler != null) {
            val msg = mHandler.obtainMessage()
            msg.arg1 = operationType
            msg.obj = dest
            msg.what = OnIOperationFinish
            mHandler.sendMessage(msg)
        }
    }

    override fun onRenameFileSuccess(original: String, current: String) {
        sendOnIOperationFinish(FileUtil.getParentDirPath(current)!!)
        super.onRenameFileSuccess(original, current)

    }

    override fun onCreateNewDirectory(path: String?) {
        if (OperationConfigure.getCurrentOperationEvent() != null && OperationConfigure.getCurrentOperationEvent()?.operationType === OperationEvent.NewFile) {
            sendOnIOperationFinish(FileUtil.getParentDirPath(path)!!)
        }
        super.onCreateNewDirectory(path)
    }

    override fun onDecompressSuccess(path: String?) {
        sendOnIOperationFinish(FileUtil.getParentDirPath(path)!!)
        super.onDecompressSuccess(path)
    }

    private fun sendOnIOperationFinish(currentPath: String) {
        if (mHandler != null) {
            val msg = mHandler.obtainMessage()
            msg.what = OnIOperationFinish
            msg.obj = currentPath
            mHandler.sendMessage(msg)
        }
    }

    override fun onBackPressed(): Boolean {
        if (super.onBackPressed()) {
            return true
        }
        if (rootPath == null) {
            return false
        }
        if (mCurrentPath == null) {
            return false
        }
        if (!rootPath.equals(mCurrentPath)) {
            val file = File(mCurrentPath)
            if (file.exists()) {
                startScan(file.parent)
            }
            return true
        }
        return false
    }

    override fun hideLoadingView() {
        if (mHandler != null) {
            mHandler.removeCallbacks(showDialog)
        }
        super.hideLoadingView()
    }

    private var isOpenDir = false

    private val onClickListener: CellEvent.OnClickListener = object : CellEvent.OnClickListener {
        override fun onClick(p0: Cell?) {
            val baseData1 = p0?.holder as BaseData
            val childPath = baseData1.path
            if (!TextUtils.isEmpty(childPath)) {
                val file = File(childPath)
                if (file.exists()) {
                    val category = baseData1.category
                    FileManagerApplication.getInstance().reportFileCevent(childPath!!)
                    if (category == FileCategory.Folder.ordinal) { //打开文件夹
                        mScanStack[mCurrentPath!!] = getPosition(childPath!!)
                        isOpenDir = true
                        startScan(childPath)
                    } else if (category == FileCategory.Video.ordinal) { //打开视频
                        MediaOpenUtil.playVideoList(context, fileLists, baseData1)
                    } else if (baseData1.category == FileCategory.Picture.ordinal) { //打开图片
                        MediaOpenUtil.playPictureList(context, fileLists, baseData1)
                    } else if (baseData1.category == FileCategory.Music.ordinal) { //打开音乐
                        MediaOpenUtil.playMusicList(context, fileLists, baseData1)
                    } else {
                        if (file.isFile) {
                            if (childPath!!.toLowerCase().endsWith(".zip") || childPath.toLowerCase().endsWith(
                                    ".rar"
                                )
                            ) {
                                // todo 打开压缩文件
                            } else if (childPath.toLowerCase().endsWith(".swf")) {
                                IntentBuilder.viewFileByVideo(context, childPath)
                            } else if (childPath.endsWith(".iso") || childPath.endsWith(".ISO")) { //打开iso
                                val playPath: String? = ISOMountUtil.getIsoLargeFilePath(childPath)
                                if (playPath == null) {
                                    ToastUtils.showShort(R.string.play_iso_fail)
                                    return
                                }
                                val pos = playPath.lastIndexOf(".")
                                if (pos > 0) {
                                    val extension = playPath.toLowerCase().substring(pos)
                                    if (FileCategoryUtil.isVideoFile(extension)) {
                                        val playData = BaseData()
                                        playData.name = baseData1.name
                                        playData.path = playPath
                                        MediaOpenUtil.playVideo(context, playData, baseData1.path!!)
                                        return
                                    }
                                }
                                ToastUtils.showShort(R.string.play_iso_fail)
                            } else {
                                IntentBuilder.viewFile(context, childPath)
                            }
                        }
                    }
                } else {
                    ToastUtils.showShort(R.string.file_operation_not_exist)
                }
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

    override fun getGMUIOperation(): List<GMUIOperation>? {
        if (isSamba()) {
            val mOperations: MutableList<GMUIOperation> =
                ArrayList<GMUIOperation>()
            mOperations.add(GMUIOperation(OperationEvent.CopyMode, getString(R.string.copy)))
            return mOperations
        }
        return super.getGMUIOperation()
    }

    override fun initMenus(): XgimiMenuItem? {
        val menu = XgimiMenuItem()
        if (context == null) {
            return menu
        }
        if (isSamba()) {
            menu.addMenu(
                XgimiMenuItem(
                    OperationEvent.ChangeLayoutType,
                    if (OperationConfigure.getLayoutType() === LayoutType.GRID_LAYOUT.ordinal) getString(
                        R.string.list_mode
                    ) else getString(R.string.grid_mode)
                )
            )
            menu.addMenu(
                XgimiMenuItem(
                    OperationEvent.Sortorder,
                    if (OperationConfigure.getSortOrder() === Comparators.SortMode.NAME.ordinal) getString(
                        R.string.time_sort
                    ) else getString(R.string.name_sort)
                )
            )
            menu.addMenu(XgimiMenuItem(OperationEvent.SearchFile, getString(R.string.searching)))
        } else {
            if (hasPasteFile()) {
                menu.addMenu(XgimiMenuItem(OperationEvent.Paste, getString(R.string.paste)))
            }
            menu.addMenu(
                XgimiMenuItem(
                    OperationEvent.Sortorder,
                    if (OperationConfigure.getSortOrder() === Comparators.SortMode.NAME.ordinal) getString(
                        R.string.time_sort
                    ) else getString(R.string.name_sort)
                )
            )
            val superList = super.initMenus()!!.subMenus
            superList.add(
                1,
                XgimiMenuItem(OperationEvent.SearchFile, getString(R.string.searching))
            )
            menu.subMenus.addAll(superList)
        }
        return menu
    }

    override fun onOperationConfigureChange(configureType: Int) {
        super.onOperationConfigureChange(configureType)
        if (configureType == OperationConfigure.OPERATION_CONFIGURE_SORT_ORDER) {
            startScan(mCurrentPath!!)
        }
    }

    override fun onMenuClicked(menu: XgimiMenuItem): Boolean {
        if (menu.operationType === OperationEvent.Sortorder) {
            OperationConfigure.changeSortOrder()
        }
        return super.onMenuClicked(menu)
    }

    private fun isSamba(): Boolean {
        return !TextUtils.isDigitsOnly(rootPath) && rootPath!!.startsWith("/mnt/samba")
    }

    /**
     * 更新当前路径 text
     */
    private fun upDateCurrentPathView(path: String) {
        var showPath = path
        if (!TextUtils.isEmpty(showPath)) {
            if (showPath.contains(rootPath!!)) {
                showPath = showPath.replace(rootPath!!, "")
            }
            if (!TextUtils.isEmpty(showPath)) {
                val length = showPath.length
                if (length >= 50) {
                    showPath = "..." + showPath.substring(length - 50)
                }
            }
        }
        root.findCellByTag(CellCreateHelper.TAG_PATH)
            .findComponent(TextComponent::class.java).text = showPath
    }

    private fun updateCellContainer(fileLists: ArrayList<BaseData>) {
        if (!fileDetailCell.isEmpty) {
            fileDetailCell.removeAllCells()
        }
        fileLists.forEachIndexed { _, baseData ->
            fileDetailCell.addCell(
                CellCreateHelper.getItemCell(
                    baseData,
                    onClickListener,
                    longPressListener
                )
            )
        }
        fileDetailCell.requestFocus()
    }
}