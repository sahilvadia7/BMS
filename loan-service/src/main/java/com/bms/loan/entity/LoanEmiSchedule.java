package com.bms.loan.entity;

import com.bms.loan.enums.EmiStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEmiSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loan;

    private int installmentNumber;
    private LocalDate dueDate;
    private BigDecimal emiAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;

    @Enumerated(EnumType.STRING)
    private EmiStatus status;

    private LocalDate paymentDate;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal lateFee = BigDecimal.ZERO;

    private Integer daysLate = 0;
    private String remarks;

}
