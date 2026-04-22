package com.tinycorp.payment.service;

import com.tinycorp.common.events.OrderCreatedEvent;
import com.tinycorp.common.events.PaymentProcessedEvent;
import com.tinycorp.payment.domain.PaymentTransaction;
import com.tinycorp.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorService.class);
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentProcessedEvent> paymentKafkaTemplate;

    public PaymentProcessorService(PaymentRepository paymentRepository, KafkaTemplate<String, PaymentProcessedEvent> paymentKafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.paymentKafkaTemplate = paymentKafkaTemplate;
    }

    @KafkaListener(topics = "orders.created", groupId = "payment-service", containerFactory = "orderKafkaListenerContainerFactory")
    public void process(OrderCreatedEvent event) {
        boolean success = ThreadLocalRandom.current().nextInt(1, 100) > 10;
        String status = success ? "SUCCESS" : "FAILED";
        String reference = "PAY-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderNumber(event.orderNumber());
        transaction.setAmount(event.totalAmount());
        transaction.setPaymentMethod(ThreadLocalRandom.current().nextBoolean() ? "CARD" : "UPI");
        transaction.setStatus(status);
        transaction.setReference(reference);
        transaction.setProcessedAt(Instant.now());

        paymentRepository.save(transaction);
        paymentKafkaTemplate.send("payments.processed", event.orderNumber(),
                new PaymentProcessedEvent(event.orderNumber(), event.totalAmount(), transaction.getPaymentMethod(), status, reference, Instant.now()));

        log.info("Payment {} for order {} processed with status={}", reference, event.orderNumber(), status);
    }

    public Map<String, Object> metrics() {
        return Map.of(
                "totalPayments", paymentRepository.count(),
                "successfulPayments", paymentRepository.countByStatus("SUCCESS"),
                "failedPayments", paymentRepository.countByStatus("FAILED")
        );
    }
}
