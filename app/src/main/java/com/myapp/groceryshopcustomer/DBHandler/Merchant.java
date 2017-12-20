package com.myapp.groceryshopcustomer.DBHandler;

/**
 * Created by apple on 8/7/17.
 */

public class Merchant {
    int mid;
    String shopName,shopAddress,mobileNumber,emailId,shopImage,city,area,providesDelivery,distance;

    public Merchant() {
    }

    public Merchant(String shopName, String shopAddress, String mobileNumber, String emailId, String shopImage, String city, String area, String providesDelivery, String distance) {
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.mobileNumber = mobileNumber;
        this.emailId = emailId;
        this.shopImage = shopImage;
        this.city = city;
        this.area = area;
        this.providesDelivery = providesDelivery;
        this.distance = distance;
    }

    public Merchant(int mid, String shopName, String shopAddress, String mobileNumber, String emailId, String shopImage, String city, String area, String providesDelivery, String distance) {
        this.mid = mid;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.mobileNumber = mobileNumber;
        this.emailId = emailId;
        this.shopImage = shopImage;
        this.city = city;
        this.area = area;
        this.providesDelivery = providesDelivery;
        this.distance = distance;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getProvidesDelivery() {
        return providesDelivery;
    }

    public void setProvidesDelivery(String providesDelivery) {
        this.providesDelivery = providesDelivery;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
