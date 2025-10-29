package com.bms.account.repositories.accountType;

import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.entities.AccountType;
import com.bms.account.entities.accountType.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount,Long> {
}
