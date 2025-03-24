package com.store.webstore.Controllers;

import com.store.webstore.Models.CartItem;
import com.store.webstore.Models.CheckoutRequest;
import com.store.webstore.Models.Order;
import com.store.webstore.Repositories.OrderRepository;
import com.store.webstore.Services.CartService;
import com.store.webstore.Services.CheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final OrderRepository orderRepository;

    public CheckoutController(CartService cartService, CheckoutService checkoutService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String checkoutPage(Model model) {
        String continent = "Europe"; // Default continent
        return populateCheckoutModel(model, continent);
    }

    @GetMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCheckoutInfo(@RequestParam("continent") String continent) {
        return calculateCheckoutInfo(continent);
    }

@PostMapping("/process")
@ResponseBody
public ResponseEntity<Map<String, Object>> processCheckout(@RequestBody CheckoutRequest request) {
    try {
        System.out.println("Received request: " + request);

        if (request.getContinent() == null || request.getContinent().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Continent is required"));
        }

        Map<String, Object> checkoutData = calculateCheckoutInfo(request.getContinent());

        String orderNumber = "ORD" + UUID.randomUUID().toString().substring(0, 8);

        Order order = new Order(orderNumber, request.getContinent(), 
                                request.getTaxRate(), request.getShippingCost(), 
                                request.getTaxAmount(), request.getFinalPrice(), 
                                request.getCartItems());

        orderRepository.save(order);

        checkoutData.put("orderNumber", orderNumber);

        return ResponseEntity.ok(checkoutData);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal Server Error"));
    }
}




    @GetMapping("/payment")
    public String paymentPage(@RequestParam("orderNumber") String orderNumber, Model model) {
        model.addAttribute("orderNumber", orderNumber);
        return "payment"; // Make sure "payment.html" exists in `templates`
    }

    @PostMapping("/payment")
    public String paymentPage(@RequestBody Map<String, Object> params, Model model) {
        String orderNumber = (String) params.get("orderNumber");
        String continent = (String) params.get("continent");

        // Safe number conversion
        double taxRate = params.get("taxRate") instanceof Number ? ((Number) params.get("taxRate")).doubleValue() : 0.0;
        double shippingCost = params.get("shippingCost") instanceof Number ? ((Number) params.get("shippingCost")).doubleValue() : 0.0;
        double taxAmount = params.get("taxAmount") instanceof Number ? ((Number) params.get("taxAmount")).doubleValue() : 0.0;
        double finalPrice = params.get("finalPrice") instanceof Number ? ((Number) params.get("finalPrice")).doubleValue() : 0.0;

        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("continent", continent);
        model.addAttribute("taxRate", taxRate);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("finalPrice", finalPrice);

        return "payment";
    }

    private String populateCheckoutModel(Model model, String continent) {
        Map<String, Object> checkoutData = calculateCheckoutInfo(continent);

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", checkoutData.get("totalPrice"));
        model.addAttribute("taxRate", checkoutData.get("taxRate"));
        model.addAttribute("taxAmount", checkoutData.get("taxAmount"));
        model.addAttribute("shippingCost", checkoutData.get("shippingCost"));
        model.addAttribute("finalPrice", checkoutData.get("finalPrice"));
        model.addAttribute("continent", continent);

        return "checkout";
    }

    private Map<String, Object> calculateCheckoutInfo(String continent) {
        double taxRate = checkoutService.getTaxRate(continent);
        double shippingCost = checkoutService.getShippingCost(continent);
        double totalPrice = cartService.getTotalPrice();
        double taxAmount = totalPrice * (taxRate / 100);
        double finalPrice = totalPrice + taxAmount + shippingCost;

        Map<String, Object> response = new HashMap<>();
        response.put("taxRate", taxRate);
        response.put("shippingCost", shippingCost);
        response.put("taxAmount", taxAmount);
        response.put("finalPrice", finalPrice);
        response.put("totalPrice", totalPrice);

        return response;
    }
}
