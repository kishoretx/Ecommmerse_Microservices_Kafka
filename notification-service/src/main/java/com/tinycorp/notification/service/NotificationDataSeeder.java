package com.tinycorp.notification.service;

import com.tinycorp.notification.domain.NotificationLog;
import com.tinycorp.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.IntStream;

@Component
public class NotificationDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(NotificationDataSeeder.class);
    private final NotificationRepository notificationRepository;

    public NotificationDataSeeder(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (notificationRepository.count() > 0) {
            return;
        }
        IntStream.rangeClosed(1, 160).forEach(i -> {
            NotificationLog n = new NotificationLog();
            n.setEventType(i % 2 == 0 ? "PAYMENT" : "FULFILLMENT");
            n.setRecipient(i % 2 == 0 ? "finance@tinycorp.dev" : "customer" + i + "@tinycorp.dev");
            n.setMessage("Seed notification " + i);
            n.setStatus("SENT");
            n.setCreatedAt(Instant.now().minusSeconds(i * 500L));
            notificationRepository.save(n);
        });
        log.info("Seeded 160 notification logs");
    }
}
