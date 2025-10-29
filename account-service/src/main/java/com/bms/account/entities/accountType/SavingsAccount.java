package com.bms.account.entities.accountType;

import com.bms.account.entities.Account;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "savings_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class SavingsAccount extends Account {

    @Column(nullable = false)
    private BigDecimal minimumBalance;

    @Column(nullable = false)
    private Integer withdrawalLimitPerMonth;

    @Column(nullable = false)
    private Boolean chequeBookAvailable;

    @Column(nullable = false)
    private BigDecimal interestRate;

    public boolean isBelowMinimum() {
        if (getBalance() == null || minimumBalance == null) return false;
        return getBalance().compareTo(minimumBalance) < 0;
    }

    public boolean canWithdraw(BigDecimal amount) {
        if (amount == null || getBalance() == null) return false;
        BigDecimal remaining = getBalance().subtract(amount);
        return remaining.compareTo(minimumBalance) >= 0;
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate(); // ensures Account base fields (timestamps, number, status) are set
        if (minimumBalance == null) minimumBalance = new BigDecimal("10000.00");
        if (withdrawalLimitPerMonth == null) withdrawalLimitPerMonth = 5;
        if (chequeBookAvailable == null) chequeBookAvailable = true;
        if (interestRate == null) interestRate = new BigDecimal("3.50");
    }
}
