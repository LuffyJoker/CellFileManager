package com.xgimi.dlna.upnp;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.http.HTTPRequest;
import com.xgimi.dlna.http.HTTPRequestListener;
import com.xgimi.dlna.http.HTTPServerList;
import com.xgimi.dlna.net.HostInterface;
import com.xgimi.dlna.upnp.control.RenewSubscriber;
import com.xgimi.dlna.upnp.device.DeviceChangeListener;
import com.xgimi.dlna.upnp.device.Disposer;
import com.xgimi.dlna.upnp.device.NotifyListener;
import com.xgimi.dlna.upnp.device.ST;
import com.xgimi.dlna.upnp.device.SearchResponseListener;
import com.xgimi.dlna.upnp.device.USN;
import com.xgimi.dlna.upnp.event.EventListener;
import com.xgimi.dlna.upnp.event.NotifyRequest;
import com.xgimi.dlna.upnp.event.Property;
import com.xgimi.dlna.upnp.event.PropertyList;
import com.xgimi.dlna.upnp.event.Subscription;
import com.xgimi.dlna.upnp.event.SubscriptionRequest;
import com.xgimi.dlna.upnp.event.SubscriptionResponse;
import com.xgimi.dlna.upnp.ssdp.SSDP;
import com.xgimi.dlna.upnp.ssdp.SSDPNotifySocketList;
import com.xgimi.dlna.upnp.ssdp.SSDPPacket;
import com.xgimi.dlna.upnp.ssdp.SSDPSearchRequest;
import com.xgimi.dlna.upnp.ssdp.SSDPSearchResponseSocketList;
import com.xgimi.dlna.utils.Debug;
import com.xgimi.dlna.utils.ListenerList;
import com.xgimi.dlna.utils.Mutex;
import com.xgimi.dlna.xml.Node;
import com.xgimi.dlna.xml.NodeList;
import com.xgimi.dlna.xml.ParserException;
import com.xgimi.dlna.xml.parser.Parser;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:42
 * desc   :
 */
public class ControlPoint implements HTTPRequestListener {
    private final static int DEFAULT_EVENTSUB_PORT = 8058;
    private final static int DEFAULT_SSDP_PORT = 8008;
    private final static int DEFAULT_EXPIRED_DEVICE_MONITORING_INTERVAL = 60;

    private final static String DEFAULT_EVENTSUB_URI = "/evetSub";

    ////////////////////////////////////////////////
    //	Member
    ////////////////////////////////////////////////

    private SSDPNotifySocketList ssdpNotifySocketList;
    private SSDPSearchResponseSocketList ssdpSearchResponseSocketList;

    private SSDPNotifySocketList getSSDPNotifySocketList() {
        return ssdpNotifySocketList;
    }

    private SSDPSearchResponseSocketList getSSDPSearchResponseSocketList() {
        return ssdpSearchResponseSocketList;
    }

    ////////////////////////////////////////////////
    //	Initialize
    ////////////////////////////////////////////////

    static {
        UPnP.initialize();
    }

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////
    public ControlPoint(int ssdpPort, int httpPort, InetAddress[] binds) {
        ssdpNotifySocketList = new SSDPNotifySocketList(binds);
        ssdpSearchResponseSocketList = new SSDPSearchResponseSocketList(binds);

        setSSDPPort(ssdpPort);
        setHTTPPort(httpPort);

        setDeviceDisposer(null);
        setExpiredDeviceMonitoringInterval(DEFAULT_EXPIRED_DEVICE_MONITORING_INTERVAL);

        setRenewSubscriber(null);

        setNMPRMode(false);
        setRenewSubscriber(null);
    }

    public ControlPoint(int ssdpPort, int httpPort) {
        this(ssdpPort, httpPort, null);
    }

    public ControlPoint() {
        this(DEFAULT_SSDP_PORT, DEFAULT_EVENTSUB_PORT);
    }

    public void finalize() {
        LogUtils.e("finalize");
        stop();
    }

    ////////////////////////////////////////////////
    // Mutex
    ////////////////////////////////////////////////

    private Mutex mutex = new Mutex();

    public void lock() {
        mutex.lock();
    }

    public void unlock() {
        mutex.unlock();
    }

    ////////////////////////////////////////////////
    //	Port (SSDP)
    ////////////////////////////////////////////////

    private int ssdpPort = 0;

    public int getSSDPPort() {
        return ssdpPort;
    }

    public void setSSDPPort(int port) {
        ssdpPort = port;
    }

