package com.bms.loan.entity.home;

import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeVerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private boolean addressVerified;
    private boolean ownershipVerified;
    private boolean propertyConditionOk;

    private BigDecimal evaluatedValue; // as per field inspection
    private String officerName;
    private String officerRemarks;
    private LocalDate visitDate;

}
