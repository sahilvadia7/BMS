package com.bms.loan.entity.home;

import com.bms.loan.entity.BaseVerificationReport;
import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HomeVerificationReport extends BaseVerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private boolean ownershipVerified;
    private boolean neighbourCheckDone;      // background check
    private boolean propertyConditionOk;
    private BigDecimal evaluatedValue; // as per field inspection

    private String propertyType;
    private BigDecimal propertyArea;

}
