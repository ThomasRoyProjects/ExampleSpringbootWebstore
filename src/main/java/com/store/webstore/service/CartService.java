package com.store.webstore.service;

import com.store.webstore.model.CartItem;
import com.store.webstore.model.Product;
import com.store.webstore.repository.ProductRepository;
import com.store.webstore.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class CartService {
    private final ProductRepository productRepository;
    private final Map<Long, CartItem> cartItems = new LinkedHashMap<>();

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addProductToCart(Long productId, int quantity) {
        validateQuantity(quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        int currentQuantity = cartItems.containsKey(productId) ? cartItems.get(productId).getQuantity() : 0;
        int requestedQuantity = currentQuantity + quantity;
        if (requestedQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        cartItems.put(productId, new CartItem(
                product.getProductId(),
                product.getName(),
                money(product.getPrice()),
                requestedQuantity,
                product.getImageUrl()
        ));
    }

    public void addProductToCart(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item is required");
        }
        addProductToCart(item.getProductId(), item.getQuantity());
    }

    public List<CartItem> getCartItems() {
        List<CartItem> items = new ArrayList<>();
        for (CartItem item : cartItems.values()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            items.add(new CartItem(
                    product.getProductId(),
                    product.getName(),
                    money(product.getPrice()),
                    item.getQuantity(),
                    product.getImageUrl()
            ));
        }
        return List.copyOf(items);
    }

    public BigDecimal getTotalPrice() {
        return getCartItems().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void removeProductFromCart(Long productId) {
        cartItems.remove(productId);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
