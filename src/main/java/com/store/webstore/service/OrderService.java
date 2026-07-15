package com.store.webstore.service;

import com.store.webstore.model.CartItem;
import com.store.webstore.model.Order;
import com.store.webstore.model.OrderLine;
import com.store.webstore.model.Product;
import com.store.webstore.repository.OrderRepository;
import com.store.webstore.repository.ProductRepository;
import com.store.webstore.exception.ProductNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final EntityManager entityManager;

    public OrderService(CartService cartService, CheckoutService checkoutService, ProductRepository productRepository, OrderRepository orderRepository, EntityManager entityManager) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Order confirmOrder(String continent) {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (CartItem cartItem : cartItems) {
            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            Product product = entityManager.find(Product.class, cartItem.getProductId(), LockModeType.PESSIMISTIC_WRITE);
            if (product == null) {
                throw new ProductNotFoundException(cartItem.getProductId());
            }
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock");
            }

            BigDecimal unitPrice = money(product.getPrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal).setScale(2, RoundingMode.HALF_UP);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderLine line = new OrderLine();
            line.setProductId(product.getProductId());
            line.setProductName(product.getName());
            line.setUnitPrice(unitPrice);
            line.setQuantity(cartItem.getQuantity());
            line.setLineTotal(lineTotal);
            line.setImageUrl(product.getImageUrl());
            order.addItem(line);
        }

        CheckoutService.CheckoutTotals totals = checkoutService.calculateTotals(subtotal, continent);
        order.setOrderNumber(nextOrderNumber());
        order.setStatus("CONFIRMED");
        order.setContinent(totals.continent());
        order.setSubtotal(totals.subtotal());
        order.setTaxRate(totals.taxRate());
        order.setShippingCost(totals.shippingCost());
        order.setTaxAmount(totals.taxAmount());
        order.setFinalPrice(totals.finalPrice());

        Order saved = orderRepository.save(order);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cartService.clearCart();
            }
        });
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getReceipt(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    private String nextOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + Long.toUnsignedString(RANDOM.nextLong(), 36).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
