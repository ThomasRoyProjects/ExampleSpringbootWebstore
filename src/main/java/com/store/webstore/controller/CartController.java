package com.store.webstore.controller;

import com.store.webstore.dto.CartCommand;
import com.store.webstore.exception.ProductNotFoundException;
import com.store.webstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(Model model) {
        populateCartModel(model);
        return "cart";
    }

    @GetMapping("/items")
    public String getCartItems(Model model) {
        populateCartModel(model);
        return "cart :: cart-items";
    }

    @PostMapping("/add")
    public String addToCart(@Valid @RequestBody CartCommand command, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }
        cartService.addProductToCart(command.getProductId(), command.getQuantity());
        populateCartModel(model);
        return "cart :: cart-items";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") Long productId, Model model) {
        cartService.removeProductFromCart(productId);
        populateCartModel(model);
        return "cart :: cart-items";
    }

    @PostMapping("/clear")
    public String clearCart(Model model) {
        cartService.clearCart();
        populateCartModel(model);
        return "cart :: cart-items";
    }

    @ExceptionHandler({IllegalArgumentException.class, ProductNotFoundException.class})
    @ResponseBody
    public ResponseEntity<String> handleBadCartCommand(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(renderError(ex.getMessage()));
    }

    private void populateCartModel(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
    }

    private static String renderError(String message) {
        return "<div class='cart-error'>" + escape(message) + "</div>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
