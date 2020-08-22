package com.xgimi.dlna.upnp;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPResponse;
import com.xgimi.dlna.upnp.control.ActionListener;
import com.xgimi.dlna.upnp.control.QueryListener;
import com.xgimi.dlna.upnp.device.InvalidDescriptionException;
import com.xgimi.dlna.upnp.device.NTS;
import com.xgimi.dlna.upnp.device.ST;
import com.xgimi.dlna.upnp.event.NotifyRequest;
import com.xgimi.dlna.upnp.event.Subscriber;
import com.xgimi.dlna.upnp.event.SubscriberList;
import com.xgimi.dlna.upnp.ssdp.SSDPNotifyRequest;
import com.xgimi.dlna.upnp.ssdp.SSDPNotifySocket;
import com.xgimi.dlna.upnp.ssdp.SSDPPacket;
import com.xgimi.dlna.upnp.xml.ServiceData;
import com.xgimi.dlna.utils.Debug;
import com.xgimi.dlna.utils.Mutex;
import com.xgimi.dlna.utils.StringUtil;
import com.xgimi.dlna.xml.Node;
import com.xgimi.dlna.xml.ParserException;
import com.xgimi.dlna.xml.parser.Parser;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 18:01
 * desc   :
 */
public class Service {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "service";

    ////////////////////////////////////////////////
    //	Member
    ////////////////////////////////////////////////

    private Node serviceNode;

    public Node getServiceNode() {
        return serviceNode;
    }

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////
    public static final String SCPD_ROOTNODE = "scpd";
    public static final String SCPD_ROOTNODE_NS = "urn:schemas-upnp-org:service-1-0";

    public static final String SPEC_VERSION = "specVersion";
    public static final String MAJOR = "major";
    public static final String MAJOR_VALUE = "1";
    public static final String MINOR = "minor";
    public static final String MINOR_VALUE = "0";

    public Service() {
        this(new Node(ELEM_NAME));

        Node sp = new Node(SPEC_VERSION);

        Node M = new Node(MAJOR);
        M.setValue(MAJOR_VALUE);
        sp.addNode(M);

        Node m = new Node(MINOR);
        m.setValue(MINOR_VALUE);
        sp.addNode(m);

        //Node scpd = new Node(SCPD_ROOTNODE,SCPD_ROOTNODE_NS); wrong!
        Node scpd = new Node(SCPD_ROOTNODE);                    // better (twa)
        scpd.addAttribute("xmlns", SCPD_ROOTNODE_NS);            // better (twa)
        scpd.addNode(sp);
        getServiceData().setSCPDNode(scpd);
    }

