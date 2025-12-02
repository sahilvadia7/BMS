package com.bms.ledger.model;

import com.bms.ledger.enums.LedgerType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String transactionId;

	@Column(nullable = false)
	private String accountNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LedgerType type;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private BigDecimal balanceAfter;

	@Column(nullable = false)
	private String transactionType;

	private String description;

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
