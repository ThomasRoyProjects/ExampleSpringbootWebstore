package com.store.webstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import java.math.BigDecimal;
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
    @WithMockUser
    void checkoutUsesServerTotalsPersistsReceiptDecrementsStockOnceAndIsNotRepeatableWhenCartIsEmpty() throws Exception {
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
}
