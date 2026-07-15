package com.store.webstore.controller;

import com.store.webstore.model.Order;
import com.store.webstore.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderNumber}")
    public String showReceipt(@PathVariable String orderNumber, Model model) {
        Order order = orderService.getReceipt(orderNumber);
        model.addAttribute("order", order);
        return "thank-you";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        model.addAttribute("error", "Order not found");
        return "thank-you";
    }
}
