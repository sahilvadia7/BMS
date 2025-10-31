package com.bms.loan.entity.education;

import com.bms.loan.entity.loan.Loans;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(nullable = false, length = 100)
    private String courseName;

    @Column(nullable = false, length = 100)
    private String fieldOfStudy;

    @Column(nullable = false, length = 150)
    private String university;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private Integer courseDurationMonths;

    @Column(nullable = false)
    private LocalDate courseStartDate;

    @Column(nullable = false)
    private LocalDate expectedCompletionDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tuitionFees;

    @Column(precision = 15, scale = 2)
    private BigDecimal livingExpenses;

    @Column(precision = 15, scale = 2)
    private BigDecimal otherExpenses;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalCourseCost;

    @Column(nullable = false, length = 100)
    private String coApplicantName;

    @Column(nullable = false, length = 100)
    private String coApplicantRelation;

    @Column(nullable = false, length = 100)
    private String coApplicantOccupation;

    @Column(precision = 15, scale = 2)
    private BigDecimal coApplicantAnnualIncome;

    @Column(nullable = false)
    private Integer moratoriumMonths;

    @Column(nullable = false)
    private boolean isVerified = false;

}
