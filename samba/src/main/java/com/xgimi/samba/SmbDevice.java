package com.xgimi.samba;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:11
 * desc   : samba 设备对象
 */
public class SmbDevice {
    public String ip;
    public String userName;
    public String password;
    public String name;

    public SmbDevice(String name, String ip, String userName, String password) {
        this.name = name;
        this.ip = ip;
        this.userName = userName;
        this.password = password;
    }

    public SmbDevice(String ipAddress) {
        this.ip = ipAddress;
        this.userName = "";
        this.password = "";
        this.name = "";
    }

    @Override
    public String toString() {
        return "SmbDevice{" +
                "ip='" + ip + '\'' +
                ", userName='" + userName + '\'' +
                ", passward='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

