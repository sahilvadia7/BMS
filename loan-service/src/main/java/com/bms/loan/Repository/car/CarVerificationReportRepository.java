package com.bms.loan.Repository.car;

import com.bms.loan.entity.car.CarVerificationReport;
import com.bms.loan.entity.home.LoanSanction;
import com.bms.loan.entity.loan.ClosedLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarVerificationReportRepository extends JpaRepository<CarVerificationReport , Long> {
    Optional<CarVerificationReport> findByLoans_LoanId(Long loanId);

}
