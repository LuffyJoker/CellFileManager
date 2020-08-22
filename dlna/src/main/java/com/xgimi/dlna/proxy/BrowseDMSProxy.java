package com.xgimi.dlna.proxy;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.upnp.Action;
import com.xgimi.dlna.upnp.Argument;
import com.xgimi.dlna.upnp.ArgumentList;
import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.upnp.MediaItem;
import com.xgimi.dlna.upnp.Service;
import com.xgimi.dlna.upnp.UPnPStatus;
import com.xgimi.dlna.utils.ParseUtil;

import java.util.List;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:20
 * desc   :
 */
public class BrowseDMSProxy {

    public interface BrowseRequestCallback {
        void onGetItems(final List<MediaItem> list);
    }

    private static String currentId = "0";

    public static void syncGetDirectory(final Context context, BrowseRequestCallback callback) {
        mBrowseRequestCallback = callback;
        callback = null;
        setCurrentId("0");
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                List<MediaItem> list = null;
                try {
                    list = getDirectory(context);
                    if (list != null) {
                        Log.e("getDirectory", "MediaItem:" + list.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mBrowseRequestCallback != null) {
                    mBrowseRequestCallback.onGetItems(list);
                }
            }
        });

        thread.start();
    }

    private static BrowseRequestCallback mBrowseRequestCallback;

    public static void removeBrowseRequestCallback() {
        mBrowseRequestCallback = null;
    }

    public static void syncGetItems(final Context context, final String id, BrowseRequestCallback callback) {
        mBrowseRequestCallback = callback;
        callback = null;
        setCurrentId(id);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                List<MediaItem> list = null;
                try {
                    list = getItems(context, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mBrowseRequestCallback != null) {
                    mBrowseRequestCallback.onGetItems(list);
                }
            }
        });

        thread.start();


    }

    // TODO: 2020/6/29 此处解析 gif 图片地址出错
    public static List<MediaItem> getDirectory(Context context) throws Exception {

        Device selDevice = AllShareProxy.getInstance(context).getDMSSelectedDevice();
        if (selDevice == null) {
            LogUtils.e("dlna_framework", "no selDevice!!!");
            return null;
        }


        //		Node selDevNode = selDevice.getDeviceNode();
        //		if (selDevNode != null){
        //			selDevNode.print();
        //		}

        Service service = selDevice.getService("urn:schemas-upnp-org:service:ContentDirectory:1");
        if (service == null) {
            LogUtils.e("dlna_framework", "no service for ContentDirectory!!!");
            return null;
        }

        //		Node serverNode = service.getServiceNode();
        //		if (serverNode != null){
        //			serverNode.print();
        //		}

        Action action = service.getAction("Browse");
        if (action == null) {
            LogUtils.e("dlna_framework", "action for Browse is null!!!");
            return null;
        }
        ArgumentList argumentList = action.getArgumentList();
        argumentList.getArgument("ObjectID").setValue(0);
        argumentList.getArgument("BrowseFlag").setValue("BrowseDirectChildren");
        argumentList.getArgument("StartingIndex").setValue("0");
        argumentList.getArgument("RequestedCount").setValue("0");
        argumentList.getArgument("Filter").setValue("*");
        argumentList.getArgument("SortCriteria").setValue("");

        ArgumentList actionInputArgList = action.getInputArgumentList();
        int size = actionInputArgList.size();
        for (int i = 0; i < size; i++) {
            Argument argument = (Argument) (actionInputArgList.get(i));
            argument.getArgumentNode().print();
        }

        if (action.postControlAction()) {
            ArgumentList outArgList = action.getOutputArgumentList();
            Argument result = outArgList.getArgument("Result");
            LogUtils.d("dlna_framework", "result value = \n" + result.getValue());
            List<MediaItem> items = ParseUtil.parseResult(result);
            return items;
        } else {
            UPnPStatus err = action.getControlStatus();
            LogUtils.e("dlna_framework", "Error Code = " + err.getCode());
            LogUtils.e("dlna_framework", "Error Code = " + "Error Desc = " + err.getDescription());
        }
        return null;
    }

    public static List<MediaItem> getItems(Context context, String id) throws Exception {


        Device selDevice = AllShareProxy.getInstance(context).getDMSSelectedDevice();
        if (selDevice == null) {
            LogUtils.e("dlna_framework", "no selDevice!!!");
            return null;
        }

        Service service = selDevice.getService("urn:schemas-upnp-org:service:ContentDirectory:1");
        if (selDevice == null) {
            LogUtils.e("dlna_framework", "no service for ContentDirectory!!!");
            return null;
        }

        Action action = service.getAction("Browse");
        if (action == null) {
            LogUtils.e("dlna_framework", "action for Browse is null");
            return null;
        }

        //	action.getActionNode().print();

        ArgumentList argumentList = action.getArgumentList();
        argumentList.getArgument("ObjectID").setValue(id);
        argumentList.getArgument("BrowseFlag").setValue("BrowseDirectChildren");
        argumentList.getArgument("StartingIndex").setValue("0");
        argumentList.getArgument("RequestedCount").setValue("0");
        argumentList.getArgument("Filter").setValue("*");
        argumentList.getArgument("SortCriteria").setValue("");

        if (action.postControlAction()) {
            ArgumentList outArgList = action.getOutputArgumentList();
            Argument result = outArgList.getArgument("Result");
            LogUtils.d("dlna_framework", "result value = \n" + result.getValue());
            List<MediaItem> items = ParseUtil.parseResult(result);
            return items;
        } else {
            UPnPStatus err = action.getControlStatus();
            System.out.println("Error Code = " + err.getCode());
            System.out.println("Error Desc = " + err.getDescription());
        }
        return null;
    }

    public static String getCurrentId() {
        return currentId;
    }

    public static void setCurrentId(String currentId) {
        BrowseDMSProxy.currentId = currentId;
    }
}

