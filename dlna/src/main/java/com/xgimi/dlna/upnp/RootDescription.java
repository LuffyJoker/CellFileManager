package com.xgimi.dlna.upnp;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:11
 * desc   :
 */
public interface RootDescription {

    String ROOT_ELEMENT = "root";
    String ROOT_ELEMENT_NAMESPACE = "urn:schemas-upnp-org:device-1-0";


    String SPECVERSION_ELEMENT = "specVersion";
    String MAJOR_ELEMENT = "major";
    String MINOR_ELEMENT = "minor";
    String SERVICE_LIST_ELEMENT = "serviceList";
}
