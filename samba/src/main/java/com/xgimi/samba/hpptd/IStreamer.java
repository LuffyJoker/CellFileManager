package com.xgimi.samba.hpptd;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/26 17:07
 * desc   :
 */
public interface IStreamer {

    void start();

    void stopStream();

    /**
     * get post
     *
     * @return
     */
    int getPort();

    /**
     * For using in current device, you can just use "127.0.0.1"<p/>
     * For others, use your true ip in ipv4
     *
     * @return
     */
    String getIp();
}
