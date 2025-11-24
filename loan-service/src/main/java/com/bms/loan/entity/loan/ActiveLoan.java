package com.bms.loan.entity.loan;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loanType;

    @Column(precision = 18, scale = 2)
    private BigDecimal loanAmount;

    private int tenureMonths;

    @Column(precision = 18, scale = 2)
    private BigDecimal remainingAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal emiAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private String bankOrLenderName;

    private int totalEmis;
    private int timelyPaidEmis;
    private int lateOrMissedEmis;

    @JsonBackReference(value = "active-loans")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_history_details_id")
    private LoanHistoryDetails loanHistoryDetails;
}
