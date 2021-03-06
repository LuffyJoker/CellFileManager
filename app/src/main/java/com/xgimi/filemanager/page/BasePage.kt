package com.xgimi.filemanager.page

import android.app.Activity
import android.content.Intent
import com.xgimi.dialog.BaseDialog
import com.xgimi.dialog.ext.baseDialog
import com.xgimi.filemanager.FileSearchActivity
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.bean.GMUIOperation
import com.xgimi.filemanager.config.LayoutType
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.IntentBuilder
import com.xgimi.filemanager.listerners.OnFileItemClickListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.samba.bean.CategoryBean
import com.xgimi.samba.bean.ShareDisk
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.layout.LinearLayout
import com.xgimi.view.cell.utils.SimpleFocusAdapter
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/14 15:28
 *    desc   :
 */
abstract class BasePage(context: Activity) : FileOperationPage(context) {


    // 文件列表容器
    var fileDetailCell =
        Cell(LinearLayout(LinearLayout.VERTICAL, Constants.GROUP_SIZE, 72, 8))
            .setPadding(96)
            .setMask(true)
            .setFocusAdapter(SimpleFocusAdapter().serial(true, false, true, false))
            .setTag("fileListContainer")

    //当前页面是否选中显示
    var isSelectedTab = false

    var headerHideEnable = false

    /**
     * 菜单是否显示
     */
    var isMenuShowed = false

    //    private val mGMUIOperationDialogBuilder: GMUIOperationDialogBuilder? = null
    private val mLastRefreshOperationPageDatas = mutableListOf<BaseData>()
    private val mCurrentRefreshOperationPageDatas = mutableListOf<BaseData>()

    /**
     * 设置item点击事件
     *
     * @param listener
     */
    open fun setOnItemClickListener(listener: OnFileItemClickListener?) {
//        mGridListView.setOnItemClickListener(listener)
    }

    open fun setOnItemSelectedListener(listener: OnFileItemClickListener?) {
//        mGridListView.setOnItemSelectedListener(listener)
    }


    /**
     * 初始化菜单
     *
     * @return
     */
    open fun initMenus(): XgimiMenuItem? {
        val menu = XgimiMenuItem()

        var mode = if (OperationConfigure.getLayoutType() == LayoutType.GRID_LAYOUT.ordinal) {
            getString(R.string.grid_mode)
        } else {
            getString(R.string.list_mode)
        }

        menu.addMenu(XgimiMenuItem(OperationEvent.ChangeLayoutType, mode))
        if (getCategory() === 0) {
            menu.addMenu(
                XgimiMenuItem(
                    OperationEvent.NewFile,
                    getString(R.string.new_dir)
                )
            )
        }
        menu.addMenu(
            XgimiMenuItem(
                OperationEvent.SelectAll,
                getString(R.string.select_all)
            )
        )
        menu.addMenu(
            XgimiMenuItem(
                OperationEvent.Multiselect,
                getString(R.string.multiselect)
            )
        )
        return menu
    }

    /**
     * 获取编辑菜单
     *
     * @return
     */
    open fun getEditMenu(): XgimiMenuItem {
        val menu =
            XgimiMenuItem(OperationEvent.OPERATION_NULL, getString(R.string.edit))
        menu.addMenu(
            XgimiMenuItem(
                OperationEvent.CutMode,
                getString(R.string.cut)
            )
        )
        menu.addMenu(
            XgimiMenuItem(
                OperationEvent.CopyMode,
                getString(R.string.copy)
            )
        )
        menu.addMenu(
            XgimiMenuItem(
                OperationEvent.DelFile,
                getString(R.string.delfile)
            )
        )
        return menu
    }

    /**
     * 菜单点击事件
     *
     * @param menu
     * @return
     */
    open fun onMenuClicked(menu: XgimiMenuItem): Boolean {
        val obj: Any? = getSelectedData()
        when (menu.operationType) {
            OperationEvent.ChangeLayoutType -> OperationConfigure.changeLayoutType()
            OperationEvent.SearchFile -> {
                val intent = Intent()
                intent.setClass(context, FileSearchActivity::class.java)
                intent.putExtra("SearchType", getCategory())
                intent.putExtra("RootPath", currentPath)
                startActivity(intent)
            }
            OperationEvent.CutMode -> {
                addOperationData(obj)
                onCutFile()
            }
            OperationEvent.CopyMode -> {
                addOperationData(obj)
                onCopy()
            }
            OperationEvent.DelFile -> {
                addOperationData(obj)
                onDelFile()
            }
            OperationEvent.Paste -> onPasteFile()
            OperationEvent.Rename -> {
                addOperationData(obj)
                onRename()
            }
            OperationEvent.NewFile -> onCreateFile()
            OperationEvent.SelectAll -> {
//                mOperationList.clear()
//                val objectList: List<Any> = mGridListView.getData()
//                if (objectList != null && objectList.isNotEmpty()) {
//                    mOperationList.addAll(objectList)
//                    setChooseMode(true)
//                }
            }
            OperationEvent.Multiselect -> setChooseMode(true)
            OperationEvent.OpenMode ->
                obj?.apply {
                    (obj as BaseData)?.path?.let {
                        IntentBuilder.OpenAs(context, it)
                    }
                }
        }
        return true
    }

