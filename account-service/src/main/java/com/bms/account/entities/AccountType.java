package com.bms.account.entities;

import com.bms.account.constant.AccountTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "account_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private AccountTypeEnum type; // e.g., SAVINGS, CURRENT, FIXED_DEPOSIT

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interest; // interest rate (e.g., 3.50)

    @Column(nullable = false)
    private Boolean active = true; // whether type is active
}
