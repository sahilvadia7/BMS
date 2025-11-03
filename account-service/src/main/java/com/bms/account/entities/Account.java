package com.bms.account.entities;

import com.bms.account.constant.AccountStatus;
import com.bms.account.constant.IncomeSourceType;
import com.bms.account.constant.OccupationType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "occupation", length = 50)
    private OccupationType occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_of_income", length = 50)
    private IncomeSourceType sourceOfIncome;

    @Column(name = "gross_annual_income")
    private BigDecimal grossAnnualIncome;

    @Column(name = "nominee_name", length = 100)
    private String nomineeName;

    @Column(name = "nominee_relation", length = 50)
    private String nomineeRelation;

    @Column(name = "nominee_age")
    private Integer nomineeAge;

    @Column(name = "nominee_contact", length = 15)
    private String nomineeContact;

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
