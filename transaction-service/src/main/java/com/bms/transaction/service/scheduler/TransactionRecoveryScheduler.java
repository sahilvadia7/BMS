package com.bms.transaction.service.scheduler;

import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.InternalTransactionService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@EnableScheduling
public class TransactionRecoveryScheduler {

	private final TransactionRepository transactionRepository;
	private final TransactionService transactionService;

	public TransactionRecoveryScheduler(TransactionRepository transactionRepository,
			TransactionService transactionService) {
		this.transactionRepository = transactionRepository;
		this.transactionService = transactionService;
	}

	@Scheduled(fixedDelay = 300000)
	public void retryFailedCompensations() {
		List<Transaction> failedTxns = transactionRepository
				.findByStatusAndNextRetryTimeBefore(TransactionStatus.FAILED, LocalDateTime.now());

		for (Transaction txn : failedTxns) {
			try {
				log.info("Retrying compensation for transaction [{}] type [{}]",
						txn.getReferenceId(), txn.getTransactionType());

				transactionService.handleCompensation(
						txn.getTransactionType(),
						mapToRequest(txn),
						txn);

				txn.setStatus(TransactionStatus.REFUND);
				txn.setRemarks("Compensation retried successfully");
				txn.setNextRetryTime(null);
				txn.setRetryCount(0);
				transactionRepository.save(txn);

				log.info("Compensation retried successfully for txn [{}]", txn.getReferenceId());

			} catch (Exception e) {
				log.error(" Retry compensation failed for txn [{}]: {}", txn.getReferenceId(), e.getMessage());

				txn.setRetryCount(txn.getRetryCount() + 1);
				txn.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
				txn.setRemarks("Retry compensation failed: " + e.getMessage());
				transactionRepository.save(txn);

				if (txn.getRetryCount() >= 3) {
					txn.setStatus(TransactionStatus.PERMANENT_FAILURE);
					txn.setRemarks("Max retry attempts reached. Manual intervention required.");
					transactionRepository.save(txn);
					log.warn("Max retry attempts reached for txn [{}]", txn.getReferenceId());
				}
			}
		}
	}

	private TransactionRequest mapToRequest(Transaction txn) {
		return new TransactionRequest(
				txn.getAccountNumber(),
				txn.getDestinationAccountNumber(),
				txn.getTransactionType().name(),
				txn.getAmount(),
				txn.getCurrency().name(),
				txn.getChannel().name(),
				"****",
				txn.getDescription());
	}
}
