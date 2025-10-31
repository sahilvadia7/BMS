package com.bms.loan.entity.education;

import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationVerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private boolean admissionVerified;       // true if admission letter verified
    private boolean collegeRecognized;       // true if institute approved
    private boolean feeStructureVerified;    // true if fee structure validated with university
    private String officerName;
    private String officerRemarks;
    private LocalDate verificationDate;
    private boolean verifiedSuccessfully;

}
