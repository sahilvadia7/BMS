package com.bms.customer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // FK to User (centralized DB)

//    @Column(nullable = false)
//    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private Long kycId; // FK to KYC entity/table

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (kycId == null) {
            kycId = 0L; // default value if you want
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
