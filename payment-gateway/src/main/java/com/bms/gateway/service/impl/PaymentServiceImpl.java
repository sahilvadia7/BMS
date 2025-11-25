package com.bms.gateway.service.impl;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import com.bms.gateway.dto.response.ProviderInitResponse;
import com.bms.gateway.dto.response.ProviderRetryResponse;
import com.bms.gateway.enums.PaymentStatus;
import com.bms.gateway.enums.ProviderPaymentStatus;
import com.bms.gateway.exception.ResourceNotFoundException;
import com.bms.gateway.model.Payment;
import com.bms.gateway.service.PaymentProvider;
import com.bms.gateway.repository.PaymentRepository;
import com.bms.gateway.repository.RefundRepository;
import com.bms.gateway.service.PaymentService;
import com.bms.gateway.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.security.ProviderException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentProvider paymentProvider;

	@Override
	@Transactional
	public PaymentResponse initiatePayment(PaymentRequest request) throws BadRequestException {

		log.info("Initiating payment for transactionId: {}", request.getTransactionId());

		if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException("Amount must be greater than 0");
		}

		Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
		if (existing.isPresent()) {
			log.warn("Duplicate payment request detected for idempotencyKey={}", request.getIdempotencyKey());
			return PaymentResponse.fromEntity(existing.get());
		}

		Payment payment = Payment.builder()
				.transactionId(request.getTransactionId())
				.amount(request.getAmount())
				.method(request.getMethod())
				.status(PaymentStatus.INITIATED)
				.idempotencyKey(request.getIdempotencyKey())
				.metadataJson(request.getMetadataJson())
				.build();

		paymentRepository.save(payment);

		ProviderInitResponse providerInit = paymentProvider.initiate(payment);

		payment.setProviderReference(providerInit.getProviderReference());
		payment.setProviderResponse(providerInit.getRawResponse());
		paymentRepository.save(payment);

		return PaymentResponse.fromEntity(payment);
	}

	@Override
	@Transactional
	public PaymentResponse processPayment(String paymentId) {

		Payment payment = getPaymentOrThrow(paymentId);

		if (payment.getStatus() == PaymentStatus.SUCCESS ||
				payment.getStatus() == PaymentStatus.FAILED) {
			log.info("Payment {} already completed, skipping processing.", paymentId);
			return PaymentResponse.fromEntity(payment);
		}

		payment.setStatus(PaymentStatus.PROCESSING);
		paymentRepository.save(payment);

		ProviderPaymentStatus providerStatus;

		try {
			providerStatus = paymentProvider.checkStatus(payment);
		} catch (Exception e) {
			log.error("Provider error while checking status for {}", paymentId, e);
			payment.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment);
			throw new ProviderException("Payment provider failed");
		}

		// Apply state machine rules
		applyPaymentStatusTransition(payment, providerStatus);

		paymentRepository.save(payment);
		return PaymentResponse.fromEntity(payment);
	}

	@Override
	@Transactional
	public void confirmPayment(String providerPaymentId, boolean isSuccess, String rawPayload) {

		Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found for providerPaymentId"));

		if (payment.getStatus() == PaymentStatus.SUCCESS ||
				payment.getStatus() == PaymentStatus.FAILED) {
			log.info("Webhook duplicate: Payment {} already final", payment.getId());
			return;
		}

		payment.setProviderResponse(rawPayload);

		if (isSuccess) {
			payment.setStatus(PaymentStatus.SUCCESS);
		} else {
			payment.setStatus(PaymentStatus.FAILED);
		}

		paymentRepository.save(payment);
	}

	@Override
	public PaymentResponse getPayment(String paymentId) {
		return PaymentResponse.fromEntity(getPaymentOrThrow(paymentId));
	}

	@Override
	@Transactional
	public PaymentResponse cancelPayment(String paymentId) throws BadRequestException {

		Payment payment = getPaymentOrThrow(paymentId);

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			throw new BadRequestException("Cannot cancel a successful payment");
		}
		if (payment.getStatus() == PaymentStatus.FAILED ||
				payment.getStatus() == PaymentStatus.CANCELLED) {
			return PaymentResponse.fromEntity(payment);
		}

		providerCancel(payment);

		payment.setStatus(PaymentStatus.CANCELLED);
		paymentRepository.save(payment);

		return PaymentResponse.fromEntity(payment);
	}

	@Override
	@Transactional
	public PaymentResponse retryPayment(String paymentId) throws BadRequestException {

		Payment payment = getPaymentOrThrow(paymentId);

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			throw new BadRequestException("Cannot retry a successful payment");
		}

		// Create new attempt inside provider
		ProviderRetryResponse retry =
				paymentProvider.retry(payment);

		payment.setProviderReference(retry.getProviderReference());
		payment.setProviderResponse(retry.getRawResponse());
		payment.setStatus(PaymentStatus.PROCESSING);

		paymentRepository.save(payment);

		return PaymentResponse.fromEntity(payment);
	}

	private Payment getPaymentOrThrow(String id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
	}

	private void applyPaymentStatusTransition(Payment payment, ProviderPaymentStatus providerStatus) {

		switch (providerStatus) {

			case SUCCESS -> payment.setStatus(PaymentStatus.SUCCESS);
			case FAILED -> payment.setStatus(PaymentStatus.FAILED);
			case PENDING -> payment.setStatus(PaymentStatus.PENDING);
			case AUTHORIZED -> payment.setStatus(PaymentStatus.AUTHORIZED);
			case PROCESSING -> payment.setStatus(PaymentStatus.PROCESSING);
			default -> throw new IllegalStateException("Unknown provider payment status");
		}
	}

	private void providerCancel(Payment payment) {
		try {
			paymentProvider.cancel(payment);
		} catch (Exception e) {
			log.error("Provider failed to cancel payment {}", payment.getId(), e);
			throw new ProviderException("Provider cancellation failed");
		}
	}

	@Override
	@Transactional
	public void handlePaymentWebhook(String rawPayload) {

		log.info("Processing payment webhook payload={}", rawPayload);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(rawPayload);

			String providerPaymentId = json.get("providerPaymentId").asText();
			String statusStr = json.get("status").asText();
			String signature = json.has("signature") ? json.get("signature").asText() : null;

			if (signature != null && !paymentProvider.verifyWebhookSignature(rawPayload, signature)) {
				log.error("Invalid webhook signature for providerPaymentId={}", providerPaymentId);
				throw new SecurityException("Invalid webhook signature");
			}

			Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId)
					.orElseThrow(() -> new ResourceNotFoundException(
							"Payment not found for providerPaymentId=" + providerPaymentId));

			PaymentStatus newStatus = mapWebhookStatus(statusStr);

			if (payment.getStatus() == PaymentStatus.SUCCESS ||
					payment.getStatus() == PaymentStatus.FAILED ||
					payment.getStatus() == PaymentStatus.CANCELLED) {

				log.info("Webhook ignored — Payment {} already in final state: {}",
						payment.getId(), payment.getStatus());
				return;
			}

			payment.setProviderResponse(rawPayload);
			payment.setStatus(newStatus);

			paymentRepository.save(payment);

			log.info("Payment webhook processed successfully for paymentId={} -> {}",
					payment.getId(), newStatus);

		} catch (Exception e) {
			log.error("Failed to process payment webhook", e);
			throw new RuntimeException("Invalid payment webhook payload");
		}
	}

	private PaymentStatus mapWebhookStatus(String providerStatus) {

		return switch (providerStatus.toUpperCase()) {
			case "SUCCESS", "CAPTURED", "PAID" -> PaymentStatus.SUCCESS;
			case "FAILED", "DECLINED", "ERROR" -> PaymentStatus.FAILED;
			case "PENDING" -> PaymentStatus.PENDING;
			case "AUTHORIZED" -> PaymentStatus.AUTHORIZED;
			case "PROCESSING" -> PaymentStatus.PROCESSING;
			case "CANCELLED" -> PaymentStatus.CANCELLED;
			default -> throw new IllegalArgumentException("Unknown provider webhook status");
		};
	}

}

