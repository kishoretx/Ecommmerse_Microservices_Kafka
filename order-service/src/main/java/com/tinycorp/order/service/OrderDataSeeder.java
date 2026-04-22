package com.tinycorp.order.service;

import com.tinycorp.order.domain.CustomerOrder;
import com.tinycorp.order.domain.OrderItem;
import com.tinycorp.order.domain.OrderStatus;
import com.tinycorp.order.repository.CustomerOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Component
public class OrderDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(OrderDataSeeder.class);
    private final CustomerOrderRepository orderRepository;

    public OrderDataSeeder(CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }

        IntStream.rangeClosed(1, 180).forEach(i -> {
            CustomerOrder order = new CustomerOrder();
            order.setOrderNumber("HIST-" + String.format("%05d", i));
            order.setCustomerName("Customer " + i);
            order.setCustomerEmail("customer" + i + "@tinycorp.dev");
            order.setStatus(i % 9 == 0 ? OrderStatus.PAYMENT_FAILED : OrderStatus.FULFILLED);
            order.setCreatedAt(Instant.now().minusSeconds(3600L * i));
            order.setUpdatedAt(Instant.now().minusSeconds(1800L * i));

            BigDecimal total = BigDecimal.ZERO;
            for (int j = 1; j <= 2; j++) {
                OrderItem item = new OrderItem();
                int quantity = ThreadLocalRandom.current().nextInt(1, 4);
                BigDecimal price = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(90, 1700)).setScale(2, java.math.RoundingMode.HALF_UP);
                item.setProductId((long) ThreadLocalRandom.current().nextInt(1, 250));
                item.setProductName("Seed Product " + item.getProductId());
                item.setQuantity(quantity);
                item.setUnitPrice(price);
                order.addItem(item);
                total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
            order.setTotalAmount(total);
            orderRepository.save(order);
        });
        log.info("Seeded 180 orders into order-service");
    }
}
