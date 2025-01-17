package com.myntrascraper.tshirtscraper;

public class Product {
    private String price;
    private String discount;
    private String link;

    public Product(String price, String discount, String link) {
        this.price = price;
        this.discount = discount;
        this.link = link;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscount() {
        return discount;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Price: " + price + ", Discount: " + discount + ", Link: " + link;
    }
}