    ////////////////////////////////////////////////
    //	Port (EventSub)
    ////////////////////////////////////////////////

    private int httpPort = 0;

    public int getHTTPPort() {
        return httpPort;
    }

    public void setHTTPPort(int port) {
        httpPort = port;
    }

    ////////////////////////////////////////////////
    //	NMPR
    ////////////////////////////////////////////////

    private boolean nmprMode;

    public void setNMPRMode(boolean flag) {
        nmprMode = flag;
    }

    public boolean isNMPRMode() {
        return nmprMode;
    }

    ////////////////////////////////////////////////
    //	Device List
    ////////////////////////////////////////////////

    private NodeList devNodeList = new NodeList();

    private void addDevice(Node rootNode) {
        synchronized (devNodeList) {
            devNodeList.add(rootNode);
        }

    }

    private boolean isValidLocation(String location) {
        if (location == null || location.length() < 1) {
            return false;
        }

        int pos = location.indexOf("http://[");
        return pos < 0;
    }


    private synchronized void addDevice(SSDPPacket ssdpPacket) {
        if (ssdpPacket.isRootDevice() == false) {
            return;
        }

        if (!isValidLocation(ssdpPacket.getLocation())) {
            LogUtils.e("dlna_framework", "ssdpPacket.getLocation() = " + ssdpPacket.getLocation() + ", so drop it!!!");
            return;
        }

        String usn = ssdpPacket.getUSN();
        String udn = USN.getUDN(usn);
        Device dev = getDevice(udn);
        if (dev != null) {
            dev.setSSDPPacket(ssdpPacket);
            return;
        }

        String location = ssdpPacket.getLocation();
        try {
            URL locationUrl = new URL(location);
            Parser parser = UPnP.getXMLParser();
            Node rootNode = parser.parse(locationUrl);
            Device rootDev = getDevice(rootNode);
            if (rootDev == null) {
                return;
            }
            rootDev.setSSDPPacket(ssdpPacket);
            addDevice(rootNode);

            // Thanks for Oliver Newell (2004/10/16)
            // After node is added, invoke the AddDeviceListener to notify high-level
            // control point application that a new device has been added. (The
            // control point application must implement the DeviceChangeListener interface
            // to receive the notifications)
            performAddDeviceListener(rootDev);
        } catch (MalformedURLException me) {
            Debug.warning(ssdpPacket.toString());
            Debug.warning(me);
        } catch (ParserException pe) {
            Debug.warning(ssdpPacket.toString());
            Debug.warning(pe);
        }
    }

    private Device getDevice(Node rootNode) {
        if (rootNode == null) {
            return null;
        }
        Node devNode = rootNode.getNode(Device.ELEM_NAME);
        if (devNode == null) {
            return null;
        }
        return new Device(rootNode, devNode);
    }

    public DeviceList getDeviceList() {
        DeviceList devList = new DeviceList();

        synchronized (devList) {
            int nRoots = devNodeList.size();
            for (int n = 0; n < nRoots; n++) {
                Node rootNode = devNodeList.getNode(n);
                Device dev = getDevice(rootNode);
                if (dev == null) {
                    continue;
                }
                devList.add(dev);
            }
        }

        return devList;
    }

    public Device getDevice(String name) {
        synchronized (devNodeList) {
            int nRoots = devNodeList.size();
            for (int n = 0; n < nRoots; n++) {
                Node rootNode = devNodeList.getNode(n);
                Device dev = getDevice(rootNode);
                if (dev == null) {
                    continue;
                }
                if (dev.isDevice(name) == true) {
                    return dev;
                }
                Device cdev = dev.getDevice(name);
                if (cdev != null) {
                    return cdev;
                }
            }
            return null;
        }

    }

    public boolean hasDevice(String name) {
        return (getDevice(name) != null);
    }

    private void removeDevice(Node rootNode) {
        // Thanks for Oliver Newell (2004/10/16)
        // Invoke device removal listener prior to actual removal so Device node
        // remains valid for the duration of the listener (application may want
        // to access the node)
        Device dev = getDevice(rootNode);
        if (dev != null && dev.isRootDevice()) {
            performRemoveDeviceListener(dev);
        }

        synchronized (devNodeList) {
            devNodeList.remove(rootNode);
        }

    }

    protected void removeDevice(Device dev) {
        if (dev == null) {
            return;
        }
        removeDevice(dev.getRootNode());
    }

    protected void removeDevice(String name) {
        Device dev = getDevice(name);
        removeDevice(dev);
    }