    /**
     * 获取当前选中的数据
     *
     * @return
     */
    protected open fun getSelectedData(): Any? {
//        return if (mGridListView.hasFocus() || isMenuShowed) {
//            mGridListView.getSelectedData()
//        } else {
//            null
//        }
        return null
    }

    /**
     * 添加全局操作数据
     *
     * @param obj
     */
    open fun addOperationData(obj: Any?) {
        if (obj != null && !OperationConfigure.isSelectOperationMode) {
            mOperationList.clear()
            mOperationList.add(obj)
        }
    }

    open fun onBackPressed(): Boolean {
        if (OperationConfigure.isSelectOperationMode) {
            setChooseMode(false)
            return true
        }
        return false
    }

    /**
     * 清除操作文件界面
     */
    protected open fun clearRefreshOperationPage() {
        if (mLastRefreshOperationPageDatas.size > 0) {
            mCurrentRefreshOperationPageDatas.clear()
            // todo 此处页面局部刷新
            mLastRefreshOperationPageDatas.clear()
        }
    }

    protected open fun setInitSelection(position: Int) {
        // todo 初始化选中状态
    }

    /**
     * 显示加载进度
     */
    open fun showLoadingView() {
        // todo 正在加载 R.string.progress_wait_loading
    }

    /**
     * 隐藏加载进度
     */
    open fun hideLoadingView() {
        // todo 隐藏加载进度
    }

    /**
     * 获取编辑弹窗数据
     *
     * @return
     */
    protected open fun getGMUIOperation(): MutableList<GMUIOperation> {
        val mOperations = mutableListOf<GMUIOperation>()
        mOperations.add(GMUIOperation(OperationEvent.CutMode, getString(R.string.cut)))
        mOperations.add(GMUIOperation(OperationEvent.CopyMode, getString(R.string.copy)))
        mOperations.add(GMUIOperation(OperationEvent.DelFile, getString(R.string.delfile)))
        if (!OperationConfigure.isSelectOperationMode) {
            mOperations.add(GMUIOperation(OperationEvent.Rename, getString(R.string.rename)))
        }
        return mOperations
    }

    /**
     * 操作显示模式回调
     *
     * @param configureType
     */
    override fun onOperationConfigureChange(configureType: Int) {
        super.onOperationConfigureChange(configureType)
        if (configureType == OperationConfigure.OPERATION_CONFIGURE_LAYOUT_TYPE && isSelectedTab && isActive) {
            changeLayoutType(OperationConfigure.getLayoutType())
        }
        if (configureType == OperationConfigure.OPERATION_CONFIGURE_DISPLAY_MODE && isSelectedTab && isActive) {
            mOperationList.clear()
        }
    }

    /**
     * 改变列表模式、图标模式
     *
     * @param layoutType 列表模式、图标模式
     */
    open fun changeLayoutType(layoutType: Int) {
        //todo 改变显示模式
    }

    /**
     * 显示编辑界面
     *
     * @param data
     */
    open fun showOperationMenu(data: Any) {
        var name: String? = ""
        var isFile = false

        when (data) {
            data is BaseData -> {
                name = (data as BaseData).name
                val file = File(data.path)
                isFile = file != null && file.exists() && file.isFile
                if (data.shareItem is ShareDisk) {
                    return
                }
            }
            data is CategoryBean -> {
                name = (data as CatalogInfo).name
            }
            data is DeviceInfo -> {
                name = (data as DeviceInfo).deviceName
            }
        }

        val operations = getGMUIOperation()

        if (operations == null || operations.size == 0) {
            return
        }
        if (isFile) {
            operations.add(
                GMUIOperation(
                    OperationEvent.OpenMode,
                    getString(R.string.file_operation_openas)
                )
            )
        }

        baseDialog(context) {
            systemAlert = true
            enableCancel = true
            touchOutsideCancel = true
        }.showOnWindow()

//        mGMUIOperationDialogBuilder = GMUIOperationDialogBuilder(mContext)
//            .setTitle(name)
//            .setBlurBgView(getActivity().getWindow().getDecorView())
//            .setOperationDatas(operations)
//            .setOperationListener(object : OperbaseDialog(this.applicationContext) {
//                systemAlert = true
//                enableCancel = true
//                touchOutsideCancel = true
//            }.showOnWindow()ationListener() {
//                fun onClick(dialog: GMUIDialog, operation: GMUIOperation) {
//                    onMenuClicked(Menu(operation.mOperationType, operation.mStr))
//                    if (operation.mOperationType !== OperationEvent.Rename) {
//                        dialog.dismiss()
//                    } else {
//                        getActivity().getWindow().getDecorView()
//                            .postDelayed(Runnable { dialog.dismiss() }, 1000)
//                    }
//                }
//            })
//        mGMUIOperationDialogBuilder.create(R.style.nullAnim_theme_dialog).show()
//        if (data is BaseData) {
//            showFileInfo(data.path)
//        }
    }


}