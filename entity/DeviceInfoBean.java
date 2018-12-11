package com.iptv.hn.entity;

/**
 * Created by zhuxi on 2017/11/14.
 */
public class DeviceInfoBean {
    private String user_id;
    private String user_token;
    private String mac_addr;
    private String ip_addr;
    private String model;
    private String gid;

    //    public DeviceInfoBean(String user_id, String user_token, String mac_addr, String ip_addr) {
//        this.user_id = user_id;
//        this.user_token = user_token;
//        this.mac_addr = mac_addr;
//        this.ip_addr = ip_addr;
//        this.model = model;
//    }


    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_token() {
        return user_token;
    }

    public void setUser_token(String user_token) {
        this.user_token = user_token;
    }

    public String getMac_addr() {
        return mac_addr;
    }

    public void setMac_addr(String mac_addr) {
        this.mac_addr = mac_addr;
    }

    public String getIp_addr() {
        return ip_addr;
    }

    public void setIp_addr(String ip_addr) {
        this.ip_addr = ip_addr;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return "DeviceInfoBean{" +
                "user_id='" + user_id + '\'' +
                ", user_token='" + user_token + '\'' +
                ", mac_addr='" + mac_addr + '\'' +
                ", ip_addr='" + ip_addr + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
