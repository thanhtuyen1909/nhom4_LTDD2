package vn.edu.tdc.zuke_customer.data_models;

public class Area {
    private String key;
    private String area;
    private int transport_fee;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getTransport_fee() {
        return transport_fee;
    }

    public void setTransport_fee(int transport_fee) {
        this.transport_fee = transport_fee;
    }

    public Area(String area, int transport_fee) {
        this.area = area;
        this.transport_fee = transport_fee;
    }

    public Area() {
    }
}
