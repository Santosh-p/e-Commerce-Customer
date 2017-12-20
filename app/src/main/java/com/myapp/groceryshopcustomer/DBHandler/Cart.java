package com.myapp.groceryshopcustomer.DBHandler;

/**
 * Created by apple on 8/29/17.
 */
public class Cart {
    int ProductId, Qty;
    String ProductName, ProductImage, Manufacturer, CategoryName, Unit,availablityStatus;
    Double Price,Mrp,ProductTotalPrice,comPrice,compProductTotalPrice;

    public Cart() {
    }

    public Cart(int productId, int qty, String productName, String productImage, String manufacturer, String categoryName, String unit) {
        ProductId = productId;
        Qty = qty;
        ProductName = productName;
        ProductImage = productImage;
        Manufacturer = manufacturer;
        CategoryName = categoryName;
        Unit = unit;
    }

    public Cart(int qty, String productName, String productImage, String manufacturer, String categoryName, String unit) {
        Qty = qty;
        ProductName = productName;
        ProductImage = productImage;
        Manufacturer = manufacturer;
        CategoryName = categoryName;
        Unit = unit;
    }

    public Cart(int productId, int qty, String productName, String productImage, String manufacturer, String categoryName, String unit, Double price, Double productTotalPrice) {
        ProductId = productId;
        Qty = qty;
        ProductName = productName;
        ProductImage = productImage;
        Manufacturer = manufacturer;
        CategoryName = categoryName;
        Unit = unit;
        Price = price;
        ProductTotalPrice = productTotalPrice;
    }

    public Double getProductTotalPrice() {
        return ProductTotalPrice;
    }

    public void setProductTotalPrice(Double productTotalPrice) {
        ProductTotalPrice = productTotalPrice;
    }

    public String getAvailablityStatus() {
        return availablityStatus;
    }

    public void setAvailablityStatus(String availablityStatus) {
        this.availablityStatus = availablityStatus;
    }

    public Double getComPrice() {
        return comPrice;
    }

    public void setComPrice(Double comPrice) {
        this.comPrice = comPrice;
    }

    public Double getCompProductTotalPrice() {
        return compProductTotalPrice;
    }

    public void setCompProductTotalPrice(Double compProductTotalPrice) {
        this.compProductTotalPrice = compProductTotalPrice;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public int getProductId() {
        return ProductId;
    }

    public void setProductId(int productId) {
        ProductId = productId;
    }

    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductImage() {
        return ProductImage;
    }

    public void setProductImage(String productImage) {
        ProductImage = productImage;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public Double getMrp() {
        return Mrp;
    }

    public void setMrp(Double mrp) {
        Mrp = mrp;
    }
}
