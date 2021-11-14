package vn.edu.tdc.zuke_customer.data_models;

public class Favorite {
    private String productId, userId, key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Favorite() {
    }

    public Favorite(String productId, String userId, String key) {
        this.productId = productId;
        this.userId = userId;
        this.key = key;
    }
}
