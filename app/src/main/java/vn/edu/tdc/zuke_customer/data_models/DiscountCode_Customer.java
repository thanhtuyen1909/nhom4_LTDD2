package vn.edu.tdc.zuke_customer.data_models;

public class DiscountCode_Customer {
    private String key;
    private String code;
    private String customer_id;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public DiscountCode_Customer(String code, String customer_id) {
        this.code = code;
        this.customer_id = customer_id;
    }

    public DiscountCode_Customer() {
    }

    @Override
    public String toString() {
        return code;
    }
}
