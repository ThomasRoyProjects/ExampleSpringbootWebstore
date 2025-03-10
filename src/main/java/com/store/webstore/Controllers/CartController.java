package com.store.webstore.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.store.webstore.Models.CartItem;
import com.store.webstore.Services.CartService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @GetMapping
    public String viewCart(Model model) {
        List<CartItem> cartItems = cartService.getCartItems();
        double totalPrice = cartService.getTotalPrice();

        if (cartItems == null) {
            cartItems = new ArrayList<>(); 
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "cart"; 
    }

    @GetMapping("/items")
    public String getCartItems(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "cart :: cart-items"; 
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestBody CartItem item, @RequestHeader("X-CSRF-TOKEN") String csrfToken) {
        cartService.addProductToCart(item);
        logger.info("Added product ID: {} to cart", item.getProductId());

        return getCartItemsAsHtml();
    }

    @PostMapping("/remove/{productId}")
    @ResponseBody
    public String removeFromCart(@PathVariable("productId") Long productId, @RequestHeader("X-CSRF-TOKEN") String csrfToken) {
        cartService.removeProductFromCart(productId);
        logger.info("Removed product ID: {} from cart", productId);
        return getCartItemsAsHtml();
    }


    @PostMapping("/clear")
    @ResponseBody
    public String clearCart(@RequestHeader("X-CSRF-TOKEN") String csrfToken) {
        cartService.clearCart();
        logger.info("Cart cleared");

        List<CartItem> updatedCartItems = cartService.getCartItems();
        logger.info("Updated cart after clearing: {}", updatedCartItems.size());

        return getCartItemsAsHtml();
    }

    private String getCartItemsAsHtml() {
        List<CartItem> updatedCartItems = cartService.getCartItems();
        double totalPrice = cartService.getTotalPrice();  
        StringBuilder html = new StringBuilder();

        
        html.append("<table class='cart-items-table' style='width:80%; border-collapse: collapse;'>")
            .append("<thead>")
            .append("<tr>")
            .append("<th style='padding: 10px; background-color: #f2f2f2;'>Image</th>")
            .append("<th style='padding: 10px; background-color: #f2f2f2;'>Product</th>")
            .append("<th style='padding: 10px; background-color: #f2f2f2;'>Quantity</th>")
            .append("<th style='padding: 10px; background-color: #f2f2f2;'>Price</th>")
            .append("<th style='padding: 10px; background-color: #f2f2f2;'>Action</th>")
            .append("</tr>")
            .append("</thead>")
            .append("<tbody>");

        for (CartItem item : updatedCartItems) {
            html.append("<tr style='border-bottom: 1px solid #ddd;'>")  
                .append("<td style='padding: 10px;'><img src='").append(item.getImageUrl()).append("' alt='Product Image' width='50' style='display:block;'></td>")
                .append("<td style='padding: 10px; white-space: nowrap;'>").append(item.getProductName()).append("</td>")
                .append("<td style='padding: 10px; text-align: center; min-width: 50px;'>").append(item.getQuantity()).append("</td>")
                .append("<td style='padding: 10px; text-align: right; min-width: 80px;'>$").append(String.format("%.2f", item.getPrice())).append("</td>")
                .append("<td style='padding: 10px;'><button class='remove-item' data-id='").append(item.getProductId()).append("'>Remove</button></td>")
                .append("</tr>");
        }

        html.append("</tbody></table>");

        html.append("<div id='cartTotal' style='margin-top: 10px; font-weight: bold;'>Total: $")
            .append(String.format("%.2f", totalPrice)).append("</div>");

        return html.toString();
    }
    }
