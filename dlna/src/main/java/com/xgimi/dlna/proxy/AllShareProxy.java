package com.xgimi.dlna.proxy;

import android.content.Context;
import android.content.Intent;

import com.xgimi.dlna.center.DlnaService;
import com.xgimi.dlna.upnp.AbstractMediaMng;
import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.upnp.MediaServerMng;
import com.xgimi.dlna.utils.UpnpUtil;

import java.util.List;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:30
 * desc   :
 */
public class AllShareProxy implements IDeviceOperator, IDeviceOperator.IDMSDeviceOperator {

    private static AllShareProxy instance;
    private Context mContext;

    private AbstractMediaMng dmsMediaMng;

    private AllShareProxy(Context context) {
        mContext = context;
        dmsMediaMng = new MediaServerMng(context);
    }

    public static synchronized AllShareProxy getInstance(Context context) {
        if (instance == null) {
            instance = new AllShareProxy(context.getApplicationContext());
        }
        return instance;
    }

    public void startSearch() {
        Intent intent = new Intent(DlnaService.SEARCH_DEVICES);
        intent.setPackage(mContext.getPackageName());
        mContext.startService(intent);
    }

    public void resetSearch() {
        Intent intent = new Intent(DlnaService.RESET_SEARCH_DEVICES);
        intent.setPackage(mContext.getPackageName());
        mContext.startService(intent);
        clearDevice();
    }

    public void exitSearch() {
        mContext.stopService(new Intent(mContext, DlnaService.class));
        clearDevice();
    }


    @Override
    public void addDevice(Device d) {
        if (UpnpUtil.isMediaServerDevice(d)) {
            dmsMediaMng.addDevice(d);
        }
    }

    @Override
    public void removeDevice(Device d) {
        if (UpnpUtil.isMediaServerDevice(d)) {
            dmsMediaMng.removeDevice(d);
        }
    }

    @Override
    public void clearDevice() {
        dmsMediaMng.clear();
    }

    @Override
    public List<Device> getDMSDeviceList() {
        return dmsMediaMng.getDeviceList();
    }


    @Override
    public void setDMSSelectedDevice(Device selectedDevice) {
        dmsMediaMng.setSelectedDevice(selectedDevice);
    }

    @Override
    public Device getDMSSelectedDevice() {
        return dmsMediaMng.getSelectedDevice();
    }

}

