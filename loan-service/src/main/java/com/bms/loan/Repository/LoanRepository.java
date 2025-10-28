package com.bms.loan.Repository;

import com.bms.loan.entity.loan.Loans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loans, Long> {
    Optional<Loans> findByLoanId(Long loanApplicationId);
}
