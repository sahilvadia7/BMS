package com.bms.loan.entity;

import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.DocumentType;
import com.bms.loan.enums.KycStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which loan application this document belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loans loans;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String documentName;

    // file stored in byte in DB
    @Lob
    private byte[] documentData;

    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    private String remarks;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
