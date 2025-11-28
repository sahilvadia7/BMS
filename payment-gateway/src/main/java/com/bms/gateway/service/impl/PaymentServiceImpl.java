package com.bms.gateway.service.impl;

import com.bms.gateway.dto.request.PaymentRequest;
import com.bms.gateway.dto.response.PaymentResponse;
import com.bms.gateway.enums.TransactionStatus;
import com.bms.gateway.model.Payment;
import com.bms.gateway.repository.PaymentRepository;
import com.bms.gateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repository;

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
			boolean success = callExternalBankAPI(txn);

			if (success) {
				txn.setStatus(TransactionStatus.COMPLETED);
				txn.setCompletedAt(LocalDateTime.now());
			} else {
				txn.setStatus(TransactionStatus.FAILED);
				txn.setFailureReason("Bank API rejected the transaction");
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
