package com.xgimi.dlna.upnp.control;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPStatus;
import com.xgimi.dlna.soap.SOAP;
import com.xgimi.dlna.soap.SOAPResponse;
import com.xgimi.dlna.upnp.Action;
import com.xgimi.dlna.upnp.Argument;
import com.xgimi.dlna.upnp.ArgumentList;
import com.xgimi.dlna.upnp.Service;
import com.xgimi.dlna.xml.Node;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:32
 * desc   :
 */
public class ActionResponse extends ControlResponse {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public ActionResponse() {
        setHeader(HTTP.EXT, "");
    }

    public ActionResponse(SOAPResponse soapRes) {
        super(soapRes);
        setHeader(HTTP.EXT, "");
    }


    ////////////////////////////////////////////////
    //	Response
    ////////////////////////////////////////////////

    public void setResponse(Action action) {
        setStatusCode(HTTPStatus.OK);

        Node bodyNode = getBodyNode();
        Node resNode = createResponseNode(action);
        bodyNode.addNode(resNode);

        Node envNode = getEnvelopeNode();
        setContent(envNode);
    }

    private Node createResponseNode(Action action) {
        String actionName = action.getName();
        Node actionNameResNode = new Node(SOAP.METHODNS + SOAP.DELIM + actionName + SOAP.RESPONSE);

        Service service = action.getService();
        if (service != null) {
            actionNameResNode.setAttribute(
                    "xmlns:" + SOAP.METHODNS,
                    service.getServiceType());
        }

        ArgumentList argList = action.getArgumentList();
        int nArgs = argList.size();
        for (int n = 0; n < nArgs; n++) {
            Argument arg = argList.getArgument(n);
            if (arg.isOutDirection() == false) {
                continue;
            }
            Node argNode = new Node();
            argNode.setName(arg.getName());
            argNode.setValue(arg.getValue());
            actionNameResNode.addNode(argNode);
        }

        return actionNameResNode;
    }

    ////////////////////////////////////////////////
    //	getResponse
    ////////////////////////////////////////////////

    private Node getActionResponseNode() {
        Node bodyNode = getBodyNode();
        if (bodyNode == null || bodyNode.hasNodes() == false) {
            return null;
        }
        return bodyNode.getNode(0);
    }


    public ArgumentList getResponse() {
        ArgumentList argList = new ArgumentList();

        Node resNode = getActionResponseNode();
        if (resNode == null) {
            return argList;
        }

        int nArgs = resNode.getNNodes();
        for (int n = 0; n < nArgs; n++) {
            Node node = resNode.getNode(n);
            String name = node.getName();
            String value = node.getValue();
            Argument arg = new Argument(name, value);
            argList.add(arg);
        }

        return argList;
    }
}

