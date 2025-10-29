package com.bms.loan.Repository.home;

import com.bms.loan.entity.home.HomeLoanSanction;
import com.bms.loan.entity.loan.Loans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeLoanSanctionRepository extends JpaRepository<HomeLoanSanction, Long> {

    Optional<HomeLoanSanction> findByLoans_LoanId(Long loanId);
}
