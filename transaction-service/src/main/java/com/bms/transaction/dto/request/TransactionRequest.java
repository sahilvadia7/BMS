package com.bms.transaction.dto.request;

import com.bms.transaction.enums.Currency;
import com.bms.transaction.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

	@NotBlank(message = "initiatedBy is required")
	private String initiatedBy;

	@NotBlank(message = "accountNumber is required")
	private String accountNumber;

	private String destinationAccountNumber;

	@NotNull(message = "amount is required")
	@Positive(message = "amount must be greater than zero")
	private BigDecimal amount;

	@NotNull(message = "currency is required")
	private Currency currency;

	@NotNull(message = "transactionType is required")
	private TransactionType transactionType;

	private String description;

	private String pin;

	@NotBlank(message = "idempotencyKey is required")
	private String idempotencyKey;

	private String channelReferenceId;

	private String branchCode;

	private String linkedTransactionId;

	// ---------------- External Transfer Fields ---------------- //

	/** Bank IFSC or bank code for external transfers */
	private String destinationBankCode;

	/** Payment gateway provider name, e.g., PAYTM, NPCI, INTERNAL_UPI */
	private String gatewayProvider;

	/** Reference number returned by gateway (e.g., UTR, UPI Ref No) */
	private String externalReferenceId;

	public void validateConditionalFields() {
		if (transactionType != null && requiresPin(transactionType)) {
			if (pin == null || pin.isBlank()) {
				throw new IllegalArgumentException("PIN is required for this transaction type");
			}
		}

		if (transactionType != null && requiresDestinationAccount(transactionType)) {
			if (destinationAccountNumber == null || destinationAccountNumber.isBlank()) {
				throw new IllegalArgumentException("destinationAccountNumber is required for this transaction type");
			}
		}

		if (transactionType == TransactionType.REVERSAL) {
			if (linkedTransactionId == null || linkedTransactionId.isBlank()) {
				throw new IllegalArgumentException("linkedTransactionId is required for REVERSAL");
			}
		}

		if (transactionType == TransactionType.CASH_DEPOSIT ||
				transactionType == TransactionType.CASH_WITHDRAWAL) {

			if (destinationAccountNumber != null && !destinationAccountNumber.isBlank()) {
				throw new IllegalArgumentException("destinationAccountNumber is not allowed for cash operations");
			}
		}

		// ---------------- External Transfer Validation ---------------- //
		if (transactionType == TransactionType.EXTERNAL_TRANSFER) {
			if (destinationBankCode == null || destinationBankCode.isBlank()) {
				throw new IllegalArgumentException("destinationBankCode is required for external transfers");
			}
			if (gatewayProvider == null || gatewayProvider.isBlank()) {
				throw new IllegalArgumentException("gatewayProvider is required for external transfers");
			}
		}
	}

	/**
	 * PIN required for these types
	 */
	private boolean requiresPin(TransactionType type) {
		return switch (type) {
			case WITHDRAWAL,
				 CASH_WITHDRAWAL,
				 TRANSFER,
				 EMI_DEDUCTION,
				 PENALTY -> true;
			default -> false;
		};
	}

	/**
	 * Destination account required for these types
	 */
	private boolean requiresDestinationAccount(TransactionType type) {
		return switch (type) {
			case TRANSFER,
				 DEPOSIT,
				 LOAN_DISBURSEMENT,
				 REFUND -> true;
			default -> false;
		};
	}
}
