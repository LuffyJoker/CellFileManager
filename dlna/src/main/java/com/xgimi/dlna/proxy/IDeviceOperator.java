package com.xgimi.dlna.proxy;

import com.xgimi.dlna.upnp.Device;

import java.util.List;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:30
 * desc   :
 */
public interface IDeviceOperator {

    void addDevice(Device d);

    void removeDevice(Device d);

    void clearDevice();

    interface IDMSDeviceOperator {
        List<Device> getDMSDeviceList();

        Device getDMSSelectedDevice();

        void setDMSSelectedDevice(Device selectedDevice);
    }
}
