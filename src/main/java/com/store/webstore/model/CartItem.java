package com.store.webstore.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CartItem {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;

    public CartItem() {
    }

    public CartItem(Long productId, String productName, BigDecimal price, int quantity, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.price = scaleMoney(price);
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = scaleMoney(price);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getLineTotal() {
        return scaleMoney(price.multiply(BigDecimal.valueOf(quantity)));
    }

    private static BigDecimal scaleMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
