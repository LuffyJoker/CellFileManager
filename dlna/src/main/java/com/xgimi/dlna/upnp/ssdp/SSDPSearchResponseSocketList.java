package com.xgimi.dlna.upnp.ssdp;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.net.HostInterface;
import com.xgimi.dlna.upnp.ControlPoint;

import java.net.InetAddress;
import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:53
 * desc   :
 */
public class SSDPSearchResponseSocketList extends Vector {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    private InetAddress[] binds = null;

    public SSDPSearchResponseSocketList() {
    }

    /**
     * @param binds The host to bind.Use <tt>null</tt> for the default behavior
     */
    public SSDPSearchResponseSocketList(InetAddress[] binds) {
        this.binds = binds;
    }


    ////////////////////////////////////////////////
    //	ControlPoint

    ////////////////////////////////////////////////
    //	ControlPoint
    ////////////////////////////////////////////////

    public void setControlPoint(ControlPoint ctrlPoint) {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
            sock.setControlPoint(ctrlPoint);
        }
    }

    ////////////////////////////////////////////////
    //	get
    ////////////////////////////////////////////////

    public SSDPSearchResponseSocket getSSDPSearchResponseSocket(int n) {
        return (SSDPSearchResponseSocket) get(n);
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


    public boolean open(int port) {
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

        boolean flag = false;
        for (int k = 0; k < bindAddresses.length; k++) {
            LogUtils.e("dlna_framework", "bindAddresses k = " + k + ", addr = " + bindAddresses[k]);
        }
        try {
            for (int j = 0; j < bindAddresses.length; j++) {
                if (!isValidAddress(bindAddresses[j])) {
                    LogUtils.e("dlna_framework",
                            "ready to create SSDPSearchResponseSocket bindAddresses = " + bindAddresses[j] + ", it's "
                                    + "invalid so drop it!!!");
                    continue;
                }
                SSDPSearchResponseSocket socket = new SSDPSearchResponseSocket(bindAddresses[j], port);
                if (socket.getDatagramSocket() == null) {
                    LogUtils.e("dlna_framework", "SSDPSearchResponseSocket.getSocket() == null!!!");
                    continue;
                }
                LogUtils.i("dlna_framework",
                        "SSDPSearchResponseSocket create success!!!bindAddresses = " + bindAddresses[j]);
                add(socket);
                flag = true;
                continue;
            }
        } catch (Exception e) {
            stop();
            close();
            clear();
            return false;
        }
        return flag;
    }

    public boolean open() {
        return open(SSDP.PORT);
    }

    public void close() {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
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
            SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
            sock.start();
        }
    }

    public void stop() {
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
            sock.stop();
        }
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public boolean post(SSDPSearchRequest req) {
        boolean ret = true;
        int nSockets = size();
        for (int n = 0; n < nSockets; n++) {
            SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
            String bindAddr = sock.getLocalAddress();
            req.setLocalAddress(bindAddr);
            String ssdpAddr = SSDP.ADDRESS;
            if (HostInterface.isIPv6Address(bindAddr) == true) {
                ssdpAddr = SSDP.getIPv6Address();
            }
            //sock.joinGroup(ssdpAddr, SSDP.PORT, bindAddr);
            if (sock.post(ssdpAddr, SSDP.PORT, req) == false) {
                ret = false;
            }
            //sock.leaveGroup(ssdpAddr, SSDP.PORT, bindAddr);
        }
        return ret;
    }

}


