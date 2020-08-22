package com.xgimi.dlna.utils;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:58
 * desc   :
 */
public class ThreadCore implements Runnable {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public ThreadCore() {
    }

    ////////////////////////////////////////////////
    //	Thread
    ////////////////////////////////////////////////

    private Thread mThreadObject = null;

    public void setThreadObject(Thread obj) {
        mThreadObject = obj;
    }

    public Thread getThreadObject() {
        return mThreadObject;
    }

    public void start() {
        Thread threadObject = getThreadObject();
        if (threadObject == null) {
            threadObject = new Thread(this, "Cyber.ThreadCore");
            setThreadObject(threadObject);
            threadObject.start();
        }
    }

    public void run() {
    }

    public boolean isRunnable() {
        return (Thread.currentThread() == getThreadObject());
    }

    public void stop() {
        Thread threadObject = getThreadObject();
        if (threadObject != null) {
            //threadObject.destroy();
            //threadObject.stop();

            // Thanks for Kazuyuki Shudo (08/23/07)
            threadObject.interrupt();

            setThreadObject(null);
        }
    }

    public void restart() {
        stop();
        start();
    }
}

