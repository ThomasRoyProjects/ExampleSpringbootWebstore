package com.store.webstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "tax_rates")
public class TaxRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String continent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    public TaxRate() {}

    public TaxRate(String continent, double taxRate, double shippingCost) {
        this.continent = continent;
        setTaxRate(taxRate);
        setShippingCost(shippingCost);
    }

    public Long getId() { return id; }
    public String getContinent() { return continent; }
    public void setContinent(String continent) { this.continent = continent; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = scale(taxRate); }
    public void setTaxRate(double taxRate) { setTaxRate(BigDecimal.valueOf(taxRate)); }
    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = scale(shippingCost); }
    public void setShippingCost(double shippingCost) { setShippingCost(BigDecimal.valueOf(shippingCost)); }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
