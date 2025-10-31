package com.bms.account.entities;

import com.bms.account.constant.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 12)
    private String accountNumber;

    @Column(nullable = false, length = 20)
    private String cifNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_type_id", nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, length = 4)
    private Integer accountPin; //  PIN saved in DB

    @Column(name = "kyc_id")
    private Long kycId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        if (accountNumber == null || accountNumber.isEmpty()) {
            String ts = String.valueOf(System.currentTimeMillis());
            accountNumber = "AC" + ts.substring(ts.length() - 10);
        }
        if (status == null) {
            status = AccountStatus.PENDING;
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (accountPin == null) {
            accountPin = (int) (Math.random() * 9000) + 1000; //  generate 4-digit PIN automatically
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
