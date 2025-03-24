package com.store.webstore.Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders") // make sure table name does not conflict with SQL keywords
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private String continent;
    private double taxRate;
    private double shippingCost;
    private double taxAmount;
    private double finalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    public Order() {}

    public Order(String orderNumber, String continent, double taxRate, double shippingCost, double taxAmount, double finalPrice, List<CartItem> items) {
        this.orderNumber = orderNumber;
        this.continent = continent;
        this.taxRate = taxRate;
        this.shippingCost = shippingCost;
        this.taxAmount = taxAmount;
        this.finalPrice = finalPrice;
        this.items = items;
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getContinent() { return continent; }
    public double getTaxRate() { return taxRate; }
    public double getShippingCost() { return shippingCost; }
    public double getTaxAmount() { return taxAmount; }
    public double getFinalPrice() { return finalPrice; }
    public List<CartItem> getItems() { return items; }
}
