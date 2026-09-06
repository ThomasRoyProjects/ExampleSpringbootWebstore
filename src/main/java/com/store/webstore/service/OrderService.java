package com.store.webstore.service;

import com.store.webstore.model.CartItem;
import com.store.webstore.model.Order;
import com.store.webstore.model.OrderLine;
import com.store.webstore.model.Product;
import com.store.webstore.model.User;
import com.store.webstore.repository.OrderRepository;
import com.store.webstore.repository.ProductRepository;
import com.store.webstore.repository.UserRepository;
import com.store.webstore.exception.ProductNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile Consumer<Long> preLockHook;

    public static void setPreLockHook(Consumer<Long> hook) {
        preLockHook = hook;
    }

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public OrderService(CartService cartService,
                        CheckoutService checkoutService,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository,
                        EntityManager entityManager,
                        PlatformTransactionManager transactionManager) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Order confirmOrder(String continent, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalStateException("Authenticated customer is required for checkout");
        }
        return cartService.withLock(() -> transactionTemplate.execute(status -> {
            List<CartItem> cartItems = cartService.getCartItems();
            if (cartItems.isEmpty()) {
                throw new IllegalStateException("Cart is empty");
            }

            User customer = userRepository.findByEmail(User.normalizeEmail(userEmail))
                    .orElseThrow(() -> new IllegalStateException("Customer account not found: " + userEmail));

            List<CartItem> sortedItems = cartItems.stream()
                    .sorted(Comparator.comparing(CartItem::getProductId))
                    .toList();

            Map<Long, Product> lockedProducts = new LinkedHashMap<>();
            for (CartItem cartItem : sortedItems) {
                if (cartItem.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive");
                }
                Consumer<Long> hook = preLockHook;
                if (hook != null) {
                    hook.accept(cartItem.getProductId());
                }
                Product product = entityManager.find(Product.class, cartItem.getProductId());
                if (product == null) {
                    throw new ProductNotFoundException(cartItem.getProductId());
                }
                entityManager.refresh(product, LockModeType.PESSIMISTIC_WRITE);
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new IllegalArgumentException("Requested quantity exceeds available stock");
                }
                lockedProducts.put(cartItem.getProductId(), product);
            }

            Order order = new Order();
            order.setUser(customer);
            BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            for (CartItem cartItem : cartItems) {
                Product product = lockedProducts.get(cartItem.getProductId());
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
        }));
    }

    @Transactional(readOnly = true)
    public Order getReceipt(String orderNumber, String userEmail, boolean isAdmin) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!isAdmin) {
            if (order.getUser() == null || userEmail == null || !order.getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
                throw new IllegalArgumentException("Order not found");
            }
        }
        return order;
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
