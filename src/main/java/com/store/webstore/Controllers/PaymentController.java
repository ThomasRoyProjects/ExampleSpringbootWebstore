package com.store.webstore.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {
@GetMapping("/payment")
public String paymentPage(
        @RequestParam String orderNumber,
        @RequestParam String continent,
        @RequestParam double taxRate,
        @RequestParam double shippingCost,
        @RequestParam double taxAmount,
        @RequestParam double finalPrice,
        Model model) {

    model.addAttribute("orderNumber", orderNumber);
    model.addAttribute("continent", continent);
    model.addAttribute("taxRate", taxRate);
    model.addAttribute("shippingCost", shippingCost);
    model.addAttribute("taxAmount", taxAmount);
    model.addAttribute("finalPrice", finalPrice);

    return "payment";
}}
