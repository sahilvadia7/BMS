package com.bms.gateway.service.impl;

import com.bms.gateway.dto.request.RefundRequest;
import com.bms.gateway.dto.response.RefundResponse;
import com.bms.gateway.exception.ResourceNotFoundException;
import com.bms.gateway.model.Payment;
import com.bms.gateway.model.Refund;
import com.bms.gateway.enums.PaymentStatus;
import com.bms.gateway.enums.RefundStatus;
import com.bms.gateway.repository.PaymentRepository;
import com.bms.gateway.repository.RefundRepository;
import com.bms.gateway.service.RefundService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class RefundServiceImpl implements RefundService {

	private final RefundRepository refundRepository;
	private final PaymentRepository paymentRepository;

	public RefundServiceImpl(RefundRepository refundRepository,
			PaymentRepository paymentRepository) {
		this.refundRepository = refundRepository;
		this.paymentRepository = paymentRepository;
	}

	/**
	 * CREATE / INITIATE A REFUND (Dummy internal logic only)
	 */
	@Override
	@Transactional
	public RefundResponse initiateRefund(RefundRequest request) {

		log.info("Refund initiated for paymentId={}", request.getPaymentId());

		Payment payment = paymentRepository.findById(request.getPaymentId())
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		if (payment.getStatus() != PaymentStatus.SUCCESS) {
			throw new IllegalStateException("Refund allowed only for successful payments");
		}

		Refund refund = Refund.builder()
				.payment(payment)
				.refundAmount(BigDecimal.valueOf(request.getAmount()))
				.reason(request.getReason())
				.status(RefundStatus.PENDING)
				.createdAt(LocalDateTime.now())
				.build();

		refundRepository.save(refund);

		log.info("Refund record created refundId={}", refund.getId());

		return RefundResponse.from(refund);
	}

	/**
	 * CHANGE REFUND STATUS INTERNALLY (since no provider exists)
	 * You will call this from admin panel or internal logic
	 */
	@Override
	@Transactional
	public RefundResponse updateRefundStatus(String refundId, RefundStatus newStatus) {

		Refund refund = refundRepository.findById(refundId)
				.orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

		refund.setStatus(newStatus);
		refund.setUpdatedAt(LocalDateTime.now());

		refundRepository.save(refund);

		return RefundResponse.from(refund);
	}

	/**
	 * LIST ALL REFUNDS FOR A PAYMENT
	 */
	@Override
	@Transactional(readOnly = true)
	public RefundResponse getRefundsByPayment(String paymentId) {

		paymentRepository.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

		return refundRepository.findByPaymentId(paymentId);
	}

	/**
	 * HANDLE DUMMY REFUND WEBHOOK (Optional)
	 * No provider → webhook may just update status
	 */
	@Override
	@Transactional
	public void handleRefundWebhook(String payload) {
		log.info("Processing refund webhook payload={}", payload);

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(payload);

			String refundId = json.get("refundId").asText();
			String statusStr = json.get("status").asText();
			String providerRefundId = json.has("providerRefundId")
					? json.get("providerRefundId").asText()
					: null;

			RefundStatus newStatus = RefundStatus.valueOf(statusStr.toUpperCase());

			Refund refund = refundRepository.findById(refundId)
					.orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

			refund.setStatus(newStatus);
			refund.setProviderRefundId(providerRefundId);
			refund.setProviderResponse(payload);
			refund.setUpdatedAt(LocalDateTime.now());
			refundRepository.save(refund);

			Payment payment = refund.getPayment();

			if (newStatus == RefundStatus.SUCCESS) {
				payment.setStatus(PaymentStatus.REFUNDED);
				payment.setUpdatedAt(LocalDateTime.now());
				paymentRepository.save(payment);

				log.info("Payment {} marked as REFUNDED", payment.getId());
			}

			if (newStatus == RefundStatus.FAILED) {
				payment.setStatus(PaymentStatus.SUCCESS);
				paymentRepository.save(payment);

				log.info("Refund {} failed. Payment restored to SUCCESS state.", refundId);
			}

			log.info("Refund webhook processed successfully for refundId={}", refundId);

		} catch (Exception e) {
			log.error("Failed to process refund webhook", e);
			throw new RuntimeException("Invalid webhook payload");
		}
	}

}
