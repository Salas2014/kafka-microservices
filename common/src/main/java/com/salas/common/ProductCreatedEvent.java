package com.salas.common;

public class ProductCreatedEvent {
    private String productid;
    private String title;
    private String price;
    private String quantity;

    public ProductCreatedEvent() {
    }

    public ProductCreatedEvent(String productid, String title, String price, String quantity) {
        this.productid = productid;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
