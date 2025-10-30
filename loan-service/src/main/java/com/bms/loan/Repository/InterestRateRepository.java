package com.bms.loan.Repository;

import com.bms.loan.entity.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRateRepository extends JpaRepository<InterestRate, Long> {

    Optional<InterestRate> findByLoanType(String loanType);

}
