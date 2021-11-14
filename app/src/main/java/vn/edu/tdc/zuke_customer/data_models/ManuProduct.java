package vn.edu.tdc.zuke_customer.data_models;

import java.util.ArrayList;

public class ManuProduct {
    private ArrayList<Product> listProduct;
    private String nameManu;
    private boolean isExpandable;

    public ArrayList<Product> getListProduct() {
        return listProduct;
    }

    public void setListProduct(ArrayList<Product> listProduct) {
        this.listProduct = listProduct;
    }

    public String getNameManu() {
        return nameManu;
    }

    public void setNameManu(String nameManu) {
        this.nameManu = nameManu;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public ManuProduct(ArrayList<Product> listProduct, String nameManu) {
        this.listProduct = listProduct;
        this.nameManu = nameManu;
        this.isExpandable = false;
    }

    public ManuProduct() {
    }
}
