package com.bms.account.entities.accountType;

import com.bms.account.entities.Account;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Represents a Current Account in the banking system.
 * This type of account is typically used by businesses and professionals
 * for frequent transactions. It supports overdraft and has monthly service charges.
 */
@Entity
@Table(name = "current_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CurrentAccount extends Account {

    /**
     * Business or organization name linked to this account.
     */
    @Column(nullable = false)
    private String businessName;

    /**
     * Overdraft limit allowed for this account.
     * The account can go below zero balance up to this amount.
     */
    @Column(nullable = false)
    private BigDecimal overdraftLimit;

    /**
     * Monthly service charge for maintaining this account.
     */
    @Column(nullable = false)
    private BigDecimal monthlyServiceCharge;

    /**
     * Whether a cheque book facility is available.
     */
    @Column(nullable = false)
    private Boolean chequeBookAvailable;

    /**
     * Whether overdraft facility is active.
     */
    @Column(nullable = false)
    private Boolean hasOverdraftFacility;

    /**
     * Automatically set default values before persisting.
     */
    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();

        if (businessName == null) businessName = "Default Business";
        if (overdraftLimit == null) overdraftLimit = new BigDecimal("50000.00"); // ₹50,000
        if (monthlyServiceCharge == null) monthlyServiceCharge = new BigDecimal("200.00"); // ₹200
        if (chequeBookAvailable == null) chequeBookAvailable = true;
        if (hasOverdraftFacility == null) hasOverdraftFacility = true;
    }

    /**
     * Check if a withdrawal is possible considering overdraft.
     */
    public boolean canWithdraw(BigDecimal amount) {
        if (amount == null || getBalance() == null) return false;
        BigDecimal remaining = getBalance().subtract(amount);
        // Allow negative balance up to -overdraftLimit if overdraft facility is active
        return hasOverdraftFacility
                ? remaining.compareTo(overdraftLimit.negate()) >= 0
                : remaining.compareTo(BigDecimal.ZERO) >= 0;
    }
}
