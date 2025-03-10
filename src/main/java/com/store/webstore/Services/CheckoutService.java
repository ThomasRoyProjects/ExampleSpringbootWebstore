package com.store.webstore.Services;

import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Service
public class CheckoutService {

    private Properties taxProperties = new Properties();

    public CheckoutService() {
        // Load properties file
        try (FileInputStream fis = new FileInputStream(new ClassPathResource("taxes.properties").getFile())) {
            taxProperties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getTaxRate(String continent) {
        return Double.parseDouble(taxProperties.getProperty("tax." + continent.toLowerCase(), "0.0"));
    }

    public double getShippingCost(String continent) {
        return Double.parseDouble(taxProperties.getProperty("shipping." + continent.toLowerCase(), "0.0"));
    }
}
