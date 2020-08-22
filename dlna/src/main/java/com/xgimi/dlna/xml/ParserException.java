package com.xgimi.dlna.xml;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:56
 * desc   :
 */
public class ParserException extends Exception {
    public ParserException(Exception e) {
        super(e);
    }

    public ParserException(String s) {
        super(s);
    }
}
