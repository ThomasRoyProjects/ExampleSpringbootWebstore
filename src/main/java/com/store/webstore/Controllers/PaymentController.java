package com.store.webstore.Controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {
    
    @GetMapping("/payment")
    public String paymentPage() {
        return "payment";  
    }
}
