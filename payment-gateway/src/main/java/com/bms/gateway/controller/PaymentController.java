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

	@Operation(summary = "Process payment", description = "Access: Internal")
	@PostMapping("/{paymentId}/process")
	public ResponseEntity<PaymentResponse> processPayment(
			@PathVariable String paymentId) {
		log.info("Processing payment {}", paymentId);
		PaymentResponse response = paymentService.processPayment(paymentId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Confirm payment", description = "Access: Internal (Payment Provider)")
	@PostMapping("/confirm/{providerPaymentId}")
	public ResponseEntity<Void> confirmPayment(
			@PathVariable String providerPaymentId,
			@RequestParam boolean success,
			@RequestBody(required = false) String rawPayload) {
		log.info("Confirming payment providerId={}, success={}", providerPaymentId, success);
		paymentService.confirmPayment(providerPaymentId, success, rawPayload);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Get payment details", description = "Access: Customer, Admin")
	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentResponse> getPayment(
			@PathVariable String paymentId) {
		log.info("Fetching payment {}", paymentId);
		PaymentResponse response = paymentService.getPayment(paymentId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Cancel payment", description = "Access: Customer, Admin")
	@PostMapping("/{paymentId}/cancel")
	public ResponseEntity<PaymentResponse> cancelPayment(
			@PathVariable String paymentId) throws Exception {
		log.info("Cancelling payment {}", paymentId);
		PaymentResponse response = paymentService.cancelPayment(paymentId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Retry payment", description = "Access: Customer")
	@PostMapping("/{paymentId}/retry")
	public ResponseEntity<PaymentResponse> retryPayment(
			@PathVariable String paymentId) throws Exception {
		log.info("Retrying payment {}", paymentId);
		PaymentResponse response = paymentService.retryPayment(paymentId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Handle payment webhook", description = "Access: Public (Payment Provider)")
	@PostMapping("/webhook")
	public ResponseEntity<String> handlePaymentWebhook(
			@RequestBody String rawPayload,
			@RequestHeader(value = "X-Signature", required = false) String signature) {
		log.info("Received webhook: signature={}, payload={}", signature, rawPayload);

		paymentService.handlePaymentWebhook(rawPayload);

		return ResponseEntity.ok("Webhook Received");
	}
}
