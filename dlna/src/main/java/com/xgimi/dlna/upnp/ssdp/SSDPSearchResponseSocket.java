package com.xgimi.dlna.upnp.ssdp;

import com.xgimi.dlna.upnp.ControlPoint;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:52
 * desc   :
 */
public class SSDPSearchResponseSocket extends HTTPUSocket implements Runnable {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SSDPSearchResponseSocket() {
        setControlPoint(null);
    }

    public SSDPSearchResponseSocket(String bindAddr, int port) {
        super(bindAddr, port);
        setControlPoint(null);
    }

    ////////////////////////////////////////////////
    //	ControlPoint
    ////////////////////////////////////////////////

    private ControlPoint controlPoint = null;

    public void setControlPoint(ControlPoint ctrlp) {
        this.controlPoint = ctrlp;
    }

    public ControlPoint getControlPoint() {
        return controlPoint;
    }

    ////////////////////////////////////////////////
    //	run
    ////////////////////////////////////////////////

    private Thread deviceSearchResponseThread = null;

    public void run() {
        Thread thisThread = Thread.currentThread();

        ControlPoint ctrlPoint = getControlPoint();

        while (deviceSearchResponseThread == thisThread) {
            Thread.yield();
            SSDPPacket packet = receive();
            if (packet == null) {
                break;
            }
            if (ctrlPoint != null) {
                ctrlPoint.searchResponseReceived(packet);
            }
        }

        //		log.e("SSDPSearchResponseSocket runOver...");

    }

    public void start() {

        StringBuffer name = new StringBuffer("Cyber.SSDPSearchResponseSocket/");
        DatagramSocket s = getDatagramSocket();
        // localAddr is null on Android m3-rc37a (01/30/08)
        InetAddress localAddr = s.getLocalAddress();
        if (localAddr != null) {
            name.append(s.getLocalAddress()).append(':');
            name.append(s.getLocalPort());
        }
        deviceSearchResponseThread = new Thread(this, name.toString());
        deviceSearchResponseThread.start();
    }

    public void stop() {
        deviceSearchResponseThread = null;
    }

    ////////////////////////////////////////////////
    //	post
    ////////////////////////////////////////////////

    public boolean post(String addr, int port, SSDPSearchResponse res) {
        return post(addr, port, res.getHeader());
    }

    ////////////////////////////////////////////////
    //	post
    ////////////////////////////////////////////////

    public boolean post(String addr, int port, SSDPSearchRequest req) {
        return post(addr, port, req.toString());
    }
}