    private void removeDevice(SSDPPacket packet) {
        if (packet.isByeBye() == false) {
            return;
        }
        String usn = packet.getUSN();
        String udn = USN.getUDN(usn);
        removeDevice(udn);
    }

    ////////////////////////////////////////////////
    //	Expired Device
    ////////////////////////////////////////////////

    private Disposer deviceDisposer;
    private long expiredDeviceMonitoringInterval;

    public void removeExpiredDevices() {
        DeviceList devList = getDeviceList();
        int devCnt = devList.size();
        Device dev[] = new Device[devCnt];
        for (int n = 0; n < devCnt; n++) {
            dev[n] = devList.getDevice(n);
        }
        for (int n = 0; n < devCnt; n++) {
            if (dev[n].isExpired() == true) {
                Debug.message("Expired device = " + dev[n].getFriendlyName());
                removeDevice(dev[n]);
            }
        }
    }

    public void setExpiredDeviceMonitoringInterval(long interval) {
        expiredDeviceMonitoringInterval = interval;
    }

    public long getExpiredDeviceMonitoringInterval() {
        return expiredDeviceMonitoringInterval;
    }

    public void setDeviceDisposer(Disposer disposer) {
        deviceDisposer = disposer;
    }

    public Disposer getDeviceDisposer() {
        return deviceDisposer;
    }

    ////////////////////////////////////////////////
    //	Notify
    ////////////////////////////////////////////////

    private ListenerList deviceNotifyListenerList = new ListenerList();

    public void addNotifyListener(NotifyListener listener) {
        deviceNotifyListenerList.add(listener);
    }

    public void removeNotifyListener(NotifyListener listener) {
        deviceNotifyListenerList.remove(listener);
    }

    public void performNotifyListener(SSDPPacket ssdpPacket) {
        int listenerSize = deviceNotifyListenerList.size();
        for (int n = 0; n < listenerSize; n++) {
            NotifyListener listener = (NotifyListener) deviceNotifyListenerList.get(n);
            try {
                listener.deviceNotifyReceived(ssdpPacket);
            } catch (Exception e) {
                Debug.warning("NotifyListener returned an error:", e);
            }
        }
    }

    ////////////////////////////////////////////////
    //	SearchResponse
    ////////////////////////////////////////////////

    private ListenerList deviceSearchResponseListenerList = new ListenerList();

    public void addSearchResponseListener(SearchResponseListener listener) {
        deviceSearchResponseListenerList.add(listener);
    }

    public void removeSearchResponseListener(SearchResponseListener listener) {
        deviceSearchResponseListenerList.remove(listener);
    }

    public void performSearchResponseListener(SSDPPacket ssdpPacket) {
        int listenerSize = deviceSearchResponseListenerList.size();
        for (int n = 0; n < listenerSize; n++) {
            SearchResponseListener listener = (SearchResponseListener) deviceSearchResponseListenerList.get(n);
            try {
                listener.deviceSearchResponseReceived(ssdpPacket);
            } catch (Exception e) {
                Debug.warning("SearchResponseListener returned an error:", e);
            }


        }
    }

    /////////////////////////////////////////////////////////////////////
    // Device status changes (device added or removed)
    // Applications that support the DeviceChangeListener interface are
    // notified immediately when a device is added to, or removed from,
    // the control point.
    /////////////////////////////////////////////////////////////////////

    ListenerList deviceChangeListenerList = new ListenerList();

    public void addDeviceChangeListener(DeviceChangeListener listener) {
        deviceChangeListenerList.add(listener);
    }

    public void removeDeviceChangeListener(DeviceChangeListener listener) {
        deviceChangeListenerList.remove(listener);
    }

    public void performAddDeviceListener(Device dev) {
        int listenerSize = deviceChangeListenerList.size();
        for (int n = 0; n < listenerSize; n++) {
            DeviceChangeListener listener = (DeviceChangeListener) deviceChangeListenerList.get(n);
            listener.deviceAdded(dev);
        }
    }

    public void performRemoveDeviceListener(Device dev) {
        int listenerSize = deviceChangeListenerList.size();
        for (int n = 0; n < listenerSize; n++) {
            DeviceChangeListener listener = (DeviceChangeListener) deviceChangeListenerList.get(n);
            listener.deviceRemoved(dev);
        }
    }

    ////////////////////////////////////////////////
    //	SSDPPacket
    ////////////////////////////////////////////////

