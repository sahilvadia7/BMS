package com.bms.loan.Repository;

import com.bms.loan.entity.loan.ActiveLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveLoanRepository extends JpaRepository<ActiveLoan, Long> {
}
