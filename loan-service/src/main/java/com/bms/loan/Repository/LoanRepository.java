package com.bms.loan.Repository;

import com.bms.loan.entity.Loans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loans, Integer> {
    Optional<Loans> findByLoanId(Long loanApplicationId);
}
