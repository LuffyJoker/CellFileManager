package com.xgimi.dlna.proxy;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:20
 * desc   : 设备改变监听
 */
public interface IDeviceChangeListener {

    void onDeviceChange(boolean isSelDeviceChange);
}
