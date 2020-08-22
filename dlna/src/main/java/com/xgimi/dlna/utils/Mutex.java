package com.xgimi.dlna.utils;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 18:00
 * desc   :
 */
public class Mutex {
    private boolean syncLock;

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public Mutex() {
        syncLock = false;
    }

    ////////////////////////////////////////////////
    //	lock
    ////////////////////////////////////////////////

    public synchronized void lock() {
        while (syncLock == true) {
            try {
                wait();
            } catch (Exception e) {
                Debug.warning(e);
            }
        }
        syncLock = true;
    }

    public synchronized void unlock() {
        syncLock = false;
        notifyAll();
    }

}
