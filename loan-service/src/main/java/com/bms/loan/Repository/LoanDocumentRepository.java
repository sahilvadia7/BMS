package com.bms.loan.Repository;

import com.bms.loan.entity.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Integer> {

    List<LoanDocument> findByLoans_LoanId(Long loanId);
}
