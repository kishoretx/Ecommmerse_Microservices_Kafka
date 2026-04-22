package com.tinycorp.payment.repository;

import com.tinycorp.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {
    long countByStatus(String status);
}
