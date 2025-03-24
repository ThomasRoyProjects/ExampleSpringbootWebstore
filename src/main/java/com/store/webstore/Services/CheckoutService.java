package com.store.webstore.Services;

import com.store.webstore.Models.TaxRate;
import com.store.webstore.Repositories.TaxRateRepository;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    private final TaxRateRepository taxRateRepository;

    public CheckoutService(TaxRateRepository taxRateRepository) {
        this.taxRateRepository = taxRateRepository;
    }
public double getTaxRate(String continent) {
    TaxRate taxRate = taxRateRepository.findByContinent(continent);
    if (taxRate == null) {
        throw new IllegalArgumentException("Tax rate not found for continent: " + continent);
    }
    return taxRate.getTaxRate();
}


    public double getShippingCost(String continent) {
        TaxRate taxRate = taxRateRepository.findByContinent(continent);
        return (taxRate != null) ? taxRate.getShippingCost() : 0.0;
    }
}
