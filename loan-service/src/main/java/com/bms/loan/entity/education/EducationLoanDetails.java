package com.bms.loan.entity.education;

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
public class EducationLoanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loans loans;

    private String courseName;
    private String university;
    private Integer courseDurationMonths;
    private BigDecimal tuitionFees;
    private String coApplicantName;

}
