package com.bms.transaction.repository;

import com.bms.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean findByReferenceId(String referenceId);
}
