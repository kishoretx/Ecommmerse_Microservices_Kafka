package com.tinycorp.dashboard.controller;

import com.tinycorp.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "TinyCorp Ecommerce Control Center");
        return "index";
    }

    @GetMapping("/api/dashboard/metrics")
    @ResponseBody
    @Operation(summary = "Aggregate metrics from all microservices")
    public Map<String, Object> metrics() {
        return dashboardService.aggregateMetrics();
    }

    @GetMapping("/api/dashboard/products")
    @ResponseBody
    @Operation(summary = "Get products for order placement UI")
    public List<Map<String, Object>> products() {
        return dashboardService.listProducts();
    }

    @PostMapping("/api/dashboard/orders")
    @ResponseBody
    @Operation(summary = "Place an order through dashboard")
    public Map<String, Object> placeOrder(@RequestBody Map<String, Object> request) {
        return dashboardService.placeOrder(request);
    }

    @PostMapping("/api/dashboard/demo-orders")
    @ResponseBody
    @Operation(summary = "Create random demo orders from dashboard")
    public Map<String, Object> createDemoOrders(@RequestParam(name = "count", defaultValue = "5") int count) {
        return dashboardService.createDemoOrders(Math.max(1, Math.min(count, 50)));
    }
}
