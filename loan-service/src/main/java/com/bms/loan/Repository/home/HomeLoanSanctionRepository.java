package com.bms.loan.Repository.home;

import com.bms.loan.entity.home.LoanSanction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeLoanSanctionRepository extends JpaRepository<LoanSanction, Long> {

    Optional<LoanSanction> findByLoans_LoanId(Long loanId);
}
