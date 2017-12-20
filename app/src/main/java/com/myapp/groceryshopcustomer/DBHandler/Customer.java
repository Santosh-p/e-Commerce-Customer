package com.myapp.groceryshopcustomer.DBHandler;

/**
 * Created by apple on 8/2/17.
 */

public class Customer {
    String DeviceId;
    int cust_id;
    String cust_name, emaiid, login_status;


    public Customer() {
    }

    public Customer(String deviceId, int cust_id, String cust_name, String emaiid, String login_status) {
        DeviceId = deviceId;
        this.cust_id = cust_id;
        this.cust_name = cust_name;
        this.emaiid = emaiid;
        this.login_status = login_status;
    }

    public Customer(String emaiid, int cust_id) {
        this.emaiid = emaiid;
        this.cust_id = cust_id;
    }

    public Customer(int cust_id) {
        this.cust_id = cust_id;
    }

    public Customer(int cust_id, String cust_name, String emaiid) {
        this.cust_id = cust_id;
        this.cust_name = cust_name;
        this.emaiid = emaiid;
    }

    public Customer(int cust_id, String login_status) {
        this.cust_id = cust_id;
        this.login_status = login_status;
    }

    public String getLogin_status() {
        return login_status;
    }

    public void setLogin_status(String login_status) {
        this.login_status = login_status;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getEmaiid() {
        return emaiid;
    }

    public void setEmaiid(String emaiid) {
        this.emaiid = emaiid;
    }

    public int getCust_id() {
        return cust_id;
    }

    public void setCust_id(int cust_id) {
        this.cust_id = cust_id;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }
}
