package com.bms.loan.Repository.home;

import com.bms.loan.entity.home.HomeVerificationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeVerificationReportRepository extends JpaRepository<HomeVerificationReport, Long> {
    Optional<HomeVerificationReport> findByLoans_LoanId(Long loanId);

}
