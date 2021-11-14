package vn.edu.tdc.zuke_customer.data_models;

public class Banner {
    // Khai báo biến:
    private String name;
    private String image;
    private String endDate;
    private String startDate;

    // Get - set:
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

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    // Hàm khởi tạo:
    public Banner() {
    }

    public Banner(String name, String image) {
        this.name = name;
        this.image = image;
    }
}
