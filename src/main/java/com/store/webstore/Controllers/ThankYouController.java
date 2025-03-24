package com.store.webstore.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThankYouController {
    @GetMapping("/thank-you")
    public String showThankYouPage() {
        return "thank-you";
    }
}