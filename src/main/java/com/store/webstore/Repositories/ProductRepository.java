package com.store.webstore.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store.webstore.Models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
