package com.xgimi.dlna.http;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:35
 * desc   : Http 请求监听
 */
public interface HTTPRequestListener {
    void httpRequestReceived(HTTPRequest httpReq);
}
