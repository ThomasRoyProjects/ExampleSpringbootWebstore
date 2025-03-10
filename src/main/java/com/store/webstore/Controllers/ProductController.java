package com.store.webstore.Controllers;

import com.store.webstore.Models.CartItem;
import com.store.webstore.Models.Product;
import com.store.webstore.Repositories.ProductRepository;
import com.store.webstore.Services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin-products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product"; 
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/admin-products";
    }

    @GetMapping
    public String showAllProducts(Model model) {
        Iterable<Product> products = productRepository.findAll();
        
        List<CartItem> cartItems = cartService.getCartItems();
        double totalPrice = cartService.getTotalPrice();

        model.addAttribute("products", products);
        model.addAttribute("cartItems", cartItems != null ? cartItems : new ArrayList<>());
        model.addAttribute("totalPrice", totalPrice);

        return "admin-products"; 
    }

    @GetMapping("/{id}")
    public String showProductDetails(@PathVariable("id") Long productId, Model model) {
        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()) {
            model.addAttribute("error", "Product not found");
            return "error-page";
        }

        model.addAttribute("product", productOptional.get());

        List<CartItem> cartItems = cartService.getCartItems();
        double totalPrice = cartService.getTotalPrice();

        model.addAttribute("cartItems", cartItems != null ? cartItems : new ArrayList<>());
        model.addAttribute("totalPrice", totalPrice);

        return "product-details";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long productId) {
        productRepository.deleteById(productId);
        return "redirect:/admin-products"; 
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long productId, Model model) {
        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()) {
            return "redirect:/admin-products";
        }

        model.addAttribute("product", productOptional.get());
        return "edit-product"; 
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long productId, @ModelAttribute Product product) {
        Optional<Product> existingProductOptional = productRepository.findById(productId);

        if (existingProductOptional.isEmpty()) {
            return "redirect:/admin-products";
        }

        Product existingProduct = existingProductOptional.get();
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity()); 

        productRepository.save(existingProduct);
        return "redirect:/admin-products";
    }


    @GetMapping("/../add-products")
    public String legacyAddProductsRedirect() {
        return "redirect:/admin/products/add";
    }

    @PostMapping("/../add-products")
    public String legacyAddProductRedirect(@RequestParam String name,
                                           @RequestParam String description,
                                           @RequestParam BigDecimal price,
                                           @RequestParam String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageUrl(imageUrl);

        productRepository.save(product);

        return "redirect:/admin/products/add";
    }
}
