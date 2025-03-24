package com.store.webstore.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.store.webstore.Services.CartService;

@Controller 
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private CartService cartService;

    @PostMapping("/checkout")
    public ResponseEntity<String> processOrder() {
        cartService.processOrder(); 
        return ResponseEntity.ok("Order processed successfully");  
    }

    @GetMapping("/thank-you")
    public String showThankYouPage(
        @RequestParam(name = "orderNumber", required = false) String orderNumber,
        @RequestParam(name = "continent", required = false) String continent,
        @RequestParam(name = "taxRate", required = false) Double taxRate, 
        @RequestParam(name = "shippingCost", required = false) Double shippingCost,
        @RequestParam(name = "taxAmount", required = false) Double taxAmount,
        @RequestParam(name = "finalPrice", required = false) Double finalPrice,
        Model model
    ) {
        model.addAttribute("orderNumber", orderNumber);
        model.addAttribute("continent", continent);
        model.addAttribute("taxRate", taxRate != null ? taxRate : 0.0);  
        model.addAttribute("shippingCost", shippingCost != null ? shippingCost : 0.0);
        model.addAttribute("taxAmount", taxAmount != null ? taxAmount : 0.0);
        model.addAttribute("finalPrice", finalPrice != null ? finalPrice : 0.0);

        return "thank-you";
    }
}

