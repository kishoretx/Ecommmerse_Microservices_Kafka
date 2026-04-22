package com.tinycorp.common.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessedEvent(
        String orderNumber,
        BigDecimal amount,
        String paymentMethod,
        String status,
        String reference,
        Instant processedAt
) {
}
