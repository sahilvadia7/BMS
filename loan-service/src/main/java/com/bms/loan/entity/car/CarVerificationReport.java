package com.bms.loan.entity.car;


import com.bms.loan.entity.BaseVerificationReport;
import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CarVerificationReport extends BaseVerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private boolean insuranceValid;

    private boolean employmentVerified;
    private boolean carDocumentsVerified;
    private boolean physicalCarInspectionDone;  // officer saw the car

    private int carConditionScore;              // 1–10
    private int neighbourhoodStabilityScore;  // optional
    private int employmentStabilityYears; // how long employed (external data)

}
