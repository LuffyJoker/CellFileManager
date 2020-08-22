package com.xgimi.filemanager.listerners;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 17:14
 * desc   : 解压缩监听
 */
public interface OnDepressInfoListener {
    void onDepressSuccess(String destPath);

    void onDepressFailure();
}
