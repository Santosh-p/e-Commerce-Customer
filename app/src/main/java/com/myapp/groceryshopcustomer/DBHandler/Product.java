package com.myapp.groceryshopcustomer.DBHandler;

import java.util.List;

/**
 * Created by apple on 8/14/17.
 */

public class Product {
    int productId,position;
    String productName,productImage,productManufacturar,productCategory;
    List<ProductUnit> productUnitArr;

    public Product() {
    }

    public Product(int productId, String productName, String productImage, String productManufacturar, List<ProductUnit> productUnitArr) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productManufacturar = productManufacturar;
        this.productUnitArr = productUnitArr;
    }

    public Product(int productId, String productName, String productImage, String productManufacturar, String productCategory, List<ProductUnit> productUnitArr) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productManufacturar = productManufacturar;
        this.productCategory = productCategory;
        this.productUnitArr = productUnitArr;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductManufacturar() {
        return productManufacturar;
    }

    public void setProductManufacturar(String productManufacturar) {
        this.productManufacturar = productManufacturar;
    }

    public List<ProductUnit> getProductUnitArr() {
        return productUnitArr;
    }

    public void setProductUnitArr(List<ProductUnit> productUnitArr) {
        this.productUnitArr = productUnitArr;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
