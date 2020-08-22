package com.xgimi.dlna.upnp.control;

import com.xgimi.dlna.http.HTTPRequest;
import com.xgimi.dlna.soap.SOAP;
import com.xgimi.dlna.soap.SOAPResponse;
import com.xgimi.dlna.upnp.Service;
import com.xgimi.dlna.upnp.StateVariable;
import com.xgimi.dlna.xml.Node;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:38
 * desc   :
 */
public class QueryRequest extends ControlRequest {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public QueryRequest() {
    }

    public QueryRequest(HTTPRequest httpReq) {
        set(httpReq);
    }

    ////////////////////////////////////////////////
    //	Qyery
    ////////////////////////////////////////////////

    private Node getVarNameNode() {
        Node bodyNode = getBodyNode();
        if (bodyNode == null) {
            return null;
        }
        if (bodyNode.hasNodes() == false) {
            return null;
        }
        Node queryStateVarNode = bodyNode.getNode(0);
        if (queryStateVarNode == null) {
            return null;
        }
        if (queryStateVarNode.hasNodes() == false) {
            return null;
        }
        return queryStateVarNode.getNode(0);
    }

    public String getVarName() {
        Node node = getVarNameNode();
        if (node == null) {
            return "";
        }
        return node.getValue();
    }

    ////////////////////////////////////////////////
    //	setRequest
    ////////////////////////////////////////////////

    public void setRequest(StateVariable stateVar) {
        Service service = stateVar.getService();

        String ctrlURL = service.getControlURL();

        setRequestHost(service);

        setEnvelopeNode(SOAP.createEnvelopeBodyNode());
        Node envNode = getEnvelopeNode();
        Node bodyNode = getBodyNode();
        Node qeuryNode = createContentNode(stateVar);
        bodyNode.addNode(qeuryNode);
        setContent(envNode);

        setSOAPAction(Control.QUERY_SOAPACTION);
    }

    ////////////////////////////////////////////////
    //	Contents
    ////////////////////////////////////////////////

    private Node createContentNode(StateVariable stateVar) {
        Node queryVarNode = new Node();
        queryVarNode.setName(Control.NS, Control.QUERY_STATE_VARIABLE);
        queryVarNode.setNameSpace(Control.NS, Control.XMLNS);

        Node varNode = new Node();
        varNode.setName(Control.NS, Control.VAR_NAME);
        varNode.setValue(stateVar.getName());
        queryVarNode.addNode(varNode);

        return queryVarNode;
    }

    ////////////////////////////////////////////////
    //	post
    ////////////////////////////////////////////////

    public QueryResponse post() {
        SOAPResponse soapRes = postMessage(getRequestHost(), getRequestPort());
        return new QueryResponse(soapRes);
    }
}


