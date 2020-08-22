package com.xgimi.dlna.upnp.ssdp;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.utils.Debug;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:49
 * desc   :
 */
public class HTTPUSocket {
    ////////////////////////////////////////////////
    //	Member
    ////////////////////////////////////////////////

    private DatagramSocket ssdpUniSock = null;
    //private MulticastSocket ssdpUniSock = null;

    public DatagramSocket getDatagramSocket() {
        return ssdpUniSock;
    }

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public HTTPUSocket() {
        open();
    }

    public HTTPUSocket(String bindAddr, int bindPort) {
        open(bindAddr, bindPort);
    }

    public HTTPUSocket(int bindPort) {
        open(bindPort);
    }

    protected void finalize() {
        close();
    }

    ////////////////////////////////////////////////
    //	bindAddr
    ////////////////////////////////////////////////

    private String localAddr = "";

    public void setLocalAddress(String addr) {
        localAddr = addr;
    }

    /**
     * @return {@link DatagramSocket} open for receieving packets
     *
     * @since 1.8
     */
    public DatagramSocket getUDPSocket() {
        return ssdpUniSock;
    }

    public String getLocalAddress() {
        if (0 < localAddr.length()) {
            return localAddr;
        }
        return ssdpUniSock.getLocalAddress().getHostAddress();
    }

    ////////////////////////////////////////////////
    //	open
    ////////////////////////////////////////////////

    public boolean open() {
        close();

        try {
            ssdpUniSock = new DatagramSocket();
        } catch (Exception e) {
            Debug.warning(e);
            return false;
        }

        return true;
    }

    public boolean open(String bindAddr, int bindPort) {
        close();

        try {
            // Changed to bind the specified address and port for Android v1.6 (2009/10/07)
            InetSocketAddress bindInetAddr = new InetSocketAddress(InetAddress.getByName(bindAddr), bindPort);
            ssdpUniSock = new DatagramSocket(bindInetAddr);

        } catch (Exception e) {
            Debug.warning(e);
            return false;
        }

		/*
		try {
			// Bind only using the port without the interface address. (2003/12/12)
			InetSocketAddress bindInetAddr = new InetSocketAddress(bindPort);
			ssdpUniSock = new DatagramSocket(null);
			ssdpUniSock.setReuseAddress(true);
			ssdpUniSock.bind(bindInetAddr);
			return true;
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		*/

        setLocalAddress(bindAddr);

        return true;
    }

    public boolean open(int bindPort) {
        close();

        try {
            InetSocketAddress bindSock = new InetSocketAddress(bindPort);
            ssdpUniSock = new DatagramSocket(null);
            ssdpUniSock.setReuseAddress(true);
            ssdpUniSock.bind(bindSock);
        } catch (Exception e) {
            //Debug.warning(e);
            return false;
        }

        return true;
    }

    ////////////////////////////////////////////////
    //	close
    ////////////////////////////////////////////////

    public boolean close() {
        if (ssdpUniSock == null) {
            return true;
        }

        try {
            ssdpUniSock.close();
            ssdpUniSock = null;
        } catch (Exception e) {
            Debug.warning(e);
            return false;
        }

        return true;
    }

    ////////////////////////////////////////////////
    //	send
    ////////////////////////////////////////////////

    public boolean post(String addr, int port, String msg) {
        try {
            InetAddress inetAddr = InetAddress.getByName(addr);
            DatagramPacket dgmPacket = new DatagramPacket(msg.getBytes(), msg.length(), inetAddr, port);
            ssdpUniSock.send(dgmPacket);
            LogUtils.d("dlna_framework", "send to " + inetAddr.toString() + ", port = " + port);
        } catch (Exception e) {
            Debug.warning("addr = " + ssdpUniSock.getLocalAddress().getHostName());
            Debug.warning("port = " + ssdpUniSock.getLocalPort());
            Debug.warning(e);
            LogUtils.d("dlna_framework", "addr = " + ssdpUniSock.getLocalAddress().getHostName());
            LogUtils.d("dlna_framework", "port = " + ssdpUniSock.getLocalPort());
            LogUtils.e("dlna_framework", e);
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////
    //	reveive
    ////////////////////////////////////////////////

    public SSDPPacket receive() {
        byte ssdvRecvBuf[] = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
        SSDPPacket recvPacket = new SSDPPacket(ssdvRecvBuf, ssdvRecvBuf.length);
        recvPacket.setLocalAddress(getLocalAddress());
        try {
            ssdpUniSock.receive(recvPacket.getDatagramPacket());
            recvPacket.setTimeStamp(System.currentTimeMillis());
        } catch (Exception e) {
            //Debug.warning(e);
            return null;
        }
        return recvPacket;
    }

    ////////////////////////////////////////////////
    //	join/leave
    ////////////////////////////////////////////////

/*
	boolean joinGroup(String mcastAddr, int mcastPort, String bindAddr)
	{
		try {
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.joinGroup(mcastGroup, mcastIf);
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		return true;
	}

	boolean leaveGroup(String mcastAddr, int mcastPort, String bindAddr)
	 {
		try {
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.leaveGroup(mcastGroup, mcastIf);
		 }
		 catch (Exception e) {
			 Debug.warning(e);
			 return false;
		 }
		 return true;
	 }
*/
}


