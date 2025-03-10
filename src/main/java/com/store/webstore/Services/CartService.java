package com.store.webstore.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.webstore.Models.CartItem;
import com.store.webstore.Models.Product;
import com.store.webstore.Repositories.ProductRepository;
import com.store.webstore.configAndMisc.ProductNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    private List<CartItem> cartItems = new ArrayList<>();


    public void addProductToCart(CartItem item) {
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(item.getProductId())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                return;
            }
        }

        CartItem newItem = new CartItem(
                product.getProductId(),
                product.getName(),
                product.getPrice().doubleValue(),
                item.getQuantity(),
                product.getImageUrl()
        );
        cartItems.add(newItem);
    }


    public List<CartItem> getCartItems() {
        return cartItems;
    }


    public double getTotalPrice() {
        return cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }


    public void removeProductFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProductId().equals(productId));
    }

    public void clearCart() {
        cartItems.clear();
    }
}
