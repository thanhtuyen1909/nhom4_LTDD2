package vn.edu.tdc.zuke_customer.data_models;

public class Manufactures {
    public String key;
    public String name;
    public String image;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Manufactures(String key, String name, String image) {
        this.key = key;
        this.name = name;
        this.image = image;
    }

    public Manufactures() {
    }

    @Override
    public String toString() {
        return name;
    }
}
