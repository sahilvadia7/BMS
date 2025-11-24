package com.bms.loan.Repository;

import com.bms.loan.entity.loan.ClosedLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloseLoansRepository extends JpaRepository<ClosedLoan, Long> {
}
