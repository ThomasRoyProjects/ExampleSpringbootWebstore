package com.store.webstore.exception;

public class ProductNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
