package com.store.webstore.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.store.webstore.service.ProductService;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductService productService;
    private final boolean seedProducts;

    public ProductDataInitializer(ProductService productService,
                                  @Value("${webstore.seed.products:true}") boolean seedProducts) {
        this.productService = productService;
        this.seedProducts = seedProducts;
    }

    @Override
    public void run(String... args) {
        if (!seedProducts) {
            return;
        }

        productService.upsertSeed(
                "Body Lotion",
                "A soothing body lotion for daily use.",
                BigDecimal.valueOf(19.99),
                "/images/bodylotion.jpg",
                100);
        productService.upsertSeed(
                "Face Cream",
                "A hydrating face cream with natural ingredients.",
                BigDecimal.valueOf(29.99),
                "/images/facecream.jpg",
                150);
    }
}
