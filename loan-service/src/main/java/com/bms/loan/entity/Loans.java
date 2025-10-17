package com.bms.loan.entity;

import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.LoanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    private Long customerId;

    private String cifNumber;


    @Enumerated(EnumType.STRING)
    private LoanType loanType; // CAR, HOME, EDUCATION, PERSONAL, BUSINESS

    private BigDecimal interestRate;

    private BigDecimal requestedAmount; // amount customer applied for

    private BigDecimal ApprovedAmount;

    private Integer requestedTenureMonths; // tenure in months

    private BigDecimal totalAmountPaid; // amount paid

    private String bankName;

    private String bankAccount;

    private String ifscCode;

    @Enumerated(EnumType.STRING)
    private LoanStatus status; // APPLIED, DOCUMENTS_SUBMITTED, EVALUATED, APPROVED, REJECTED, SANCTIONED, DISBURSED

    private String remarks; // any notes or reason for rejection

    @CreationTimestamp
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Integer version; // for optimistic locking


    // External customer fields
    @Builder.Default
    private Integer externalCreditScore = 700; // default CIBIL for testing

    @Builder.Default
    @Column(name = "external_blacklisted")
    private boolean externalBlacklisted = false; // default not blacklisted

    @Builder.Default
    private BigDecimal externalMonthlyIncome = BigDecimal.valueOf(50000); // default income

    @Builder.Default
    private BigDecimal externalTotalExistingEmi = BigDecimal.ZERO; // default EMI

    private LocalDate DisbursementDate;

}
