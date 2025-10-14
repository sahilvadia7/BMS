package com.bms.loan.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_application_id")
    private LoanApplication loanApplication;

    @ManyToOne
    @JoinColumn(name = "repayment_schedule_id")
    private RepaymentSchedule schedule;

    @Positive
    private BigDecimal amount;

    private LocalDateTime paymentDate = LocalDateTime.now();

    private String paymentMode;

    private String txnRef; // transaction reference
}
