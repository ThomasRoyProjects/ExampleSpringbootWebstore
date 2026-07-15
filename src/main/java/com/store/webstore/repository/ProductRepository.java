package com.store.webstore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store.webstore.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameIgnoreCase(String name);
}
