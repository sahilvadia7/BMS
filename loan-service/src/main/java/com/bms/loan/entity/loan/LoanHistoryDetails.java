package com.bms.loan.entity.loan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanHistoryDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean haveExistingLoans;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalOutstandingAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalMonthlyEmi;

    private int totalClosedLoans;
    private int totalActiveLoans;

    // One LoanHistory belongs to one LoanApplication
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonIgnore
    private Loans loans;

    // Child lists
    @JsonManagedReference(value = "active-loans")
    @OneToMany(mappedBy = "loanHistoryDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActiveLoan> activeLoans = new ArrayList<>();

    @JsonManagedReference(value = "closed-loans")
    @OneToMany(mappedBy = "loanHistoryDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClosedLoan> closedLoans = new ArrayList<>();
}
