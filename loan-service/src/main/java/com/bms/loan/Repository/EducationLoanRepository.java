package com.bms.loan.Repository;

import com.bms.loan.entity.education.EducationLoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationLoanRepository extends JpaRepository<EducationLoanDetails, Long> {
}
