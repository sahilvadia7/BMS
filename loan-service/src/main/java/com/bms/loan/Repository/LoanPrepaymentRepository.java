package com.bms.loan.Repository;

import com.bms.loan.entity.loan.LoanPrepayment;
import com.bms.loan.entity.loan.Loans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanPrepaymentRepository extends JpaRepository<LoanPrepayment, Long> {
    List<LoanPrepayment> findByLoansOrderByPrepaymentDateAsc(Loans loans);

}
