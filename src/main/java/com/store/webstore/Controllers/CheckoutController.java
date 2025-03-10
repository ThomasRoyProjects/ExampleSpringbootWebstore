package com.store.webstore.Controllers;

import com.store.webstore.Services.CheckoutService;
import com.store.webstore.Models.CartItem;
import com.store.webstore.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CheckoutService checkoutService;

    @GetMapping
    public String checkoutPage(Model model) {
        // default continent (can be hardcoded or selected by user)
        String continent = "Europe"; 
        double taxRate = checkoutService.getTaxRate(continent);
        double shippingCost = checkoutService.getShippingCost(continent);

        // get cart items and total price
        List<CartItem> cartItems = cartService.getCartItems();
        double totalPrice = cartService.getTotalPrice();

        // calculate tax and final price
        double taxAmount = totalPrice * (taxRate / 100);
        double finalPrice = totalPrice + taxAmount + shippingCost;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("taxRate", taxRate);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("shippingCost", shippingCost);
        model.addAttribute("finalPrice", finalPrice);
        model.addAttribute("continent", continent);

        return "checkout"; // checkout view
    }

    @GetMapping("/update")
    @ResponseBody
    public Map<String, Object> getUpdatedCheckoutInfo(@RequestParam("continent") String continent) {
        double taxRate = checkoutService.getTaxRate(continent);
        double shippingCost = checkoutService.getShippingCost(continent);

        List<CartItem> cartItems = cartService.getCartItems();
        double totalPrice = cartService.getTotalPrice();

        double taxAmount = totalPrice * (taxRate / 100);
        double finalPrice = totalPrice + taxAmount + shippingCost;

        Map<String, Object> response = new HashMap<>();
        response.put("taxRate", taxRate);
        response.put("shippingCost", shippingCost);
        response.put("taxAmount", taxAmount);
        response.put("finalPrice", finalPrice);

        return response; // return JSON checkout info
    }
}