    public void notifyReceived(SSDPPacket packet) {
        if (packet.isRootDevice() == true) {
            if (packet.isAlive() == true) {
                //	log.e("is Alive message , packet = " + packet.toString());
                addDevice(packet);
            } else if (packet.isByeBye() == true) {
                LogUtils.e("dlna_framework", "is byebye message , packet = " + packet.toString());
                removeDevice(packet);
            } else {

            }
        }
        performNotifyListener(packet);
    }

    public void searchResponseReceived(SSDPPacket packet) {
        if (packet.isRootDevice() == true) {
            //s	log.e("searchResponseReceived SSDPPacket = \n" + packet.toString());
            addDevice(packet);
        }

        performSearchResponseListener(packet);
    }

    ////////////////////////////////////////////////
    //	M-SEARCH
    ////////////////////////////////////////////////

    private int searchMx = SSDP.DEFAULT_MSEARCH_MX;

    public int getSearchMx() {
        return searchMx;
    }

    public void setSearchMx(int mx) {
        searchMx = mx;
    }

    public boolean search(String target, int mx) {
        //	log.e("search target = " + target + ", mx = " + mx);
        SSDPSearchRequest msReq = new SSDPSearchRequest(target, mx);
        SSDPSearchResponseSocketList ssdpSearchResponseSocketList = getSSDPSearchResponseSocketList();
        boolean ret = ssdpSearchResponseSocketList.post(msReq);
        return ret;
    }

    public boolean search(String target) {
        return search(target, SSDP.DEFAULT_MSEARCH_MX);
    }

    public boolean search() {
        return search(ST.ROOT_DEVICE, SSDP.DEFAULT_MSEARCH_MX);
    }


    ////////////////////////////////////////////////
    //	EventSub HTTPServer
    ////////////////////////////////////////////////

    private HTTPServerList httpServerList = new HTTPServerList();

    private HTTPServerList getHTTPServerList() {
        return httpServerList;
    }

    public void httpRequestReceived(HTTPRequest httpReq) {
        if (Debug.isOn() == true) {
            httpReq.print();
        }

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/08/03)
        if (httpReq.isNotifyRequest() == true) {
            NotifyRequest notifyReq = new NotifyRequest(httpReq);
            String uuid = notifyReq.getSID();
            long seq = notifyReq.getSEQ();
            PropertyList props = notifyReq.getPropertyList();
            int propCnt = props.size();
            for (int n = 0; n < propCnt; n++) {
                Property prop = props.getProperty(n);
                String varName = prop.getName();
                String varValue = prop.getValue();
                performEventListener(uuid, seq, varName, varValue);
            }
            httpReq.returnOK();
            return;
        }

