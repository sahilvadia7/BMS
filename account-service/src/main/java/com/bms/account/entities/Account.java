
package com.bms.account.entities;

import com.bms.account.constant.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts"
//        indexes = {
//                @Index(name = "idx_account_number", columnList = "accountNumber"),
//                @Index(name = "idx_account_cif", columnList = "cifNumber"),
//                @Index(name = "idx_account_branch", columnList = "branchId")
//        },
//        uniqueConstraints = {
//                @UniqueConstraint(name = "uk_account_number", columnNames = "accountNumber")
//        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique per account
    @Column(unique = true, nullable = false, length = 12)
    private String accountNumber;

    // CIF links the account to a single customer (cross-service reference)
    @Column(nullable = false, length = 20)
    private String cifNumber;

    @ManyToOne(fetch = FetchType.EAGER)  // or LAZY
    @JoinColumn(name = "account_type_id", nullable = false)
    private AccountType accountType;


    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;  // ACTIVE, CLOSED, FROZEN

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long branchId; // FK to Branch (central DB)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        // Auto-generate account number pattern: e.g. "AC202510160001"
        if (accountNumber == null || accountNumber.isEmpty()) {
            String ts = String.valueOf(System.currentTimeMillis());
            accountNumber = "AC" + ts.substring(ts.length() - 10); // 2 + 10 = 12 chars
        }

        if (status == null) {
            status = AccountStatus.PENDING;
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}