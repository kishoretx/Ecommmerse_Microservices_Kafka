package com.tinycorp.order.repository;

import com.tinycorp.order.domain.CustomerOrder;
import com.tinycorp.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);
    long countByStatus(OrderStatus status);
}
