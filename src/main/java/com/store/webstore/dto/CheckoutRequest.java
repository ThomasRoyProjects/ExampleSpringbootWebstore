package com.store.webstore.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckoutRequest {
    @NotBlank(message = "Continent is required")
    private String continent;

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }
}
