package com.xgimi.samba.core;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.xgimi.samba.ShareItem;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:24
 * desc   :
 */
public class ShareClient {

    private static volatile SMBClient mSmbClient = null;
    /**
     * New or reused mConnection.
     */
    private final Connection mConnection;
    private final Session mSession;
    private final String mServiceName;
    private final String mNetBiosName;
    private final AuthenticationContext mAuthenticationContext;

    public static void closeAllConnect() {
        synchronized (ShareClient.class) {
            if (mSmbClient != null) {
                mSmbClient.close();
                mSmbClient = null;
            }
        }
    }

    //TransactBufferSize过高会造成溢出
    private static final int TRANSACT_BUFFER_SIZE = 2048 * 64;
    public ShareClient(String host, AuthenticationContext authenticationContext) throws Exception {
        synchronized (ShareClient.class) {
            if (mSmbClient == null) {
                SmbConfig config = SmbConfig.builder()
                                            .withTransactBufferSize(TRANSACT_BUFFER_SIZE)
                                            .withDfsEnabled(true).build();
                mSmbClient = new SMBClient(config);
            }
        }
        mAuthenticationContext = authenticationContext;
        mServiceName = host;
        mConnection = mSmbClient.connect(mServiceName);
        mNetBiosName = mConnection.getConnectionInfo().getNetBiosName();
        mSession = mConnection.authenticate(mAuthenticationContext);
        cancelSignatureVerification();
    }

    public ShareClient(String host, String name, String password, String domain) throws Exception {
        this(host, new AuthenticationContext(name, password.toCharArray(), domain));
    }

    /**
     * 取消数据包签名校验
     */
    private void cancelSignatureVerification() {
        // PacketSignatory packetSignatory = Reflect.on(mSession).get("packetSignatory");
        // ProxyPacketSignatory proxyPacketSignatory = new ProxyPacketSignatory(packetSignatory);
        // Reflect.on(mSession).set("packetSignatory", proxyPacketSignatory);
    }

    public ShareItem getRootShareItem() {
        // return new ShareRoot(this);
        return null;
    }

    /**
     * Get the server name of the server.
     *
     * @return Name of the server
     */
    public String getServerName() {
        return mServiceName;
    }

    public String getNetBiosName() {
        return mNetBiosName;
    }

    public Session getSession() {
        return mSession;
    }

    public void startNanoStreamer() {
        // HttpBean.setmName(mAuthenticationContext.getUsername());
        // HttpBean.setmPassword(
        //         String.valueOf(mAuthenticationContext.getPassword()));
        // try {
        //     NanoStreamer.INSTANCE().start();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    public void closeConnect() {
        try {
            if (mSession != null) {
                mSession.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

