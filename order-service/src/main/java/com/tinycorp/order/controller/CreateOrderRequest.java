package com.tinycorp.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerName,
        @Email String customerEmail,
        @NotEmpty List<@Valid OrderLineRequest> items
) {
    public record OrderLineRequest(
            @Min(1) Long productId,
            @Min(1) Integer quantity
    ) {}
}
