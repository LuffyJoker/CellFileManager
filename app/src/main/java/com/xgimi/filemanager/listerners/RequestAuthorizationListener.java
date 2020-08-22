package com.xgimi.filemanager.listerners;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 15:22
 * desc   : 请求认证监听
 */
public interface RequestAuthorizationListener {
    void onRequestSuccess();

    void onRequestFail();
}
