package com.bms.loan.entity.home;

import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeLoanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private String propertyAddress;
    private BigDecimal propertyValue;
    private String builderName;
    private BigDecimal downPayment;
    private String propertyType; // FLAT, VILLA, PLOT
    private BigDecimal loanToValueRatio; // (calculated)
    private String ownershipType; // SELF_OWNED / JOINT
    private String registrationNumber; // property registration id
    private boolean approvedByAuthority;
}
