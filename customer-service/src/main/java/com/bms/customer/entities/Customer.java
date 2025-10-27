package com.bms.customer.entities;

import com.bms.customer.enums.Gender;
import com.bms.customer.enums.Roles;
import com.bms.customer.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_cif_number", columnList = "cifNumber"),
                @Index(name = "idx_customer_email", columnList = "email"),
                @Index(name = "idx_customer_phone", columnList = "phoneNo")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customer_cif_number", columnNames = "cifNumber")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 10)
    private String phoneNo;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 20)
    private String cifNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDate dob;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CustomerKycMapping> kycDocuments = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        if (this.status == null) {
            this.status = UserStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}