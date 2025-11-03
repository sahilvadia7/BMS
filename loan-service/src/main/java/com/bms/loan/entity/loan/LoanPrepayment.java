package com.bms.loan.entity.loan;


import com.bms.loan.enums.PrepaymentOption;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPrepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private BigDecimal prepaymentAmount;

    @Enumerated(EnumType.STRING)
    private PrepaymentOption prepaymentOption; // REDUCE_TENURE or REDUCE_EMI

    private LocalDate prepaymentDate;

    private BigDecimal newOutstandingPrincipal;
    private BigDecimal newEmi;
    private Integer newTenureMonths;

    private String remarks;
}
