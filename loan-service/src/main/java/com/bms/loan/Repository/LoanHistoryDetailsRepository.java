package com.bms.loan.Repository;

import com.bms.loan.entity.loan.LoanHistoryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanHistoryDetailsRepository extends JpaRepository<LoanHistoryDetails, Long> {
}
