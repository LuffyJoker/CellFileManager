package com.xgimi.dlna.utils;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:14
 * desc   :
 */
public final class TimerUtil {
    public final static void wait(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (Exception e) {
        }
    }

    public final static void waitRandom(int time) {
        int waitTime = (int) (Math.random() * (double) time);
        try {
            Thread.sleep(waitTime);
        } catch (Exception e) {
        }
    }
}


