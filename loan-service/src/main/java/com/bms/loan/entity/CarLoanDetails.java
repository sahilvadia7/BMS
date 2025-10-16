package com.bms.loan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class CarLoanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonIgnore
    private Loans loans;

    private String carModel;
    private String manufacturer;
    private int manufactureYear;
    private BigDecimal carValue;
    private String registrationNumber;
    private int carAgeYears; // how old the car is
    private int carConditionScore; // e.g. 1–10 from evaluation team
    private BigDecimal downPayment;
    private boolean insuranceValid;
    private int employmentStabilityYears; // how long employed (external data)

}
