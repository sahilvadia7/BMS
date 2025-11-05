package com.bms.transaction.repository;

import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean findByReferenceId(String referenceId);

    boolean existsByReferenceId(String s);

	List<Transaction> findByStatusIn(List<TransactionStatus> failed);

	List<Transaction> findByStatusAndNextRetryTimeBefore(TransactionStatus transactionStatus, LocalDateTime now);

	List<Transaction> findByAccountIdAndTransactionDateBetween(Long accountId, LocalDateTime weekStart, LocalDateTime now);
}
