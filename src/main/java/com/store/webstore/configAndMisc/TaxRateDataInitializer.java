package com.store.webstore.configAndMisc;

import com.store.webstore.Models.TaxRate;
import com.store.webstore.Repositories.TaxRateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TaxRateDataInitializer implements CommandLineRunner {
    private final TaxRateRepository taxRateRepository;

    public TaxRateDataInitializer(TaxRateRepository taxRateRepository) {
        this.taxRateRepository = taxRateRepository;
    }

    @Override
    public void run(String... args) {
        if (taxRateRepository.count() == 0) {
            taxRateRepository.save(new TaxRate("North_America", 8.5, 10.00));
            taxRateRepository.save(new TaxRate("South_America", 7.0, 12.50));
            taxRateRepository.save(new TaxRate("Europe", 10.0, 15.00));
            taxRateRepository.save(new TaxRate("Africa", 5.0, 20.00));
            taxRateRepository.save(new TaxRate("Asia", 12.0, 18.00));
            taxRateRepository.save(new TaxRate("Australia", 6.5, 16.00));
            taxRateRepository.save(new TaxRate("Antarctica", 0.0, 50.00));
        }
    }
}