        httpReq.returnBadRequest();
    }

    ////////////////////////////////////////////////
    //	Event Listener
    ////////////////////////////////////////////////

    private ListenerList eventListenerList = new ListenerList();

    public void addEventListener(EventListener listener) {
        eventListenerList.add(listener);
    }

    public void removeEventListener(EventListener listener) {
        eventListenerList.remove(listener);
    }

    public void performEventListener(String uuid, long seq, String name, String value) {
        int listenerSize = eventListenerList.size();
        for (int n = 0; n < listenerSize; n++) {
            EventListener listener = (EventListener) eventListenerList.get(n);
            listener.eventNotifyReceived(uuid, seq, name, value);
        }
    }

    ////////////////////////////////////////////////
    //	Subscription
    ////////////////////////////////////////////////

    private String eventSubURI = DEFAULT_EVENTSUB_URI;

    public String getEventSubURI() {
        return eventSubURI;
    }

    public void setEventSubURI(String url) {
        eventSubURI = url;
    }

    private String getEventSubCallbackURL(String host) {
        return HostInterface.getHostURL(host, getHTTPPort(), getEventSubURI());
    }

    public boolean subscribe(Service service, long timeout) {
        if (service.isSubscribed() == true) {
            String sid = service.getSID();
            return subscribe(service, sid, timeout);
        }

        Device rootDev = service.getRootDevice();
        if (rootDev == null) {
            return false;
        }
        String ifAddress = rootDev.getInterfaceAddress();
        SubscriptionRequest subReq = new SubscriptionRequest();
        subReq.setSubscribeRequest(service, getEventSubCallbackURL(ifAddress), timeout);
        SubscriptionResponse subRes = subReq.post();
        if (subRes.isSuccessful() == true) {
            service.setSID(subRes.getSID());
            service.setTimeout(subRes.getTimeout());
            return true;

        }
        service.clearSID();
        return false;
    }

    public boolean subscribe(Service service) {
        return subscribe(service, Subscription.INFINITE_VALUE);
    }

    public boolean subscribe(Service service, String uuid, long timeout) {
        SubscriptionRequest subReq = new SubscriptionRequest();
        subReq.setRenewRequest(service, uuid, timeout);
        if (Debug.isOn() == true) {
            subReq.print();
        }
        SubscriptionResponse subRes = subReq.post();
        if (Debug.isOn() == true) {
            subRes.print();
        }
        if (subRes.isSuccessful() == true) {
            service.setSID(subRes.getSID());
            service.setTimeout(subRes.getTimeout());
            return true;
        }
        service.clearSID();
        return false;
    }

    public boolean subscribe(Service service, String uuid) {
        return subscribe(service, uuid, Subscription.INFINITE_VALUE);
    }

    public boolean isSubscribed(Service service) {
        if (service == null) {
            return false;
        }
        return service.isSubscribed();
    }

    public boolean unsubscribe(Service service) {
        SubscriptionRequest subReq = new SubscriptionRequest();
        subReq.setUnsubscribeRequest(service);
        SubscriptionResponse subRes = subReq.post();
        if (subRes.isSuccessful() == true) {
            service.clearSID();
            return true;
        }
        return false;
    }

    public void unsubscribe(Device device) {
        ServiceList serviceList = device.getServiceList();
        int serviceCnt = serviceList.size();
        for (int n = 0; n < serviceCnt; n++) {
            Service service = serviceList.getService(n);
            if (service.hasSID() == true) {
                unsubscribe(service);
            }
        }

        DeviceList childDevList = device.getDeviceList();
        int childDevCnt = childDevList.size();
        for (int n = 0; n < childDevCnt; n++) {
            Device cdev = childDevList.getDevice(n);
            unsubscribe(cdev);
        }
    }

    public void unsubscribe() {
        DeviceList devList = getDeviceList();
        int devCnt = devList.size();
        for (int n = 0; n < devCnt; n++) {
            Device dev = devList.getDevice(n);
            unsubscribe(dev);
        }
    }

    ////////////////////////////////////////////////
    //	getSubscriberService
    ////////////////////////////////////////////////

    public Service getSubscriberService(String uuid) {
        DeviceList devList = getDeviceList();
        int devCnt = devList.size();
        for (int n = 0; n < devCnt; n++) {
            Device dev = devList.getDevice(n);
            Service service = dev.getSubscriberService(uuid);
            if (service != null) {
                return service;
            }
        }
        return null;
    }

    ////////////////////////////////////////////////
    //	getSubscriberService
    ////////////////////////////////////////////////

    public void renewSubscriberService(Device dev, long timeout) {
        ServiceList serviceList = dev.getServiceList();
        int serviceCnt = serviceList.size();
        for (int n = 0; n < serviceCnt; n++) {
            Service service = serviceList.getService(n);
            if (service.isSubscribed() == false) {
                continue;
            }
            String sid = service.getSID();
            boolean isRenewed = subscribe(service, sid, timeout);
            if (isRenewed == false) {
                subscribe(service, timeout);
            }
        }

        DeviceList cdevList = dev.getDeviceList();
        int cdevCnt = cdevList.size();
        for (int n = 0; n < cdevCnt; n++) {
            Device cdev = cdevList.getDevice(n);
            renewSubscriberService(cdev, timeout);
        }
    }

    public void renewSubscriberService(long timeout) {
        DeviceList devList = getDeviceList();
        int devCnt = devList.size();
        for (int n = 0; n < devCnt; n++) {
            Device dev = devList.getDevice(n);
            renewSubscriberService(dev, timeout);
        }
    }

    public void renewSubscriberService() {
        renewSubscriberService(Subscription.INFINITE_VALUE);
    }

    ////////////////////////////////////////////////
    //	Subscriber
    ////////////////////////////////////////////////

    private RenewSubscriber renewSubscriber;

    public void setRenewSubscriber(RenewSubscriber sub) {
        renewSubscriber = sub;
    }

    public RenewSubscriber getRenewSubscriber() {
        return renewSubscriber;
    }

    ////////////////////////////////////////////////
    //	run
    ////////////////////////////////////////////////

    public boolean start(String target, int mx) {
        LogUtils.e("dlna_framework", "start target = " + target + ", mx = " + mx);
        stop();

        ////////////////////////////////////////
        // HTTP Server
        ////////////////////////////////////////

        int retryCnt = 0;
        int bindPort = getHTTPPort();
        HTTPServerList httpServerList = getHTTPServerList();
        while (httpServerList.open(bindPort) == false) {
            retryCnt++;
            if (UPnP.SERVER_RETRY_COUNT < retryCnt) {
                return false;
            }
            setHTTPPort(bindPort + 1);
            bindPort = getHTTPPort();
        }
        httpServerList.addRequestListener(this);
        httpServerList.start();

        ////////////////////////////////////////
        // Notify Socket
        ////////////////////////////////////////

        SSDPNotifySocketList ssdpNotifySocketList = getSSDPNotifySocketList();
        if (ssdpNotifySocketList.open() == false) {
            return false;
        }
        ssdpNotifySocketList.setControlPoint(this);
        ssdpNotifySocketList.start();

        ////////////////////////////////////////
        // SeachResponse Socket
        ////////////////////////////////////////

        int ssdpPort = getSSDPPort();
        retryCnt = 0;
        SSDPSearchResponseSocketList ssdpSearchResponseSocketList = getSSDPSearchResponseSocketList();
        while (ssdpSearchResponseSocketList.open(ssdpPort) == false) {
            retryCnt++;
            if (UPnP.SERVER_RETRY_COUNT < retryCnt) {
                return false;
            }
            setSSDPPort(ssdpPort + 1);
            ssdpPort = getSSDPPort();
        }
        ssdpSearchResponseSocketList.setControlPoint(this);
        ssdpSearchResponseSocketList.start();

        ////////////////////////////////////////
        // search root devices
        ////////////////////////////////////////

        search(target, mx);

        ////////////////////////////////////////
        // Disposer
        ////////////////////////////////////////

        Disposer disposer = new Disposer(this);
        setDeviceDisposer(disposer);
        disposer.start();

        ////////////////////////////////////////
        // Subscriber
        ////////////////////////////////////////

        if (isNMPRMode() == true) {
            RenewSubscriber renewSub = new RenewSubscriber(this);
            setRenewSubscriber(renewSub);
            renewSub.start();
        }


        return true;
    }

    public boolean start(String target) {
        return start(target, SSDP.DEFAULT_MSEARCH_MX);
    }

    public boolean start() {
        return start(ST.ROOT_DEVICE, SSDP.DEFAULT_MSEARCH_MX);
    }

    public boolean stop() {
        LogUtils.e("dlna_framework" + "stop");
        unsubscribe();

        SSDPNotifySocketList ssdpNotifySocketList = getSSDPNotifySocketList();
        ssdpNotifySocketList.stop();
        ssdpNotifySocketList.close();
        ssdpNotifySocketList.clear();

        SSDPSearchResponseSocketList ssdpSearchResponseSocketList = getSSDPSearchResponseSocketList();
        ssdpSearchResponseSocketList.stop();
        ssdpSearchResponseSocketList.close();
        ssdpSearchResponseSocketList.clear();

        HTTPServerList httpServerList = getHTTPServerList();
        httpServerList.stop();
        httpServerList.close();
        httpServerList.clear();

        ////////////////////////////////////////
        // Disposer
        ////////////////////////////////////////

        Disposer disposer = getDeviceDisposer();
        if (disposer != null) {
            disposer.stop();
            setDeviceDisposer(null);
        }

        ////////////////////////////////////////
        // Subscriber
        ////////////////////////////////////////

        RenewSubscriber renewSub = getRenewSubscriber();
        if (renewSub != null) {
            renewSub.stop();
            setRenewSubscriber(null);
        }


        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LogUtils.e("dlna_framework", "ready to clear devNodeList...devNodeList.size = " + devNodeList.size());
        try {
            if (devNodeList != null) {
                synchronized (devNodeList) {
                    devNodeList = new NodeList();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }

    ////////////////////////////////////////////////
    //	userData
    ////////////////////////////////////////////////

    private Object userData = null;

    public void setUserData(Object data) {
        userData = data;
    }

    public Object getUserData() {
        return userData;
    }

    ////////////////////////////////////////////////
    //	print
    ////////////////////////////////////////////////

    public void print() {
        DeviceList devList = getDeviceList();
        int devCnt = devList.size();
        Debug.message("Device Num = " + devCnt);
        for (int n = 0; n < devCnt; n++) {
            Device dev = devList.getDevice(n);
            Debug.message(
                    "[" + n + "] " + dev.getFriendlyName() + ", " + dev.getLeaseTime() + ", " + dev.getElapsedTime());
        }
    }
}

