package com.tinycorp.order.service;

import com.tinycorp.common.events.OrderCreatedEvent;
import com.tinycorp.common.events.OrderFulfilledEvent;
import com.tinycorp.common.events.PaymentProcessedEvent;
import com.tinycorp.order.controller.CreateOrderRequest;
import com.tinycorp.order.domain.CustomerOrder;
import com.tinycorp.order.domain.OrderItem;
import com.tinycorp.order.domain.OrderStatus;
import com.tinycorp.order.repository.CustomerOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final CustomerOrderRepository orderRepository;
    private final RestClient restClient;
    private final KafkaTemplate<String, OrderCreatedEvent> orderKafkaTemplate;
    private final KafkaTemplate<String, OrderFulfilledEvent> fulfillmentKafkaTemplate;
    private final String productServiceBaseUrl;

    public OrderService(CustomerOrderRepository orderRepository,
                        RestClient restClient,
                        KafkaTemplate<String, OrderCreatedEvent> orderKafkaTemplate,
                        KafkaTemplate<String, OrderFulfilledEvent> fulfillmentKafkaTemplate,
                        @Value("${services.product.url:http://localhost:8081}") String productServiceBaseUrl) {
        this.orderRepository = orderRepository;
        this.restClient = restClient;
        this.orderKafkaTemplate = orderKafkaTemplate;
        this.fulfillmentKafkaTemplate = fulfillmentKafkaTemplate;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }

    @Transactional
    public CustomerOrder createOrder(CreateOrderRequest request) {
        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderLineRequest line : request.items()) {
            ProductLookup product = restClient.get()
                    .uri(productServiceBaseUrl + "/api/products/{id}", line.productId())
                    .retrieve()
                    .body(ProductLookup.class);

            restClient.post()
                    .uri(productServiceBaseUrl + "/api/products/{id}/reserve?quantity={q}", line.productId(), line.quantity())
                    .retrieve()
                    .toBodilessEntity();

            OrderItem item = new OrderItem();
            item.setProductId(line.productId());
            item.setProductName(product.name());
            item.setQuantity(line.quantity());
            item.setUnitPrice(product.price());
            order.addItem(item);

            total = total.add(product.price().multiply(BigDecimal.valueOf(line.quantity())));
        }

        order.setTotalAmount(total);
        CustomerOrder saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getOrderNumber(),
                saved.getCustomerName(),
                saved.getCustomerEmail(),
                saved.getTotalAmount(),
                saved.getCreatedAt()
        );

        orderKafkaTemplate.send("orders.created", saved.getOrderNumber(), event);
        log.info("Order {} created and published to Kafka", saved.getOrderNumber());

        return saved;
    }

    @KafkaListener(topics = "payments.processed", groupId = "order-service", containerFactory = "paymentKafkaListenerContainerFactory")
    @Transactional
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        orderRepository.findByOrderNumber(event.orderNumber()).ifPresent(order -> {
            order.setUpdatedAt(Instant.now());
            if ("SUCCESS".equalsIgnoreCase(event.status())) {
                order.setStatus(OrderStatus.FULFILLED);
                fulfillmentKafkaTemplate.send("orders.fulfilled", order.getOrderNumber(),
                        new OrderFulfilledEvent(order.getOrderNumber(), order.getCustomerEmail(), "FULFILLED", Instant.now()));
                log.info("Order {} fulfilled after successful payment", order.getOrderNumber());
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                log.warn("Order {} payment failed", order.getOrderNumber());
            }
        });
    }

    public List<CustomerOrder> allOrders() {
        return orderRepository.findAll();
    }

    public Map<String, Object> metrics() {
        return Map.of(
                "totalOrders", orderRepository.count(),
                "fulfilledOrders", orderRepository.countByStatus(OrderStatus.FULFILLED),
                "failedOrders", orderRepository.countByStatus(OrderStatus.PAYMENT_FAILED),
                "pendingOrders", orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING)
        );
    }
}
