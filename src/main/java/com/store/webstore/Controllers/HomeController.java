package com.store.webstore.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.store.webstore.Models.Product;
import com.store.webstore.Repositories.*;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/")
    public String handleRootUrl(Model model) {
        // fetch products from repo for authenticated and unauthenticated users
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);

        //auth check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "home"; // homepage:authenticated user view
        }
        return "main"; // homepage:unauthenticated user view
    }


    @GetMapping("/home")
    public String showProductsPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login-page";
    }
    
    @GetMapping("/main") 
    public String showProductsPageMain(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "products";
    }
    @GetMapping("/admin-login")
    public String showAdminLoginPage() {
        return "admin-login"; 
    }
    @GetMapping("add-products")
    public String addProducts() {
    	return "add-products";
    }

}
