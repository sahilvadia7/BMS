package com.bms.loan.Repository;

import com.bms.loan.entity.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRateRepository extends JpaRepository<InterestRate, Long> {

    InterestRate findByLoanType(String loanType);

}
