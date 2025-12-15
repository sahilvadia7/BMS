package com.bms.transaction.dto.response;

import com.bms.transaction.model.Transaction;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransactionResponseDto(
		String transactionId,
		String redirectUrl,
		String sourceAccountNumber,
		String destinationAccountNumber,
        String transactionType,
        BigDecimal amount,
        LocalDateTime transactionDate,
		String currency,
		String channel,
        String status,
        String description,
        String referenceId
) {
	public static TransactionResponseDto failed(Transaction txn) {
		return TransactionResponseDto.builder()
				.transactionId(txn.getTransactionId())
				.redirectUrl(null)
				.sourceAccountNumber(txn.getAccountNumber())
				.destinationAccountNumber(txn.getDestinationAccountNumber())
				.transactionType(txn.getTransactionType().name())
				.amount(txn.getAmount())
				.transactionDate(txn.getTransactionDate())
				.currency(txn.getCurrency().name())
				.channel(txn.getChannelReferenceId())
				.status(txn.getStatus().name())
				.description("Transaction failed: " +
						(txn.getDescription() != null ? txn.getDescription() : "Unknown error"))
				.referenceId(txn.getTransactionId())
				.build();
	}
}