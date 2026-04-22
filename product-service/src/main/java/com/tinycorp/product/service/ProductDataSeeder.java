package com.tinycorp.product.service;

import com.tinycorp.product.domain.Product;
import com.tinycorp.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Component
public class ProductDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ProductDataSeeder.class);
    private final ProductRepository productRepository;

    public ProductDataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        List<String> categories = List.of("Laptop", "Accessory", "Monitor", "Storage", "Peripheral");
        List<Product> products = IntStream.rangeClosed(1, 300)
                .mapToObj(i -> {
                    Product p = new Product();
                    p.setSku("SKU-" + String.format("%04d", i));
                    p.setName("TinyCorp Product " + i);
                    p.setCategory(categories.get(i % categories.size()));
                    p.setPrice(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(25, 2500)).setScale(2, java.math.RoundingMode.HALF_UP));
                    p.setStock(ThreadLocalRandom.current().nextInt(2, 150));
                    return p;
                }).toList();

        productRepository.saveAll(products);
        log.info("Seeded {} products into product-service", products.size());
    }
}
