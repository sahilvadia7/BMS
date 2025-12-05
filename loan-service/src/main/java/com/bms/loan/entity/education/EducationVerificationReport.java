package com.bms.loan.entity.education;

import com.bms.loan.entity.BaseVerificationReport;
import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EducationVerificationReport extends BaseVerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private boolean admissionVerified;
    private boolean collegeRecognized;       // true if institute approved
    private boolean feeStructureVerified;    // true if fee structure validated with university
    private boolean studentBackgroundClear;     // background validation

    private boolean coApplicantIncomeVerified;  // salary slips or ITR
    private boolean coApplicantIdentityValid;   // PAN, Aadhaar KYC match
}
