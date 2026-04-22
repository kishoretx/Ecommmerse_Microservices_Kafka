package com.tinycorp.common.events;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String orderNumber,
        String customerName,
        String customerEmail,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
