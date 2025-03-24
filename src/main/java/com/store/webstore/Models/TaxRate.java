package com.store.webstore.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "tax_rates")
public class TaxRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String continent;
    private double taxRate;
    private double shippingCost;

    public TaxRate() {}

    public TaxRate(String continent, double taxRate, double shippingCost) {
        this.continent = continent;
        this.taxRate = taxRate;
        this.shippingCost = shippingCost;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }
}
