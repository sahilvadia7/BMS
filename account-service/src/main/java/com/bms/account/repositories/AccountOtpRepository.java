package com.bms.account.repositories;

import com.bms.account.entities.AccountOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AccountOtpRepository extends JpaRepository<AccountOtp,Long> {
    Optional<AccountOtp> findByCifNumber(String cifNumber);
}