    public Service(Node node) {
        serviceNode = node;
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
    //	isServiceNode
    ////////////////////////////////////////////////

    public static boolean isServiceNode(Node node) {
        return Service.ELEM_NAME.equals(node.getName());
    }

    ////////////////////////////////////////////////
    //	Device/Root Node
    ////////////////////////////////////////////////

    private Node getDeviceNode() {
        Node node = getServiceNode().getParentNode();
        if (node == null) {
            return null;
        }
        return node.getParentNode();
    }

    private Node getRootNode() {
        return getServiceNode().getRootNode();
    }

    ////////////////////////////////////////////////
    //	Device
    ////////////////////////////////////////////////

    public Device getDevice() {
        return new Device(getRootNode(), getDeviceNode());
    }

    public Device getRootDevice() {
        return getDevice().getRootDevice();
    }

    ////////////////////////////////////////////////
    //	serviceType
    ////////////////////////////////////////////////

    private final static String SERVICE_TYPE = "serviceType";

    public void setServiceType(String value) {
        getServiceNode().setNode(SERVICE_TYPE, value);
    }

    public String getServiceType() {
        return getServiceNode().getNodeValue(SERVICE_TYPE);
    }

    ////////////////////////////////////////////////
    //	serviceID
    ////////////////////////////////////////////////

    private final static String SERVICE_ID = "serviceId";

    public void setServiceID(String value) {
        getServiceNode().setNode(SERVICE_ID, value);
    }

    public String getServiceID() {
        return getServiceNode().getNodeValue(SERVICE_ID);
    }

    ////////////////////////////////////////////////
    //	isURL
    ////////////////////////////////////////////////

    // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/03/03)
    private boolean isURL(String referenceUrl, String url) {
        if (referenceUrl == null || url == null) {
            return false;
        }
        boolean ret = url.equals(referenceUrl);
        if (ret == true) {
            return true;
        }
        String relativeRefUrl = HTTP.toRelativeURL(referenceUrl, false);
        ret = url.equals(relativeRefUrl);
        return ret == true;
    }

    ////////////////////////////////////////////////
    //	SCPDURL
    ////////////////////////////////////////////////

    private final static String SCPDURL = "SCPDURL";

    public void setSCPDURL(String value) {
        getServiceNode().setNode(SCPDURL, value);
    }

    public String getSCPDURL() {
        return getServiceNode().getNodeValue(SCPDURL);
    }

    public boolean isSCPDURL(String url) {
        return isURL(getSCPDURL(), url);
    }

    ////////////////////////////////////////////////
    //	controlURL
    ////////////////////////////////////////////////

    private final static String CONTROL_URL = "controlURL";

    public void setControlURL(String value) {
        getServiceNode().setNode(CONTROL_URL, value);
    }

    public String getControlURL() {
        return getServiceNode().getNodeValue(CONTROL_URL);
    }

    public boolean isControlURL(String url) {
        return isURL(getControlURL(), url);
    }

    ////////////////////////////////////////////////
    //	eventSubURL
    ////////////////////////////////////////////////

    private final static String EVENT_SUB_URL = "eventSubURL";

    public void setEventSubURL(String value) {
        getServiceNode().setNode(EVENT_SUB_URL, value);
    }

    public String getEventSubURL() {
        return getServiceNode().getNodeValue(EVENT_SUB_URL);
    }

    public boolean isEventSubURL(String url) {
        return isURL(getEventSubURL(), url);
    }

    ////////////////////////////////////////////////
    //	SCPD node
    ////////////////////////////////////////////////

    public boolean loadSCPD(String scpdStr) throws InvalidDescriptionException {
        try {
            Parser parser = UPnP.getXMLParser();
            Node scpdNode = parser.parse(scpdStr);
            if (scpdNode == null) {
                return false;
            }
            ServiceData data = getServiceData();
            data.setSCPDNode(scpdNode);
        } catch (ParserException e) {
            throw new InvalidDescriptionException(e);
        }
        return true;
    }

    public boolean loadSCPD(File file) throws ParserException {
        Parser parser = UPnP.getXMLParser();
        Node scpdNode = parser.parse(file);
        if (scpdNode == null) {
            return false;
        }
        ServiceData data = getServiceData();
        data.setSCPDNode(scpdNode);
        return true;
    }

    /**
     * @since 1.8.0
     */
    public boolean loadSCPD(InputStream input) throws ParserException {
        Parser parser = UPnP.getXMLParser();
        Node scpdNode = parser.parse(input);
        if (scpdNode == null) {
            return false;
        }
        ServiceData data = getServiceData();
        data.setSCPDNode(scpdNode);
        return true;
    }


    public void setDescriptionURL(String value) {
        getServiceData().setDescriptionURL(value);
    }

    public String getDescriptionURL() {
        return getServiceData().getDescriptionURL();
    }


    private Node getSCPDNode(URL scpdUrl) throws ParserException {
        Parser parser = UPnP.getXMLParser();
        return parser.parse(scpdUrl);
    }

    private Node getSCPDNode(File scpdFile) throws ParserException {
        Parser parser = UPnP.getXMLParser();
        return parser.parse(scpdFile);
    }


    private Node getSCPDNode() {
        ServiceData data = getServiceData();
        Node scpdNode = data.getSCPDNode();
        if (scpdNode != null) {
            return scpdNode;
        }

        Device rootDev = getDevice();
        if (rootDev == null) {
            return null;
        }

        String scpdURLStr = getSCPDURL();
        //		log.e("scpdURLStr = " + scpdURLStr);
        try {
            URL scpdUrl = new URL(rootDev.getAbsoluteURL(scpdURLStr));
            //			log.e("scpdUrl = " + scpdUrl.toString());
            scpdNode = getSCPDNode(scpdUrl);
            if (scpdNode != null) {
                data.setSCPDNode(scpdNode);
                return scpdNode;
            }
        } catch (Exception e) {
        }

        String newScpdURLStr = rootDev.getDescriptionFilePath() + HTTP.toRelativeURL(scpdURLStr);
        //		log.e("newScpdURLStr = " + newScpdURLStr);
        try {
            scpdNode = getSCPDNode(new File(newScpdURLStr));
        } catch (Exception e) {
            Debug.warning(e);
        }

        return null;
    }


    public byte[] getSCPDData() {
        Node scpdNode = getSCPDNode();
        if (scpdNode == null) {
            return new byte[0];
        }
        // Thanks for Mikael Hakman (04/25/05)
        String desc = new String();
        desc += UPnP.XML_DECLARATION;
        desc += "\n";
        desc += scpdNode.toString();
        return desc.getBytes();
    }

    ////////////////////////////////////////////////
    //	actionList
    ////////////////////////////////////////////////

    public ActionList getActionList() {
        ActionList actionList = new ActionList();
        Node scdpNode = getSCPDNode();
        if (scdpNode == null) {
            return actionList;
        }
        Node actionListNode = scdpNode.getNode(ActionList.ELEM_NAME);
        if (actionListNode == null) {
            return actionList;
        }
        int nNode = actionListNode.getNNodes();
        for (int n = 0; n < nNode; n++) {
            Node node = actionListNode.getNode(n);
            if (Action.isActionNode(node) == false) {
                continue;
            }
            Action action = new Action(serviceNode, node);
            actionList.add(action);
        }
        return actionList;
    }

    public Action getAction(String actionName) {
        ActionList actionList = getActionList();
        int nActions = actionList.size();
        for (int n = 0; n < nActions; n++) {
            Action action = actionList.getAction(n);
            String name = action.getName();
            if (name == null) {
                continue;
            }
            if (name.equals(actionName) == true) {
                return action;
            }
        }
        return null;
    }

    public void addAction(Action a) {
        Iterator i = a.getArgumentList().iterator();
        while (i.hasNext()) {
            Argument arg = (Argument) i.next();
            arg.setService(this);
        }

        Node scdpNode = getSCPDNode();
        Node actionListNode = scdpNode.getNode(ActionList.ELEM_NAME);
        if (actionListNode == null) {
            actionListNode = new Node(ActionList.ELEM_NAME);
            scdpNode.addNode(actionListNode);
        }
        actionListNode.addNode(a.getActionNode());
    }

    ////////////////////////////////////////////////
    //	serviceStateTable
    ////////////////////////////////////////////////

    public ServiceStateTable getServiceStateTable() {
        ServiceStateTable stateTable = new ServiceStateTable();
        Node stateTableNode = getSCPDNode().getNode(ServiceStateTable.ELEM_NAME);
        if (stateTableNode == null) {
            return stateTable;
        }
        Node serviceNode = getServiceNode();
        int nNode = stateTableNode.getNNodes();
        for (int n = 0; n < nNode; n++) {
            Node node = stateTableNode.getNode(n);
            if (StateVariable.isStateVariableNode(node) == false) {
                continue;
            }
            StateVariable serviceVar = new StateVariable(serviceNode, node);
            stateTable.add(serviceVar);
        }
        return stateTable;
    }

    public StateVariable getStateVariable(String name) {
        ServiceStateTable stateTable = getServiceStateTable();
        int tableSize = stateTable.size();
        for (int n = 0; n < tableSize; n++) {
            StateVariable var = stateTable.getStateVariable(n);
            String varName = var.getName();
            if (varName == null) {
                continue;
            }
            if (varName.equals(name) == true) {
                return var;
            }
        }
        return null;
    }

    public boolean hasStateVariable(String name) {
        return (getStateVariable(name) != null);
    }

    ////////////////////////////////////////////////
    //	UserData
    ////////////////////////////////////////////////

    public boolean isService(String name) {
        if (name == null) {
            return false;
        }
        if (name.endsWith(getServiceType()) == true) {
            return true;
        }
        return name.endsWith(getServiceID()) == true;
    }

    ////////////////////////////////////////////////
    //	UserData
    ////////////////////////////////////////////////

    private ServiceData getServiceData() {
        Node node = getServiceNode();
        ServiceData userData = (ServiceData) node.getUserData();
        if (userData == null) {
            userData = new ServiceData();
            node.setUserData(userData);
            userData.setNode(node);
        }
        return userData;
    }

    ////////////////////////////////////////////////
    //	Notify
    ////////////////////////////////////////////////

    private String getNotifyServiceTypeNT() {
        return getServiceType();
    }

    private String getNotifyServiceTypeUSN() {
        return getDevice().getUDN() + "::" + getServiceType();
    }

    public void announce(String bindAddr) {
        // uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:v
        Device rootDev = getRootDevice();
        String devLocation = rootDev.getLocationURL(bindAddr);
        String serviceNT = getNotifyServiceTypeNT();
        String serviceUSN = getNotifyServiceTypeUSN();

        Device dev = getDevice();

        SSDPNotifyRequest ssdpReq = new SSDPNotifyRequest();
        ssdpReq.setServer(UPnP.getServerName());
        ssdpReq.setLeaseTime(dev.getLeaseTime());
        ssdpReq.setLocation(devLocation);
        ssdpReq.setNTS(NTS.ALIVE);
        ssdpReq.setNT(serviceNT);
        ssdpReq.setUSN(serviceUSN);

        SSDPNotifySocket ssdpSock = new SSDPNotifySocket(bindAddr);
        Device.notifyWait();
        ssdpSock.post(ssdpReq);
    }

    public void byebye(String bindAddr) {
        // uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:v

        String devNT = getNotifyServiceTypeNT();
        String devUSN = getNotifyServiceTypeUSN();

        SSDPNotifyRequest ssdpReq = new SSDPNotifyRequest();
        ssdpReq.setNTS(NTS.BYEBYE);
        ssdpReq.setNT(devNT);
        ssdpReq.setUSN(devUSN);

        SSDPNotifySocket ssdpSock = new SSDPNotifySocket(bindAddr);
        Device.notifyWait();
        ssdpSock.post(ssdpReq);
    }

    public boolean serviceSearchResponse(SSDPPacket ssdpPacket) {
        String ssdpST = ssdpPacket.getST();

        if (ssdpST == null) {
            return false;
        }

        Device dev = getDevice();

        String serviceNT = getNotifyServiceTypeNT();
        String serviceUSN = getNotifyServiceTypeUSN();

        if (ST.isAllDevice(ssdpST) == true) {
            dev.postSearchResponse(ssdpPacket, serviceNT, serviceUSN);
        } else if (ST.isURNService(ssdpST) == true) {
            String serviceType = getServiceType();
            if (ssdpST.equals(serviceType) == true) {
                dev.postSearchResponse(ssdpPacket, serviceType, serviceUSN);
            }
        }

        return true;
    }

    ////////////////////////////////////////////////
    // QueryListener
    ////////////////////////////////////////////////

    public void setQueryListener(QueryListener queryListener) {
        ServiceStateTable stateTable = getServiceStateTable();
        int tableSize = stateTable.size();
        for (int n = 0; n < tableSize; n++) {
            StateVariable var = stateTable.getStateVariable(n);
            var.setQueryListener(queryListener);
        }
    }

    ////////////////////////////////////////////////
    //	Subscription
    ////////////////////////////////////////////////

    public SubscriberList getSubscriberList() {
        return getServiceData().getSubscriberList();
    }

    public void addSubscriber(Subscriber sub) {
        getSubscriberList().add(sub);
    }

    public void removeSubscriber(Subscriber sub) {
        getSubscriberList().remove(sub);
    }

    public Subscriber getSubscriber(String name) {
        SubscriberList subList = getSubscriberList();
        int subListCnt = subList.size();
        for (int n = 0; n < subListCnt; n++) {
            Subscriber sub = subList.getSubscriber(n);
            if (sub == null) {
                continue;
            }
            String sid = sub.getSID();
            if (sid == null) {
                continue;
            }
            if (sid.equals(name) == true) {
                return sub;
            }
        }
        return null;
    }

    private boolean notify(Subscriber sub, StateVariable stateVar) {
        String varName = stateVar.getName();
        String value = stateVar.getValue();

        String host = sub.getDeliveryHost();
        int port = sub.getDeliveryPort();

        NotifyRequest notifyReq = new NotifyRequest();
        notifyReq.setRequest(sub, varName, value);

        HTTPResponse res = notifyReq.post(host, port);
        if (res.isSuccessful() == false) {
            return false;
        }

        sub.incrementNotifyCount();

        return true;
    }

    public void notify(StateVariable stateVar) {
        SubscriberList subList = getSubscriberList();
        int subListCnt;
        Subscriber subs[];

        // Remove expired subscribers.
        subListCnt = subList.size();
        subs = new Subscriber[subListCnt];
        for (int n = 0; n < subListCnt; n++) {
            subs[n] = subList.getSubscriber(n);
        }
        for (int n = 0; n < subListCnt; n++) {
            Subscriber sub = subs[n];
            if (sub == null) {
                continue;
            }
            if (sub.isExpired() == true) {
                removeSubscriber(sub);
            }
        }

        // Notify to subscribers.
        subListCnt = subList.size();
        subs = new Subscriber[subListCnt];
        for (int n = 0; n < subListCnt; n++) {
            subs[n] = subList.getSubscriber(n);
        }
        for (int n = 0; n < subListCnt; n++) {
            Subscriber sub = subs[n];
            if (sub == null) {
                continue;
            }
            if (notify(sub, stateVar) == false) {
				/* Don't remove for NMPR specification.
				removeSubscriber(sub);
				*/
            }
        }
    }

    public void notifyAllStateVariables() {
        ServiceStateTable stateTable = getServiceStateTable();
        int tableSize = stateTable.size();
        for (int n = 0; n < tableSize; n++) {
            StateVariable var = stateTable.getStateVariable(n);
            if (var.isSendEvents() == true) {
                notify(var);
            }
        }
    }

    ////////////////////////////////////////////////
    // SID
    ////////////////////////////////////////////////

    public String getSID() {
        return getServiceData().getSID();
    }

    public void setSID(String id) {
        getServiceData().setSID(id);
    }

    public void clearSID() {
        setSID("");
        setTimeout(0);
    }

    public boolean hasSID() {
        return StringUtil.hasData(getSID());
    }

    public boolean isSubscribed() {
        return hasSID();
    }

    ////////////////////////////////////////////////
    // Timeout
    ////////////////////////////////////////////////

    public long getTimeout() {
        return getServiceData().getTimeout();
    }

    public void setTimeout(long value) {
        getServiceData().setTimeout(value);
    }

    ////////////////////////////////////////////////
    // AcionListener
    ////////////////////////////////////////////////

    public void setActionListener(ActionListener listener) {
        ActionList actionList = getActionList();
        int nActions = actionList.size();
        for (int n = 0; n < nActions; n++) {
            Action action = actionList.getAction(n);
            action.setActionListener(listener);
        }
    }

    /**
     * Add the StateVariable to the service.<br>
     * <br>
     * Note: This method should be used to create a dynamic<br>
     * Device withtout writing any XML that describe the device<br>.
     * <br>
     * Note: that no control for duplicate StateVariable is done.
     *
     * @param var StateVariable that will be added
     * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net  - 2005
     */
    public void addStateVariable(StateVariable var) {
        //TODO Some test are done not stable
        Node stateTableNode = getSCPDNode().getNode(ServiceStateTable.ELEM_NAME);
        if (stateTableNode == null) {
            stateTableNode = new Node(ServiceStateTable.ELEM_NAME);
            /*
             * Force the node <serviceStateTable> to be the first node inside <scpd>
             */
            //getSCPDNode().insertNode(stateTableNode,0);
            getSCPDNode().addNode(stateTableNode);
        }
        var.setServiceNode(getServiceNode());
        stateTableNode.addNode(var.getStateVariableNode());
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
}

