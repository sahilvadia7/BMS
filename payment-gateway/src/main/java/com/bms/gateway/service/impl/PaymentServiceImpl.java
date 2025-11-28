package com.bms.gateway.service.impl;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import com.bms.gateway.enums.TransactionStatus;
import com.bms.gateway.model.Payment;
import com.bms.gateway.repository.PaymentRepository;
import com.bms.gateway.service.PaymentService;
import com.bms.gateway.service.RefundService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentProvider paymentProvider;

	public PaymentServiceImpl(PaymentRepository paymentRepository,
			PaymentProvider paymentProvider) {
		this.paymentRepository = paymentRepository;
		this.paymentProvider = paymentProvider;
	}

	@Override
	@Transactional
	public PaymentResponse initiatePayment(PaymentRequest request) throws BadRequestException {

		log.info("Initiating payment for transactionId: {}", request.getTransactionId());

	public PaymentResponse processExternalTransfer(PaymentRequest request) {

		// Check duplicate transaction
		if (repository.findByExternalReferenceId(request.getTransactionId()).isPresent()) {
			throw new IllegalStateException("Duplicate external transaction request");
		}

		Payment txn = Payment.builder()
				.transactionId(request.getTransactionId())
				.sourceAccount(request.getSourceAccount())
				.destinationAccount(request.getDestinationAccount())
				.destinationBankCode(request.getDestinationBankCode())
				.amount(request.getAmount())
				.currency(request.getCurrency())
				.status(TransactionStatus.PENDING)
				.initiatedAt(LocalDateTime.now())
				.externalReferenceId(UUID.randomUUID().toString())
				.build();

		repository.save(txn);

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
		ProviderRetryResponse retry = paymentProvider.retry(payment);

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

		} catch (Exception e) {
			txn.setStatus(TransactionStatus.FAILED);
			txn.setFailureReason(e.getMessage());
		}

		repository.save(txn);

		return PaymentResponse.builder()
				.transactionId(txn.getTransactionId())
				.externalReferenceId(txn.getExternalReferenceId())
				.status(txn.getStatus())
				.amount(txn.getAmount())
				.destinationBankCode(txn.getDestinationBankCode())
				.initiatedAt(txn.getInitiatedAt())
				.completedAt(txn.getCompletedAt())
				.failureReason(txn.getFailureReason())
				.build();
	}


	private boolean callExternalBankAPI(Payment txn) {
		log.info("Calling external bank API for {} -> {}", txn.getSourceAccount(), txn.getDestinationAccount());
		return true;
	}
}
