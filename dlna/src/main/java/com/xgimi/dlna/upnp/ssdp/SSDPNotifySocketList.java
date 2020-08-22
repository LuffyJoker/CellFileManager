package com.xgimi.dlna.upnp.ssdp;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.net.HostInterface;
import com.xgimi.dlna.upnp.ControlPoint;

import java.net.InetAddress;
import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:44
 * desc   :
 */
public class SSDPNotifySocketList extends Vector {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    private InetAddress[] binds = null;

    public SSDPNotifySocketList() {
    }

    /**
     * @param binds The host to bind the service <tt>null</tt> means to bind to default.
     * @since 1.8
     */
    public SSDPNotifySocketList(InetAddress[] binds) {
        this.binds = binds;
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public SSDPNotifySocket getSSDPNotifySocket(int n) {
        return (SSDPNotifySocket) get(n);
    }

    ////////////////////////////////////////////////
    //	ControlPoint
    ////////////////////////////////////////////////

    public void setControlPoint(ControlPoint ctrlPoint) {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPNotifySocket sock = getSSDPNotifySocket(n);
            sock.setControlPoint(ctrlPoint);
        }
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public boolean isValidAddress(String address) {
        if (address == null || address.length() < 1) {
            return false;
        }

        int pos = address.indexOf(':');
        return pos == -1;

    }

    public boolean open() {

        InetAddress[] binds = this.binds;
        String[] bindAddresses;
        if (binds != null) {
            bindAddresses = new String[binds.length];
            for (int i = 0; i < binds.length; i++) {
                bindAddresses[i] = binds[i].getHostAddress();
            }
        } else {
            int nHostAddrs = HostInterface.getNHostAddresses();
            bindAddresses = new String[nHostAddrs];
            for (int n = 0; n < nHostAddrs; n++) {
                bindAddresses[n] = HostInterface.getHostAddress(n);
            }
        }

        for (int i = 0; i < bindAddresses.length; i++) {
            if (!isValidAddress(bindAddresses[i])) {
                LogUtils.e("dlna_framework",
                        "ready to create SSDPNotifySocket bindAddresses = " + bindAddresses[i] + ", it's invalid so " + "drop it!!!");
                continue;
            }
            if (bindAddresses[i] != null) {
                SSDPNotifySocket ssdpNotifySocket = new SSDPNotifySocket(bindAddresses[i]);
                if (ssdpNotifySocket.getSocket() == null) {
                    LogUtils.e("dlna_framework",
                            "ssdpNotifySocket.getSocket() == null!!!");
                    return false;
                }
                LogUtils.e("dlna_framework",
                        "ssdpNotifySocket create success!!!bindAddresses = " + bindAddresses[i]);
                add(ssdpNotifySocket);
                continue;
            }
        }
        return true;
    }

    public void close() {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPNotifySocket sock = getSSDPNotifySocket(n);
            sock.close();
        }
        clear();
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public void start() {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPNotifySocket sock = getSSDPNotifySocket(n);
            sock.start();
        }
    }

    public void stop() {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPNotifySocket sock = getSSDPNotifySocket(n);
            sock.stop();
        }
    }

}


