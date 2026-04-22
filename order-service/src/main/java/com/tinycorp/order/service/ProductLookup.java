package com.tinycorp.order.service;

import java.math.BigDecimal;

public record ProductLookup(Long id, String name, BigDecimal price, Integer stock) {
}
