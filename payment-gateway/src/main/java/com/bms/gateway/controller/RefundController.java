package com.bms.gateway.controller;

import com.bms.gateway.dto.request.RefundRequest;
import com.bms.gateway.dto.response.RefundResponse;
import com.bms.gateway.enums.RefundStatus;
import com.bms.gateway.service.RefundService;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/refunds")
@Tag(name = "Refund Management", description = "Endpoints for refund processing")
public class RefundController {

	private final RefundService refundService;

	public RefundController(RefundService refundService) {
		this.refundService = refundService;
	}

	@Operation(summary = "Initiate refund", description = "Access: Admin, System")
	@PostMapping("/initiate")
	public ResponseEntity<RefundResponse> initiateRefund(
			@Valid @RequestBody RefundRequest request) {
		log.info("Refund initiation request received: {}", request);
		RefundResponse response = refundService.initiateRefund(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Update refund status", description = "Access: Internal")
	@PostMapping("/{refundId}/status")
	public ResponseEntity<RefundResponse> updateRefundStatus(
			@PathVariable String refundId,
			@RequestParam RefundStatus status) {
		log.info("Updating refund status refundId={}, newStatus={}", refundId, status);

		RefundResponse updated = refundService.updateRefundStatus(refundId, status);
		return ResponseEntity.ok(updated);
	}

	@Operation(summary = "Get refunds by payment ID", description = "Access: Admin, Customer")
	@GetMapping("/payment/{paymentId}")
	public ResponseEntity<RefundResponse> getRefundsByPayment(
			@PathVariable String paymentId) {
		log.info("Fetching refunds for paymentId={}", paymentId);
		RefundResponse response = refundService.getRefundsByPayment(paymentId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Handle refund webhook", description = "Access: Public (Payment Provider)")
	@PostMapping("/webhook")
	public ResponseEntity<String> handleRefundWebhook(
			@RequestBody String payload,
			@RequestHeader(value = "X-Signature", required = false) String signature) {
		log.info("Received REFUND webhook: signature={}, payload={}", signature, payload);

		refundService.handleRefundWebhook(payload);

		return ResponseEntity.ok("Refund Webhook Received");
	}
}
