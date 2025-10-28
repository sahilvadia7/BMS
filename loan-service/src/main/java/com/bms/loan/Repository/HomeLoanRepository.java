package com.bms.loan.Repository;

import com.bms.loan.entity.home.HomeLoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HomeLoanRepository extends JpaRepository<HomeLoanDetails, Long> {
    Optional<HomeLoanDetails> findByLoans_LoanId(Long loanId);
}
