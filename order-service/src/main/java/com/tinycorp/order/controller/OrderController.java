package com.tinycorp.order.controller;

import com.tinycorp.order.domain.CustomerOrder;
import com.tinycorp.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create order and emit order-created event")
    public CustomerOrder create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    @Operation(summary = "List all orders")
    public List<CustomerOrder> all() {
        return orderService.allOrders();
    }

    @GetMapping("/metrics")
    @Operation(summary = "Order metrics for dashboard")
    public Map<String, Object> metrics() {
        return orderService.metrics();
    }
}
