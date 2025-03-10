package com.store.webstore.configAndMisc;

public class ProductNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	private Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
