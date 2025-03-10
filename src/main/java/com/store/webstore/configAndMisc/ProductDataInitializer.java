package com.store.webstore.configAndMisc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.store.webstore.Models.Product;
import com.store.webstore.Repositories.ProductRepository;

import java.math.BigDecimal;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    //this will run everytime the instance is restarted, some example products added below, change this in application.properties for persistance: spring.jpa.hibernate.ddl-auto=create-drop
    
    @Override
    public void run(String... args) throws Exception {
        Product product1 = new Product();
        product1.setName("Body Lotion");
        product1.setDescription("A soothing body lotion for daily use.");
        product1.setPrice(BigDecimal.valueOf(19.99));
        product1.setImageUrl("/images/bodylotion.jpg"); 
        product1.setStockQuantity(100);

        Product product2 = new Product();
        product2.setName("Face Cream");
        product2.setDescription("A hydrating face cream with natural ingredients.");
        product2.setPrice(BigDecimal.valueOf(29.99));
        product2.setImageUrl("/images/facecream.jpg"); 
        product2.setStockQuantity(150);

        productRepository.save(product1);
        productRepository.save(product2);

        System.out.println("Products saved to the database");
    }
}
