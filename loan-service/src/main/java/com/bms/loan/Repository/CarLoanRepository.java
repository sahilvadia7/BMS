package com.bms.loan.Repository;

import com.bms.loan.entity.CarLoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarLoanRepository extends JpaRepository<CarLoanDetails , Long> {

    Optional<CarLoanDetails> findByLoans_LoanId(Long loanId);
}
