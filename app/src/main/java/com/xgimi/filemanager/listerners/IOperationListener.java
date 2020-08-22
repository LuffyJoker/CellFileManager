package com.xgimi.filemanager.listerners;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:00
 * desc   :
 */
public interface IOperationListener {

    int OnDelSuccess = 15200;
    int OnNewFileAdd = 15211;
    int OnRenameSuccess = 15222;
    int OnIOperationFinish = 15233;
    int OnDecompressSuccess = 15244;
    int OnNoHideFile = 15255;
    int ON_LACK_STORAGE_SPACE = 100;
    int ON_FILE_NOT_EXIST = 101;
    int ON_FILE_ERROR = 102;
    int IOException = 1;
    int FileBigThanFourG = 2;
    int DestFileNotFound = 3;
    int UseableSpaceNotEnough = 4;
    int CopyCancel = 5;


    void onCreateNewDirectory(String path);

    void onPasteStart(String title);

    /**
     * 粘贴状态变化
     *
     * @param orginal
     * @param dest
     * @param count
     * @param length
     */
    void onPasteProgressChange(String orginal, String dest, long count, long length);


    /**
     * 粘贴成功后
     *
     * @param dir
     * @param path
     * @param length
     */
    void onPasteFileSuccess(boolean dir, String path, long length);

    /**
     * 粘贴失败后
     *
     * @param dir
     * @param path
     */
    void onPasteFileFailure(boolean dir, String path, int errorCode);


    /**
     * 删除成功
     *
     * @param dir
     * @param path
     * @param length
     */
    void onDelFileSuccess(boolean dir, String path, long length);

    /**
     * 删除失败
     *
     * @param dir
     * @param path
     */
    void onDelFileFailure(boolean dir, String path);


    /**
     * 重命名成功
     *
     * @param original
     * @param current
     */
    void onRenameFileSuccess(String original, String current);


    /**
     * 重命名失败
     *
     * @param orginal
     */
    void onRenameFileFailure(String orginal);


    /**
     * 解压文件成功
     *
     * @param path
     */
    void onDecompressSuccess(String path);

    void onOperationFinish(int operationType, String dest);
}

