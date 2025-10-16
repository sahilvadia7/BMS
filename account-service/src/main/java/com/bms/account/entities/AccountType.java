package com.bms.account.entities;
import com.bms.account.constant.AccountTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "account_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AccountTypeEnum type; // e.g., SAVINGS, CURRENT

    @Column(nullable = false)
    private BigDecimal interest; // interest rate

    @Column(nullable = false)
    private Boolean active = true; // true if the account type is active

    // One AccountType can have many Accounts
    @OneToMany(mappedBy = "accountType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

}
