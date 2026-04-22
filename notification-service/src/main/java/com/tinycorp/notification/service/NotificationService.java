package com.tinycorp.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinycorp.common.events.OrderFulfilledEvent;
import com.tinycorp.common.events.PaymentProcessedEvent;
import com.tinycorp.notification.domain.NotificationLog;
import com.tinycorp.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payments.processed", groupId = "notification-service")
    public void onPayment(String payload) throws Exception {
        PaymentProcessedEvent event = objectMapper.readValue(payload, PaymentProcessedEvent.class);
        NotificationLog note = new NotificationLog();
        note.setEventType("PAYMENT");
        note.setRecipient("finance@tinycorp.dev");
        note.setMessage("Payment for order %s is %s (%s)".formatted(event.orderNumber(), event.status(), event.reference()));
        note.setStatus("SENT");
        note.setCreatedAt(Instant.now());
        notificationRepository.save(note);
        log.info("Payment notification created for order {}", event.orderNumber());
    }

    @KafkaListener(topics = "orders.fulfilled", groupId = "notification-service")
    public void onFulfillment(String payload) throws Exception {
        OrderFulfilledEvent event = objectMapper.readValue(payload, OrderFulfilledEvent.class);
        NotificationLog note = new NotificationLog();
        note.setEventType("FULFILLMENT");
        note.setRecipient(event.customerEmail());
        note.setMessage("Your order %s has been fulfilled.".formatted(event.orderNumber()));
        note.setStatus("SENT");
        note.setCreatedAt(Instant.now());
        notificationRepository.save(note);
        log.info("Fulfillment notification created for order {}", event.orderNumber());
    }

    public Map<String, Object> metrics() {
        return Map.of(
                "totalNotifications", notificationRepository.count(),
                "sentNotifications", notificationRepository.countByStatus("SENT")
        );
    }
}
