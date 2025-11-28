package com.bms.transaction.dto.request;


import com.bms.transaction.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

	@NotBlank
	private String sourceAccount;

	@NotBlank
	private String destinationAccount;

	@NotBlank
	private String destinationBankCode;

	@NotNull
	@Positive
	private BigDecimal amount;

	@NotNull
	private Currency currency;

	@NotBlank
	private String transactionId;

	private String description;
}

