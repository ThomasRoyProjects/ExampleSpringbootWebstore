package com.store.webstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.store.webstore.model.Order;
import com.store.webstore.model.Product;
import com.store.webstore.model.TaxRate;
import com.store.webstore.model.User;
import com.store.webstore.repository.OrderRepository;
import com.store.webstore.repository.ProductRepository;
import com.store.webstore.repository.TaxRateRepository;
import com.store.webstore.repository.UserRepository;
import com.store.webstore.service.OrderService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:webstore-contract-tests;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "webstore.seed.products=false",
        "webstore.seed.tax-rates=false"
})
class WebstoreContractIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TaxRateRepository taxRateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        taxRateRepository.deleteAll();
        userRepository.deleteAll();
        OrderService.setPreLockHook(null);
    }

    @Test
    void registrationIgnoresInjectedAccountFieldsAndRejectsDuplicateEmailCaseInsensitively() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("id", "999")
                        .param("email", " Contract-Customer@Example.COM ")
                        .param("password", "correct-horse-battery")
                        .param("role", User.ROLE_ADMIN)
                        .param("active", "false")
                        .param("isActive", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        User registered = userRepository.findByEmail("contract-customer@example.com").orElseThrow();
        assertThat(registered.getEmail()).isEqualTo("contract-customer@example.com");
        assertThat(registered.getId()).isNotEqualTo(999L);
        assertThat(registered.getRole()).isEqualTo(User.ROLE_CUSTOMER);
        assertThat(registered.isActive()).isTrue();
        assertThat(userRepository.count()).isEqualTo(1L);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "contract-customer@example.com")
                        .param("password", "another-correct-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Email already exists."));

        assertThat(userRepository.count()).isEqualTo(1L);
    }

    @Test
    void customerAuthenticationRequiresTheEmailParameter() throws Exception {
        User customer = new User();
        customer.setEmail("login-customer@example.com");
        customer.setPassword(passwordEncoder.encode("correct-horse-battery"));
        customer.setRole(User.ROLE_CUSTOMER);
        customer.setActive(true);
        userRepository.save(customer);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", " LOGIN-CUSTOMER@EXAMPLE.COM ")
                        .param("password", "correct-horse-battery"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(authenticated().withUsername("login-customer@example.com"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "login-customer@example.com")
                        .param("password", "correct-horse-battery"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser
    void independentHttpSessionCartsDoNotSeeOrClearEachOthersItems() throws Exception {
        Product lotion = saveProduct("Session Lotion", "19.99", 10);
        MockHttpSession firstSession = new MockHttpSession();
        MockHttpSession secondSession = new MockHttpSession();

        addToCart(firstSession, lotion.getProductId(), 2)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Session Lotion")))
                .andExpect(content().string(containsString("2")));

        mockMvc.perform(get("/cart/items").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your cart is empty")));

        addToCart(secondSession, lotion.getProductId(), 1)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Session Lotion")));

        mockMvc.perform(post("/cart/clear").session(firstSession).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your cart is empty")));

        mockMvc.perform(get("/cart/items").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Session Lotion")))
                .andExpect(content().string(containsString("19.99")));
    }

    @Test
    @WithMockUser(username = "checkout-customer@example.com")
    void checkoutUsesServerTotalsPersistsReceiptDecrementsStockOnceAndIsNotRepeatableWhenCartIsEmpty() throws Exception {
        User customer = saveUser("checkout-customer@example.com", User.ROLE_CUSTOMER);
        Product serum = saveProduct("Authoritative Serum", "20.00", 5);
        taxRateRepository.save(new TaxRate("Europe", 10.0, 15.00));
        MockHttpSession buyingSession = new MockHttpSession();
        MockHttpSession otherSession = new MockHttpSession();

        addToCart(buyingSession, serum.getProductId(), 2).andExpect(status().isOk());
        addToCart(otherSession, serum.getProductId(), 1).andExpect(status().isOk());

        MvcResult checkout = mockMvc.perform(post("/checkout/process")
                        .session(buyingSession)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"continent\":\"Europe\",\"subtotal\":\"0.01\",\"taxAmount\":\"0.00\",\"shippingCost\":\"0.00\",\"finalPrice\":\"0.01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").isString())
                .andExpect(jsonPath("$.receiptUrl").isString())
                .andReturn();

        String response = checkout.getResponse().getContentAsString();
        String orderNumber = response.replaceAll(".*\\\"orderNumber\\\":\\\"([^\\\"]+)\\\".*", "$1");
        Order receipt = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        assertThat(receipt.getUser()).isNotNull();
        assertThat(receipt.getUser().getEmail()).isEqualTo("checkout-customer@example.com");
        assertThat(receipt.getSubtotal()).isEqualByComparingTo("40.00");
        assertThat(receipt.getTaxRate()).isEqualByComparingTo("10.00");
        assertThat(receipt.getTaxAmount()).isEqualByComparingTo("4.00");
        assertThat(receipt.getShippingCost()).isEqualByComparingTo("15.00");
        assertThat(receipt.getFinalPrice()).isEqualByComparingTo("59.00");
        assertThat(receipt.getItems()).hasSize(1);
        assertThat(receipt.getItems().get(0).getProductName()).isEqualTo("Authoritative Serum");
        assertThat(receipt.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(receipt.getItems().get(0).getUnitPrice()).isEqualByComparingTo("20.00");
        assertThat(receipt.getItems().get(0).getLineTotal()).isEqualByComparingTo("40.00");

        mockMvc.perform(get("/orders/{orderNumber}", orderNumber).session(buyingSession))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(view().name("thank-you"));

        Product afterFirstCheckout = productRepository.findById(serum.getProductId()).orElseThrow();
        assertThat(afterFirstCheckout.getStockQuantity()).isEqualTo(3);
        assertThat(orderRepository.count()).isEqualTo(1L);

        mockMvc.perform(get("/cart/items").session(buyingSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your cart is empty")));
        mockMvc.perform(get("/cart/items").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Authoritative Serum")));

        mockMvc.perform(post("/checkout/process")
                        .session(buyingSession)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"continent\":\"Europe\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cart is empty"));

        Product afterRepeatedCheckout = productRepository.findById(serum.getProductId()).orElseThrow();
        assertThat(afterRepeatedCheckout.getStockQuantity()).isEqualTo(3);
        assertThat(orderRepository.count()).isEqualTo(1L);
    }

    private Product saveProduct(String name, String price, int stockQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name + " description");
        product.setPrice(new BigDecimal(price));
        product.setImageUrl("/images/test.png");
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }

    private org.springframework.test.web.servlet.ResultActions addToCart(MockHttpSession session, Long productId, int quantity) throws Exception {
        return mockMvc.perform(post("/cart/add")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("{\"productId\":" + productId + ",\"quantity\":" + quantity + "}"));
    }

    @Test
    void crossUserReceiptDenialAndOwnerSuccess() throws Exception {
        saveUser("owner@example.com", User.ROLE_CUSTOMER);
        saveUser("attacker@example.com", User.ROLE_CUSTOMER);
        saveUser("admin@example.com", User.ROLE_ADMIN);

        Product product = saveProduct("Receipt Cream", "25.00", 10);
        taxRateRepository.save(new TaxRate("Europe", 10.0, 5.00));

        MockHttpSession ownerSession = new MockHttpSession();
        mockMvc.perform(post("/cart/add")
                        .session(ownerSession)
                        .with(csrf())
                        .with(user("owner@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + product.getProductId() + ",\"quantity\":1}"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/checkout/process")
                        .session(ownerSession)
                        .with(csrf())
                        .with(user("owner@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"continent\":\"Europe\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String orderNumber = result.getResponse().getContentAsString().replaceAll(".*\\\"orderNumber\\\":\\\"([^\\\"]+)\\\".*", "$1");

        // Owner can access receipt
        mockMvc.perform(get("/orders/{orderNumber}", orderNumber)
                        .with(user("owner@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(view().name("thank-you"));

        // Non-owner receives non-leaking 404
        mockMvc.perform(get("/orders/{orderNumber}", orderNumber)
                        .with(user("attacker@example.com").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "Order not found"))
                .andExpect(view().name("thank-you"));

        // Legacy unowned order (user_id is null)
        Order legacyOrder = new Order();
        legacyOrder.setOrderNumber("ORD-LEGACY-UNOWNED");
        legacyOrder.setStatus("CONFIRMED");
        legacyOrder.setContinent("Europe");
        legacyOrder.setTaxRate(new BigDecimal("10.00"));
        legacyOrder.setSubtotal(new BigDecimal("25.00"));
        legacyOrder.setShippingCost(new BigDecimal("5.00"));
        legacyOrder.setTaxAmount(new BigDecimal("2.50"));
        legacyOrder.setFinalPrice(new BigDecimal("32.50"));
        orderRepository.save(legacyOrder);

        // Ordinary customer accessing unowned order receives 404
        mockMvc.perform(get("/orders/{orderNumber}", "ORD-LEGACY-UNOWNED")
                        .with(user("owner@example.com").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "Order not found"))
                .andExpect(view().name("thank-you"));

        // Administrator can inspect both customer order and legacy unowned order
        mockMvc.perform(get("/orders/{orderNumber}", orderNumber)
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(view().name("thank-you"));

        mockMvc.perform(get("/orders/{orderNumber}", "ORD-LEGACY-UNOWNED")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(view().name("thank-you"));
    }

    @Test
    void twoConcurrentSameSessionCheckoutRequestsSerializeAndOnlyOneSucceeds() throws Exception {
        saveUser("concurrent-customer@example.com", User.ROLE_CUSTOMER);
        Product product = saveProduct("Concurrent Cream", "30.00", 10);
        taxRateRepository.save(new TaxRate("Europe", 10.0, 5.00));

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/cart/add")
                        .session(session)
                        .with(csrf())
                        .with(user("concurrent-customer@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + product.getProductId() + ",\"quantity\":2}"))
                .andExpect(status().isOk());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);

        Callable<MvcResult> checkoutTask = () -> {
            startGate.await();
            return mockMvc.perform(post("/checkout/process")
                            .session(session)
                            .with(csrf())
                            .with(user("concurrent-customer@example.com").roles("CUSTOMER"))
                            .contentType("application/json")
                            .content("{\"continent\":\"Europe\"}"))
                    .andReturn();
        };

        Future<MvcResult> future1 = executor.submit(checkoutTask);
        Future<MvcResult> future2 = executor.submit(checkoutTask);

        startGate.countDown();

        MvcResult result1 = future1.get(10, TimeUnit.SECONDS);
        MvcResult result2 = future2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        int status1 = result1.getResponse().getStatus();
        int status2 = result2.getResponse().getStatus();

        List<Integer> statuses = List.of(status1, status2);
        assertThat(statuses).containsExactlyInAnyOrder(200, 409);

        // Stock must be decremented only once (from 10 down to 8)
        Product afterCheckout = productRepository.findById(product.getProductId()).orElseThrow();
        assertThat(afterCheckout.getStockQuantity()).isEqualTo(8);

        // Exactly one order created in repository
        assertThat(orderRepository.count()).isEqualTo(1L);

        // Cart in session must be empty
        mockMvc.perform(get("/cart/items")
                        .session(session)
                        .with(user("concurrent-customer@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your cart is empty")));
    }

    @Test
    void reversedMultiProductCheckoutsAcquireRowLocksDeterministicallyWithoutDeadlock() throws Exception {
        saveUser("buyer1@example.com", User.ROLE_CUSTOMER);
        saveUser("buyer2@example.com", User.ROLE_CUSTOMER);

        Product prodA = saveProduct("Product A", "10.00", 20);
        Product prodB = saveProduct("Product B", "15.00", 20);
        taxRateRepository.save(new TaxRate("Europe", 10.0, 5.00));

        assertThat(prodA.getProductId()).isLessThan(prodB.getProductId());

        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();

        // Session 1 adds A then B
        mockMvc.perform(post("/cart/add")
                        .session(session1)
                        .with(csrf())
                        .with(user("buyer1@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + prodA.getProductId() + ",\"quantity\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/cart/add")
                        .session(session1)
                        .with(csrf())
                        .with(user("buyer1@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + prodB.getProductId() + ",\"quantity\":3}"))
                .andExpect(status().isOk());

        // Session 2 adds B then A (reversed cart order)
        mockMvc.perform(post("/cart/add")
                        .session(session2)
                        .with(csrf())
                        .with(user("buyer2@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + prodB.getProductId() + ",\"quantity\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/cart/add")
                        .session(session2)
                        .with(csrf())
                        .with(user("buyer2@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + prodA.getProductId() + ",\"quantity\":4}"))
                .andExpect(status().isOk());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);

        Callable<MvcResult> checkoutTask1 = () -> {
            startGate.await();
            return mockMvc.perform(post("/checkout/process")
                            .session(session1)
                            .with(csrf())
                            .with(user("buyer1@example.com").roles("CUSTOMER"))
                            .contentType("application/json")
                            .content("{\"continent\":\"Europe\"}"))
                    .andReturn();
        };

        Callable<MvcResult> checkoutTask2 = () -> {
            startGate.await();
            return mockMvc.perform(post("/checkout/process")
                            .session(session2)
                            .with(csrf())
                            .with(user("buyer2@example.com").roles("CUSTOMER"))
                            .contentType("application/json")
                            .content("{\"continent\":\"Europe\"}"))
                    .andReturn();
        };

        Future<MvcResult> future1 = executor.submit(checkoutTask1);
        Future<MvcResult> future2 = executor.submit(checkoutTask2);

        startGate.countDown();

        MvcResult res1 = future1.get(10, TimeUnit.SECONDS);
        MvcResult res2 = future2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(res1.getResponse().getStatus()).isEqualTo(200);
        assertThat(res2.getResponse().getStatus()).isEqualTo(200);

        // Total A bought = 2 + 4 = 6, remaining = 20 - 6 = 14
        Product updatedA = productRepository.findById(prodA.getProductId()).orElseThrow();
        assertThat(updatedA.getStockQuantity()).isEqualTo(14);

        // Total B bought = 3 + 1 = 4, remaining = 20 - 4 = 16
        Product updatedB = productRepository.findById(prodB.getProductId()).orElseThrow();
        assertThat(updatedB.getStockQuantity()).isEqualTo(16);

        assertThat(orderRepository.count()).isEqualTo(2L);
    }

    @Test
    void failedCheckoutPreservesCartAndRollsBackState() throws Exception {
        saveUser("preserved-cart@example.com", User.ROLE_CUSTOMER);
        Product scarce = saveProduct("Scarce Lotion", "50.00", 2);
        taxRateRepository.save(new TaxRate("Europe", 10.0, 5.00));

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/cart/add")
                        .session(session)
                        .with(csrf())
                        .with(user("preserved-cart@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + scarce.getProductId() + ",\"quantity\":2}"))
                .andExpect(status().isOk());

        // Deplete stock to 1 so the checkout of 2 will fail
        scarce.setStockQuantity(1);
        productRepository.save(scarce);

        mockMvc.perform(post("/checkout/process")
                        .session(session)
                        .with(csrf())
                        .with(user("preserved-cart@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"continent\":\"Europe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Requested quantity exceeds available stock"));

        // Verify cart is NOT cleared and still contains the item
        mockMvc.perform(get("/cart/items")
                        .session(session)
                        .with(user("preserved-cart@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scarce Lotion")))
                .andExpect(content().string(containsString("2")));

        // Stock remains untouched
        Product unchanged = productRepository.findById(scarce.getProductId()).orElseThrow();
        assertThat(unchanged.getStockQuantity()).isEqualTo(1);

        // No order was created
        assertThat(orderRepository.count()).isEqualTo(0L);
    }

    @Test
    void twoDifferentSessionsCompeteForLastStockUnitWithDeterministicPreLockRead() throws Exception {
        saveUser("contended-winner@example.com", User.ROLE_CUSTOMER);
        saveUser("contended-loser@example.com", User.ROLE_CUSTOMER);

        Product product = saveProduct("Contended Cream", "25.00", 1);
        taxRateRepository.save(new TaxRate("Europe", 10.0, 5.00));

        MockHttpSession winnerSession = new MockHttpSession();
        MockHttpSession loserSession = new MockHttpSession();

        mockMvc.perform(post("/cart/add")
                        .session(winnerSession)
                        .with(csrf())
                        .with(user("contended-winner@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + product.getProductId() + ",\"quantity\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/cart/add")
                        .session(loserSession)
                        .with(csrf())
                        .with(user("contended-loser@example.com").roles("CUSTOMER"))
                        .contentType("application/json")
                        .content("{\"productId\":" + product.getProductId() + ",\"quantity\":1}"))
                .andExpect(status().isOk());

        CountDownLatch loserPreloadedLatch = new CountDownLatch(1);
        CountDownLatch winnerCommittedLatch = new CountDownLatch(1);
        AtomicBoolean hookTriggered = new AtomicBoolean(false);

        OrderService.setPreLockHook(productId -> {
            if (hookTriggered.compareAndSet(false, true)) {
                loserPreloadedLatch.countDown();
                try {
                    winnerCommittedLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // Loser enters checkout first, preloads product into persistence context, and waits at pre-lock barrier
            Future<MvcResult> loserFuture = executor.submit(() ->
                    mockMvc.perform(post("/checkout/process")
                                    .session(loserSession)
                                    .with(csrf())
                                    .with(user("contended-loser@example.com").roles("CUSTOMER"))
                                    .contentType("application/json")
                                    .content("{\"continent\":\"Europe\"}"))
                            .andReturn()
            );

            // Ensure loser has preloaded product and is waiting before lock acquisition
            assertThat(loserPreloadedLatch.await(10, TimeUnit.SECONDS)).isTrue();

            // Winner checks out completely, acquiring lock and decrementing stock from 1 to 0
            mockMvc.perform(post("/checkout/process")
                            .session(winnerSession)
                            .with(csrf())
                            .with(user("contended-winner@example.com").roles("CUSTOMER"))
                            .contentType("application/json")
                            .content("{\"continent\":\"Europe\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderNumber").isString());

            // Release loser; post-refresh under lock re-reads database row, sees stock=0, and rejects
            winnerCommittedLatch.countDown();

            MvcResult loserResult = loserFuture.get(10, TimeUnit.SECONDS);
            assertThat(loserResult.getResponse().getStatus()).isEqualTo(400);
            assertThat(loserResult.getResponse().getContentAsString()).contains("Requested quantity exceeds available stock");

            // Final stock in database must be exactly 0 (not decremented twice or negative)
            Product finalProduct = productRepository.findById(product.getProductId()).orElseThrow();
            assertThat(finalProduct.getStockQuantity()).isEqualTo(0);

            // Only winner's order exists in repository
            assertThat(orderRepository.count()).isEqualTo(1L);

            // Winner's cart cleared after successful commit
            mockMvc.perform(get("/cart/items")
                            .session(winnerSession)
                            .with(user("contended-winner@example.com").roles("CUSTOMER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Your cart is empty")));

            // Loser's cart preserved after rollback
            mockMvc.perform(get("/cart/items")
                            .session(loserSession)
                            .with(user("contended-loser@example.com").roles("CUSTOMER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Contended Cream")))
                    .andExpect(content().string(containsString("1")));
        } finally {
            OrderService.setPreLockHook(null);
            executor.shutdown();
        }
    }

    private User saveUser(String email, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("correct-horse-battery"));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }
}
