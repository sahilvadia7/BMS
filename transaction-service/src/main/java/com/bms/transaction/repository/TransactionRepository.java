package com.bms.transaction.repository;

import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByStatusAndNextRetryTimeBefore(TransactionStatus transactionStatus, LocalDateTime now);

	List<Transaction> findByAccountNumberAndTransactionDateBetween(String accountNumber, LocalDateTime weekStart, LocalDateTime now);

	List<Transaction> findByStatus(TransactionStatus status);

	@Query("""
    SELECT DATE(t.transactionDate) AS date,
           t.transactionType AS type,
           SUM(t.amount) AS totalAmount
    FROM Transaction t
    WHERE t.status = 'SUCCESS'
    GROUP BY DATE(t.transactionDate), t.transactionType
    ORDER BY DATE(t.transactionDate) DESC
""")
	List<Object[]> getDailySummary();

	@Query("""
    SELECT t.transactionType, SUM(t.amount)
    FROM Transaction t
    WHERE t.accountNumber = :accountNumber
    GROUP BY t.transactionType
""")
	List<Object[]> getAccountSummary(@Param("accountNumber") String accountNumber);

	Optional<Transaction> findByTransactionId(String transactionId);

	List<Transaction> findByAccountNumber(String accountNumber);

}
