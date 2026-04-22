package com.tinycorp.notification.repository;

import com.tinycorp.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {
    long countByStatus(String status);
}
