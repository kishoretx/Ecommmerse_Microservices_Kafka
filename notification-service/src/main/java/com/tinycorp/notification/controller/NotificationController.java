package com.tinycorp.notification.controller;

import com.tinycorp.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Notification metrics for dashboard")
    public Map<String, Object> metrics() {
        return notificationService.metrics();
    }
}
