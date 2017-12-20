package com.myapp.groceryshopcustomer.DBHandler;

/**
 * Created by apple on 8/14/17.
 */

public class ProductUnit {
    String cart_availability;
    int cart_qty;
    String unit;
    Double price;
    Double mrp;
    Double save_rs;
    int quantity;

    public ProductUnit() {
    }

    public ProductUnit(String cart_availability, int cart_qty, String unit, Double price, Double mrp, Double save_rs, int quantity) {
        this.cart_availability = cart_availability;
        this.cart_qty = cart_qty;
        this.unit = unit;
        this.price = price;
        this.mrp = mrp;
        this.save_rs = save_rs;
        this.quantity = quantity;
    }

    public String getCart_availability() {
        return cart_availability;
    }

    public void setCart_availability(String cart_availability) {
        this.cart_availability = cart_availability;
    }

    public int getCart_qty() {
        return cart_qty;
    }

    public void setCart_qty(int cart_qty) {
        this.cart_qty = cart_qty;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getMrp() {
        return mrp;
    }

    public void setMrp(Double mrp) {
        this.mrp = mrp;
    }

    public Double getSave_rs() {
        return save_rs;
    }

    public void setSave_rs(Double save_rs) {
        this.save_rs = save_rs;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
