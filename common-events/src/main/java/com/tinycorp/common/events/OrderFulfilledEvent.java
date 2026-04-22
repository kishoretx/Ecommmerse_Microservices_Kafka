package com.tinycorp.common.events;

import java.time.Instant;

public record OrderFulfilledEvent(
        String orderNumber,
        String customerEmail,
        String status,
        Instant fulfilledAt
) {
}
