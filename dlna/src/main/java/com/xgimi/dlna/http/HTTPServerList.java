package com.xgimi.dlna.http;

import com.xgimi.dlna.net.HostInterface;
import com.xgimi.dlna.upnp.Device;

import java.net.InetAddress;
import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:03
 * desc   :
 */
public class HTTPServerList extends Vector {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    private InetAddress[] binds = null;
    private int port = Device.HTTP_DEFAULT_PORT;

    public HTTPServerList() {
    }

    public HTTPServerList(InetAddress[] list, int port) {
        this.binds = list;
        this.port = port;
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public void addRequestListener(HTTPRequestListener listener) {
        int nServers = size();
        for (int n = 0; n < nServers; n++) {
            HTTPServer server = getHTTPServer(n);
            server.addRequestListener(listener);
        }
    }

    public HTTPServer getHTTPServer(int n) {
        return (HTTPServer) get(n);
    }

    ////////////////////////////////////////////////
    //	open/close
    ////////////////////////////////////////////////

    public void close() {
        int nServers = size();
        for (int n = 0; n < nServers; n++) {
            HTTPServer server = getHTTPServer(n);
            server.close();
        }
    }

    public int open() {
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
        int j = 0;
        for (int i = 0; i < bindAddresses.length; i++) {
            HTTPServer httpServer = new HTTPServer();
            if ((bindAddresses[i] == null) || (httpServer.open(bindAddresses[i], port) == false)) {
                close();
                clear();
            } else {
                add(httpServer);
                j++;
            }
        }
        return j;
    }


    public boolean open(int port) {
        this.port = port;
        return open() != 0;
    }

    ////////////////////////////////////////////////
    //	start/stop
    ////////////////////////////////////////////////

    public void start() {
        int nServers = size();
        for (int n = 0; n < nServers; n++) {
            HTTPServer server = getHTTPServer(n);
            server.start();
        }
    }

    public void stop() {
        int nServers = size();
        for (int n = 0; n < nServers; n++) {
            HTTPServer server = getHTTPServer(n);
            server.stop();
        }
    }

}


