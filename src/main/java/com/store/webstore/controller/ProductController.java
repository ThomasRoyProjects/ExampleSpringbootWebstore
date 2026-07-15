package com.store.webstore.controller;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

import com.store.webstore.model.Product;
import com.store.webstore.service.CartService;
import com.store.webstore.dto.ProductForm;
import com.store.webstore.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;

    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping
    public String showAllProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("cartItems", cartService.getCartItems() != null ? cartService.getCartItems() : new ArrayList<>());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        return "admin-products";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm());
        }
        return "add-products";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                             BindingResult bindingResult,
                             Model model) {
        if (StringUtils.hasText(productForm.getName())) {
            productService.findByName(productForm.getName())
                    .ifPresent(existing -> bindingResult.rejectValue("name", "duplicate", "A product with this name already exists"));
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findAll());
            return "add-products";
        }
        productService.create(productForm);
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long productId, RedirectAttributes redirectAttributes) {
        productService.delete(productId);
        redirectAttributes.addFlashAttribute("message", "Product deleted.");
        return "redirect:/admin/products";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long productId,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (StringUtils.hasText(productForm.getName())) {
            productService.findByName(productForm.getName())
                    .filter(existing -> !existing.getProductId().equals(productId))
                    .ifPresent(existing -> bindingResult.rejectValue("name", "duplicate", "A product with this name already exists"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findAll());
            model.addAttribute("editingProductId", productId);
            return "admin-products";
        }

        return productService.update(productId, productForm)
                .map(Product::getProductId)
                .map(id -> {
                    redirectAttributes.addFlashAttribute("message", "Product updated.");
                    return "redirect:/admin/products";
                })
                .orElse("redirect:/admin/products");
    }
}
