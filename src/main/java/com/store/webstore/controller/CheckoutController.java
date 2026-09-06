package com.store.webstore.controller;

import com.store.webstore.dto.CheckoutRequest;
import com.store.webstore.model.Order;
import com.store.webstore.service.CartService;
import com.store.webstore.service.CheckoutService;
import com.store.webstore.service.OrderService;
import com.store.webstore.exception.ProductNotFoundException;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, CheckoutService checkoutService, OrderService orderService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @GetMapping
    public String checkoutPage(Model model) {
        return populateCheckoutModel(model, "Europe");
    }

    @GetMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCheckoutInfo(@RequestParam("continent") String continent) {
        return checkoutResponse(continent);
    }

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processCheckout(
            @Valid @RequestBody CheckoutRequest request,
            BindingResult bindingResult,
            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", bindingResult.getFieldError().getDefaultMessage()));
        }
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }
        Order order = orderService.confirmOrder(request.getContinent(), authentication.getName());
        return ResponseEntity.ok(Map.of("orderNumber", order.getOrderNumber(), "receiptUrl", "/orders/" + order.getOrderNumber()));
    }

    @ExceptionHandler({IllegalArgumentException.class, ProductNotFoundException.class})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> badRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    private String populateCheckoutModel(Model model, String continent) {
        CheckoutService.CheckoutTotals totals = checkoutService.calculateTotals(cartService.getTotalPrice(), continent);
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", totals.subtotal());
        model.addAttribute("taxRate", totals.taxRate());
        model.addAttribute("taxAmount", totals.taxAmount());
        model.addAttribute("shippingCost", totals.shippingCost());
        model.addAttribute("finalPrice", totals.finalPrice());
        model.addAttribute("continent", continent);
        return "checkout";
    }

    private Map<String, Object> checkoutResponse(String continent) {
        CheckoutService.CheckoutTotals totals = checkoutService.calculateTotals(cartService.getTotalPrice(), continent);
        return Map.of(
                "continent", totals.continent(),
                "totalPrice", totals.subtotal(),
                "taxRate", totals.taxRate(),
                "shippingCost", totals.shippingCost(),
                "taxAmount", totals.taxAmount(),
                "finalPrice", totals.finalPrice()
        );
    }
}
