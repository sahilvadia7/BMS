package com.bms.loan.Repository.education;

import com.bms.loan.entity.education.EducationVerificationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EducationVerificationReportRepository extends JpaRepository<EducationVerificationReport, Long> {

    Optional<EducationVerificationReport> findByLoans_LoanId(Long loanId);

}
