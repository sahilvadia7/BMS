package com.bms.gateway.controller;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import com.bms.gateway.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/initiate")
	public ResponseEntity<PaymentResponse> initiatePayment(
			@Valid @RequestBody PaymentRequest request
	) throws Exception {
		log.info("Initiating payment for request: {}", request);
		PaymentResponse response = paymentService.processExternalTransfer(request);
		return ResponseEntity.ok(response);
	}
}
