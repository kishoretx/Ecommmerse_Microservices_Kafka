package com.tinycorp.dashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DashboardService {

    private final RestClient restClient;
    private final String productServiceUrl;
    private final String orderServiceUrl;
    private final String paymentServiceUrl;
    private final String notificationServiceUrl;

    public DashboardService(@Value("${services.product.url:http://localhost:8081}") String productServiceUrl,
                            @Value("${services.order.url:http://localhost:8082}") String orderServiceUrl,
                            @Value("${services.payment.url:http://localhost:8083}") String paymentServiceUrl,
                            @Value("${services.notification.url:http://localhost:8084}") String notificationServiceUrl) {
        this.restClient = RestClient.builder().build();
        this.productServiceUrl = productServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
        this.notificationServiceUrl = notificationServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> aggregateMetrics() {
        Map<String, Object> out = new HashMap<>();
        out.put("products", fetchMap(productServiceUrl + "/api/products/metrics"));
        out.put("orders", fetchMap(orderServiceUrl + "/api/orders/metrics"));
        out.put("payments", fetchMap(paymentServiceUrl + "/api/payments/metrics"));
        out.put("notifications", fetchMap(notificationServiceUrl + "/api/notifications/metrics"));
        return out;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listProducts() {
        List<Map<String, Object>> products = restClient.get()
                .uri(productServiceUrl + "/api/products")
                .retrieve()
                .body(List.class);
        return products == null ? List.of() : products;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> placeOrder(Map<String, Object> request) {
        Map<String, Object> response = restClient.post()
                .uri(orderServiceUrl + "/api/orders")
                .body(request)
                .retrieve()
                .body(Map.class);
        return response == null ? Map.of("error", "No response from order-service") : response;
    }

    public Map<String, Object> createDemoOrders(int count) {
        int created = 0;
        for (int i = 0; i < count; i++) {
            long p1 = ThreadLocalRandom.current().nextLong(1, 200);
            long p2 = ThreadLocalRandom.current().nextLong(1, 200);
            Map<String, Object> payload = Map.of(
                    "customerName", "UI Customer " + ThreadLocalRandom.current().nextInt(1000, 9999),
                    "customerEmail", "shopper" + ThreadLocalRandom.current().nextInt(1000, 9999) + "@tinycorp.dev",
                    "items", new Object[]{
                            Map.of("productId", p1, "quantity", 1),
                            Map.of("productId", p2, "quantity", 2)
                    }
            );

            try {
                restClient.post()
                        .uri(orderServiceUrl + "/api/orders")
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                created++;
            } catch (Exception ignored) {
            }
        }
        return Map.of("requested", count, "created", created);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchMap(String url) {
        try {
            Map<String, Object> result = restClient.get().uri(url).retrieve().body(Map.class);
            return result == null ? Map.of("error", "No response") : result;
        } catch (Exception ex) {
            return Map.of("error", ex.getMessage());
        }
    }
}
