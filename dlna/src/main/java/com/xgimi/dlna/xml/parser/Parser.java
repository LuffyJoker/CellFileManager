package com.xgimi.dlna.xml.parser;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPRequest;
import com.xgimi.dlna.http.HTTPResponse;
import com.xgimi.dlna.xml.Node;
import com.xgimi.dlna.xml.ParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:55
 * desc   :
 */
public abstract class Parser {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public Parser() {
    }

    ////////////////////////////////////////////////
    //	parse
    ////////////////////////////////////////////////

    public abstract Node parse(InputStream inStream) throws ParserException;

    ////////////////////////////////////////////////
    //	parse (URL)
    ////////////////////////////////////////////////

    public Node parse(URL locationURL) throws ParserException {
        String host = locationURL.getHost();
        int port = locationURL.getPort();
        // Thanks for Hao Hu
        if (port == -1) {
            port = 80;
        }
        String uri = locationURL.getPath();

        try {
            HttpURLConnection urlCon = (HttpURLConnection) locationURL.openConnection();
            urlCon.setRequestMethod("GET");
            urlCon.setRequestProperty(HTTP.CONTENT_LENGTH, "0");
            if (host != null) {
                urlCon.setRequestProperty(HTTP.HOST, host);
            }

            InputStream urlIn = urlCon.getInputStream();

            Node rootElem = parse(urlIn);

            //	rootElem.print();

            urlIn.close();
            urlCon.disconnect();

            return rootElem;

        } catch (Exception e) {
            //throw new ParserException(e);
            // e.printStackTrace();
        }

        HTTPRequest httpReq = new HTTPRequest();
        httpReq.setMethod(HTTP.GET);
        httpReq.setURI(uri);
        HTTPResponse httpRes = httpReq.post(host, port);
        if (httpRes.isSuccessful() == false) {
            throw new ParserException("HTTP comunication failed: no answer from peer." +
                    "Unable to retrive resoure -> " + locationURL.toString());
        }
        String content = new String(httpRes.getContent());
        ByteArrayInputStream strBuf = new ByteArrayInputStream(content.getBytes());
        return parse(strBuf);
    }

    ////////////////////////////////////////////////
    //	parse (File)
    ////////////////////////////////////////////////

    public Node parse(File descriptionFile) throws ParserException {
        try {
            InputStream fileIn = new FileInputStream(descriptionFile);
            Node root = parse(fileIn);
            fileIn.close();
            return root;

        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

    ////////////////////////////////////////////////
    //	parse (Memory)
    ////////////////////////////////////////////////

    public Node parse(String descr) throws ParserException {
        try {
            InputStream decrIn = new ByteArrayInputStream(descr.getBytes());
            Node root = parse(decrIn);
            return root;
        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

}



