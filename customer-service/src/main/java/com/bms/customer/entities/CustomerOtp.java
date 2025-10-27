package com.bms.customer.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "customer_otp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private String otp;

    private LocalDateTime expiresAt;

    private boolean verified;
    
    private boolean used;

    private int attempts;

    private LocalDateTime createdAt;
}
