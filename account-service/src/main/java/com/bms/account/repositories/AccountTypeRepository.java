package com.bms.account.repositories;

import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.entities.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType,Long> {
    Optional<AccountType> findByType(AccountTypeEnum type);

}
