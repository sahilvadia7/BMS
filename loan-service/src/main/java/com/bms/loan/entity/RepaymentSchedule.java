package com.bms.loan.entity;

import com.bms.loan.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_application_id")
    @JsonBackReference
    private LoanApplication loanApplication;

    private LocalDate dueDate;

    @Positive
    private BigDecimal principalDue;

    @Positive
    private BigDecimal interestDue;

    @Positive
    private BigDecimal emi;

    @PositiveOrZero
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Repayment> repayments;
}
