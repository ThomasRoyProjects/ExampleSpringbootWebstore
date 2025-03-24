package com.store.webstore.Repositories;

import com.store.webstore.Models.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {
    TaxRate findByContinent(String continent);
}
