package com.bms.account.repositories.accountType;

import com.bms.account.entities.accountType.CurrentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentAccountRepository extends JpaRepository<CurrentAccount,Long> {
    boolean existsByCifNumber(String cifNumber);

}
