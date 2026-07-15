package com.store.webstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.store.webstore.model.TaxRate;
import com.store.webstore.repository.TaxRateRepository;

@Component
public class TaxRateDataInitializer implements CommandLineRunner {
    private final TaxRateRepository taxRateRepository;
    private final boolean seedTaxRates;

    public TaxRateDataInitializer(TaxRateRepository taxRateRepository,
                                  @Value("${webstore.seed.tax-rates:true}") boolean seedTaxRates) {
        this.taxRateRepository = taxRateRepository;
        this.seedTaxRates = seedTaxRates;
    }

    @Override
    public void run(String... args) {
        if (!seedTaxRates) {
            return;
        }

        upsert("North_America", 8.5, 10.00);
        upsert("South_America", 7.0, 12.50);
        upsert("Europe", 10.0, 15.00);
        upsert("Africa", 5.0, 20.00);
        upsert("Asia", 12.0, 18.00);
        upsert("Australia", 6.5, 16.00);
        upsert("Antarctica", 0.0, 50.00);
    }

    private void upsert(String continent, double taxRate, double shippingCost) {
        TaxRate rate = taxRateRepository.findByContinent(continent);
        if (rate == null) {
            rate = new TaxRate();
            rate.setContinent(continent);
        }
        rate.setTaxRate(taxRate);
        rate.setShippingCost(shippingCost);
        taxRateRepository.save(rate);
    }
}
