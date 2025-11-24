package com.bms.loan.entity.loan;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosedLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loanType;

    @Column(precision = 18, scale = 2)
    private BigDecimal loanAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private String bankOrLenderName;

    private boolean closedOnTime;

    private String closureReason;

    @JsonBackReference(value = "closed-loans")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_history_details_id")
    private LoanHistoryDetails loanHistoryDetails;

}
