package com.tinycorp.payment.controller;

import com.tinycorp.payment.service.PaymentProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProcessorService paymentProcessorService;

    public PaymentController(PaymentProcessorService paymentProcessorService) {
        this.paymentProcessorService = paymentProcessorService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Payment metrics for dashboard")
    public Map<String, Object> metrics() {
        return paymentProcessorService.metrics();
    }
}
