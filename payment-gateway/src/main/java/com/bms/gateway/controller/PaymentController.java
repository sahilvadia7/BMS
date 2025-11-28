package com.bms.gateway.controller;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import com.bms.gateway.service.PaymentService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Management", description = "Endpoints for payment processing")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Operation(summary = "Initiate payment", description = "Access: Customer")
	@PostMapping("/initiate")
	public ResponseEntity<PaymentResponse> initiatePayment(
			@Valid @RequestBody PaymentRequest request) throws Exception {
		log.info("Initiating payment for request: {}", request);
		PaymentResponse response = paymentService.processExternalTransfer(request);
		return ResponseEntity.ok(response);
	}
}
