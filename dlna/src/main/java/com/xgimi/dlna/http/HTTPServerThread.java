package com.xgimi.dlna.http;

import java.net.Socket;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:41
 * desc   :
 */
public class HTTPServerThread extends Thread {
    private HTTPServer httpServer;
    private Socket sock;

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public HTTPServerThread(HTTPServer httpServer, Socket sock) {
        super("Cyber.HTTPServerThread");
        this.httpServer = httpServer;
        this.sock = sock;
    }

    ////////////////////////////////////////////////
    //	run
    ////////////////////////////////////////////////

    public void run() {
        HTTPSocket httpSock = new HTTPSocket(sock);
        if (httpSock.open() == false) {
            return;
        }
        HTTPRequest httpReq = new HTTPRequest();
        httpReq.setSocket(httpSock);
        while (httpReq.read() == true) {
            httpServer.performRequestListener(httpReq);
            if (httpReq.isKeepAlive() == false) {
                break;
            }
        }
        httpSock.close();
    }
}

