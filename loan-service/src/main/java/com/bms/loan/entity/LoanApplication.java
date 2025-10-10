package com.bms.loan.entity;

import com.bms.loan.enums.LoanProductCode;
import com.bms.loan.enums.LoanStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    @NotNull
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    private LoanProductCode productCode; // HOME, PERSONAL, CAR

    @Positive
    private BigDecimal principal;

    @Positive
    private Integer tenureMonths;

    @Positive
    private BigDecimal annualRate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.APPLIED;

    private Integer creditScore;

    private BigDecimal outstandingBalance;

    private LocalDateTime appliedAt = LocalDateTime.now();
    private LocalDateTime approvedAt;
    private LocalDateTime disbursedAt;

    private String approvedBy;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<RepaymentSchedule> repaymentSchedules;
}
