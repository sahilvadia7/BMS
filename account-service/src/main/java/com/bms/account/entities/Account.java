package com.bms.account.entities;

import com.bms.account.enums.AccountStatus;
import com.bms.account.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String accountNumber; // System-generated

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType; // SAVINGS, CURRENT

    @Column(nullable = false)
    private BigDecimal balance;

//    private String currency; // Future  Optional

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status; // ACTIVE, CLOSED, FROZEN

    @Column(nullable = false)
    private Long customerId; // Foreign key to Customer entity

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // -------------------------------
    // Lifecycle hooks for timestamps and default values
    // -------------------------------

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AccountStatus.ACTIVE; // default status
        }
        if (accountNumber == null || accountNumber.isEmpty()) {
            accountNumber = generateAccountNumber(); // auto-generate
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------------
    // Account number generator
    // -------------------------------
    private String generateAccountNumber() {
        // Example: ACCT + timestamp
        return "ACCT" + System.currentTimeMillis();
    }
}

