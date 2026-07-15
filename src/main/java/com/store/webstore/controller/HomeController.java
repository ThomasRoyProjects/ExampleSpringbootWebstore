package com.store.webstore.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.store.webstore.service.CartService;
import com.store.webstore.service.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CartService cartService;

    public HomeController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String handleRootUrl(Model model) {
        model.addAttribute("products", productService.findAll());
        return "main";
    }

    @GetMapping("/products")
    public String showProductsPage(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("cartItems", cartService.getCartItems() != null ? cartService.getCartItems() : new ArrayList<>());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "products";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login-page";
    }

    @GetMapping("/admin/login")
    public String showAdminLoginPage() {
        return "admin-login";
    }
}
