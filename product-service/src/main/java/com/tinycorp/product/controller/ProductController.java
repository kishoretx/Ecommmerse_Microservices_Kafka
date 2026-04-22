package com.tinycorp.product.controller;

import com.tinycorp.product.domain.Product;
import com.tinycorp.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List all products")
    public List<Product> all() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public Product byId(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve stock for a product")
    public Product reserve(@PathVariable("id") Long id, @RequestParam("quantity") @Min(1) int quantity) {
        return productService.reserveStock(id, quantity);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Catalog metrics used by dashboard")
    public Map<String, Object> metrics() {
        return productService.metrics();
    }
}
