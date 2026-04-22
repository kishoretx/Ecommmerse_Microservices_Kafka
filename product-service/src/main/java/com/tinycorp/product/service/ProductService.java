package com.tinycorp.product.service;

import com.tinycorp.product.domain.Product;
import com.tinycorp.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    @Transactional
    public Product reserveStock(Long productId, int quantity) {
        Product product = findById(productId);
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product " + productId);
        }
        product.setStock(product.getStock() - quantity);
        log.info("Reserved {} units from product {}. Remaining stock={}", quantity, productId, product.getStock());
        return product;
    }

    public Map<String, Object> metrics() {
        long total = productRepository.count();
        long lowStock = productRepository.findAll().stream().filter(p -> p.getStock() < 10).count();
        return Map.of(
                "totalProducts", total,
                "lowStockProducts", lowStock,
                "criticalProducts", productRepository.findTop10ByOrderByStockAsc()
        );
    }
}
