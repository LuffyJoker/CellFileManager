package com.xgimi.filemanager.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import com.blankj.utilcode.util.LogUtils
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.GMUIOperation
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/14 16:57
 *    desc   :
 */
object IntentBuilder {

    private val TAG = "IntentBuilder"

    /**
     * 初始化打开类型弹窗菜单
     */
    private val mGMUIOperation = mutableListOf<GMUIOperation>()

    init {
        mGMUIOperation.add(GMUIOperation(0, R.string.dialog_type_text))
        mGMUIOperation.add(GMUIOperation(1, R.string.dialog_type_audio))
        mGMUIOperation.add(GMUIOperation(2, R.string.dialog_type_video))
        mGMUIOperation.add(GMUIOperation(3, R.string.dialog_type_image))
        mGMUIOperation.add(GMUIOperation(4, R.string.dialog_type_other))
    }

    /**
     * 打开文件
     *
     * @param context
     * @param filePath
     */
    fun viewFile(context: Context, filePath: String) {
        val type = getMimeType(filePath)
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            try {
                OpenAs(context, filePath, type)
            } catch (e: Exception) {
                e.printStackTrace()
                showChooseFileDialog(context, filePath)
            }
        } else {
            showChooseFileDialog(context, filePath)
        }
    }

    /**
     * 显示类型选择菜单
     *
     * @param context
     * @param filePath
     */
    private fun showChooseFileDialog(context: Context, filePath: String) {
//        val appContext = context.applicationContext
//        val builder: GMUIOperationDialogBuilder = GMUIOperationDialogBuilder(context)
//            .setTitle(R.string.choose_file_type)
//        if (context is Activity) {
//            builder.setBlurBgView(context.window.decorView)
//        } else {
//            builder.setBackgroundRes(R.drawable.new_background_all_default)
//        }
//        builder.setOperationDatas(mGMUIOperation)
//            .setOperationListener(object : OperationListener() {
//                fun onClick(dialog: GMUIDialog, operation: GMUIOperation) {
//                    var selectType = "*/*"
//                    when (operation.mOperationType) {
//                        0 -> selectType = "text/plain"
//                        1 -> selectType = "audio/*"
//                        2 -> selectType = "video/*"
//                        3 -> selectType = "image/*"
//                        4 -> {
//                            com.xgimi.filemanager.filehelper.IntentBuilder.OpenAs(
//                                appContext,
//                                filePath
//                            )
//                            dialog.dismiss()
//                            return
//                        }
//                    }
//                    try {
//                        OpenAs(context, filePath, selectType)
//                    } catch (e: Exception) {
//                        Toast.makeText(appContext, R.string.open_fail, Toast.LENGTH_SHORT).show()
//                    }
//                    dialog.dismiss()
//                }
//            }).show()
    }

    /**
     * 打开视频文件
     *
     * @param context
     * @param filePath
     */
    fun viewFileByVideo(
        context: Context,
        filePath: String
    ) {
        try {
            OpenAs(context, filePath, "video/*")
        } catch (e: Exception) {
            Toast.makeText(context, R.string.open_fail, Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * 获取文件类型
     *
     * @param filePath 文件路径
     * @return 文件类型
     */
    private fun getMimeType(filePath: String): String {
//        val dotPosition = filePath.lastIndexOf('.')
//        if (dotPosition == -1) {
//            return "*/*"
//        }
//        val ext = filePath.substring(dotPosition + 1, filePath.length).toLowerCase()
//        var mimeType: String = MimeUtils.guessMimeTypeFromExtension(ext)
//        if (ext == "mtz") {
//            mimeType = "application/miui-mtz"
//        }
//        return mimeType ?: "*/*"
        return "*/*"
    }

    /**
     * 通过文件路径、类型打开文件
     *
     * @param context  上下文
     * @param filePath 文件路径
     * @param mimeType 文件类型
     */
    private fun OpenAs(
        context: Context,
        filePath: String,
        mimeType: String
    ) {
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.action = Intent.ACTION_VIEW
            val uri = getFileUri(context, filePath)
            LogUtils.i(TAG, "uri:$uri")
            intent.setDataAndType(uri, mimeType)
            intent.putExtra("path", filePath)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.no_find_open_app, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通过文件路径打开文件
     *
     * @param context 上下文
     * @param imgPath 文件路径
     */
    fun OpenAs(context: Context, imgPath: String) {
        OpenAs(context, imgPath, getMimeType(imgPath))
    }

    /**
     * 获取文件打开uri
     *
     * @param context 上下文
     * @param path    文件路径
     * @return uri
     */
    private fun getFileUri(context: Context, path: String): Uri {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            GenericFileProvider.getUriForFile(
//                context,
//                "com.xgimi.gmui.files",
//                File(path)
//            )
//        } else {
//            Uri.fromFile(File(path))
//        }
        return Uri.fromFile(File(path))
    }
}