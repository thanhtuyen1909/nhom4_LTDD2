package vn.edu.tdc.zuke_customer.data_models;

import java.util.Date;

public class Rating {
    // Khai báo biến:
    private String key;
    private String comment;
    private String productID;
    private int rating;
    private String orderID;
    private String created_at;

    // Get - set:
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    // Contructors
    public Rating() {
    }

    public Rating(String comment, String productID, int rating, String orderID, String created_at) {
        this.comment = comment;
        this.productID = productID;
        this.rating = rating;
        this.orderID = orderID;
        this.created_at = created_at;
    }

    public Rating(String comment, int rating, String created_at) {
        this.comment = comment;
        this.rating = rating;
        this.created_at = created_at;
    }
}
