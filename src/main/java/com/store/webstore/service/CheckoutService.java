package com.store.webstore.service;

import com.store.webstore.model.TaxRate;
import com.store.webstore.repository.TaxRateRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    private final TaxRateRepository taxRateRepository;

    public CheckoutService(TaxRateRepository taxRateRepository) {
        this.taxRateRepository = taxRateRepository;
    }

    public TaxRate getTaxRule(String continent) {
        if (continent == null || continent.isBlank()) {
            throw new IllegalArgumentException("Continent is required");
        }
        TaxRate taxRate = taxRateRepository.findByContinent(continent);
        if (taxRate == null) {
            throw new IllegalArgumentException("Unsupported continent");
        }
        return taxRate;
    }

    public CheckoutTotals calculateTotals(BigDecimal subtotal, String continent) {
        TaxRate taxRate = getTaxRule(continent);
        BigDecimal scaledSubtotal = money(subtotal);
        BigDecimal rate = taxRate.getTaxRate().setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = money(taxRate.getShippingCost());
        BigDecimal taxAmount = scaledSubtotal.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal finalPrice = scaledSubtotal.add(taxAmount).add(shipping).setScale(2, RoundingMode.HALF_UP);
        return new CheckoutTotals(taxRate.getContinent(), scaledSubtotal, rate, shipping, taxAmount, finalPrice);
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    public record CheckoutTotals(
            String continent,
            BigDecimal subtotal,
            BigDecimal taxRate,
            BigDecimal shippingCost,
            BigDecimal taxAmount,
            BigDecimal finalPrice
    ) {
    }
}
