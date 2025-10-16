package com.bms.customer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_kyc_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerKycMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kyc_id", nullable = false, unique = true)
    private Kyc kyc;

    @Column(nullable = false)
    private boolean isPrimary = false;

    private LocalDate verificationDate;

    @PrePersist
    protected void onCreate() {
        if (this.verificationDate == null) {
            this.verificationDate = LocalDate.now();
        }
    }
}