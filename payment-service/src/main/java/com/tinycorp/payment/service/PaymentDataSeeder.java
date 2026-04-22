package com.tinycorp.payment.service;

import com.tinycorp.payment.domain.PaymentTransaction;
import com.tinycorp.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.IntStream;

@Component
public class PaymentDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PaymentDataSeeder.class);
    private final PaymentRepository paymentRepository;

    public PaymentDataSeeder(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void run(String... args) {
        if (paymentRepository.count() > 0) {
            return;
        }

        IntStream.rangeClosed(1, 220).forEach(i -> {
            PaymentTransaction tx = new PaymentTransaction();
            tx.setOrderNumber("HIST-" + String.format("%05d", i));
            tx.setAmount(BigDecimal.valueOf(100 + i * 3L));
            tx.setPaymentMethod(i % 2 == 0 ? "CARD" : "UPI");
            tx.setStatus(i % 10 == 0 ? "FAILED" : "SUCCESS");
            tx.setReference("SEED-PAY-" + i);
            tx.setProcessedAt(Instant.now().minusSeconds(i * 1200L));
            paymentRepository.save(tx);
        });

        log.info("Seeded 220 payment transactions");
    }
}
