package com.xgimi.dlna.upnp.device;

import java.io.File;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:59
 * desc   :
 */
public class InvalidDescriptionException extends Exception {
    public InvalidDescriptionException() {
        super();
    }

    public InvalidDescriptionException(String s) {
        super(s);
    }

    public InvalidDescriptionException(String s, File file) {
        super(s + " (" + file.toString() + ")");
    }

    public InvalidDescriptionException(Exception e) {
        super(e.getMessage());
    }
}
