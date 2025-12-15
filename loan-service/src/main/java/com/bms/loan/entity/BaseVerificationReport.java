package com.bms.loan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseVerificationReport {

    private String officerName;
    private String officerRemarks;

    private LocalDate visitDate;

    private boolean addressVerified;        // common verification item
    private boolean identityVerified;       // KYC cross-check
    private boolean verifiedSuccessfully;   // final status
}
