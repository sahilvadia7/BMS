package com.bms.gateway.dto.request;


import com.bms.gateway.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

	@NotBlank(message = "Source account is required")
	@Size(min = 10, max = 20, message = "Source account must be 10–20 digits")
	private String sourceAccount;

	@NotBlank(message = "Destination account is required")
	@Size(min = 10, max = 20, message = "Destination account must be 10–20 digits")
	private String destinationAccount;

	@NotBlank(message = "Destination bank code is required")
	@Pattern(regexp = "^[A-Z0-9]{4,11}$", message = "Bank code must be SWIFT/IFSC format")
	private String destinationBankCode;

	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be greater than 0")
	private BigDecimal amount;

	@NotNull(message = "Currency is required")
	private Currency currency;

	@NotBlank(message = "Transaction ID is missing")
	private String transactionId;

	@Size(max = 255, message = "Description max length is 255 characters")
	private String description;
}
