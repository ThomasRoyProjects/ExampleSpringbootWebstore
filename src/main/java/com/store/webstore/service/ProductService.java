package com.store.webstore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store.webstore.dto.ProductForm;
import com.store.webstore.model.Product;
import com.store.webstore.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findByName(String name) {
        return productRepository.findByNameIgnoreCase(name);
    }

    @Transactional
    public Product create(ProductForm form) {
        Product product = new Product();
        applyForm(product, form);
        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> update(Long productId, ProductForm form) {
        return productRepository.findById(productId).map(product -> {
            applyForm(product, form);
            return productRepository.save(product);
        });
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

    @Transactional
    public Product upsertSeed(String name, String description, BigDecimal price, String imageUrl, int stockQuantity) {
        Product product = productRepository.findByNameIgnoreCase(name).orElseGet(Product::new);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageUrl(imageUrl);
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }

    public ProductForm toForm(Product product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setImageUrl(product.getImageUrl());
        form.setStockQuantity(product.getStockQuantity());
        return form;
    }

    private void applyForm(Product product, ProductForm form) {
        product.setName(form.getName());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setImageUrl(form.getImageUrl());
        product.setStockQuantity(form.getStockQuantity());
    }
}
