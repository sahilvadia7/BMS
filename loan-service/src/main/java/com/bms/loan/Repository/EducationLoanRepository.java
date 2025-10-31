package com.bms.loan.Repository;

import com.bms.loan.entity.education.EducationLoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EducationLoanRepository extends JpaRepository<EducationLoanDetails, Long> {

    Optional<EducationLoanDetails> findByLoans_LoanId(Long aLong);
}
