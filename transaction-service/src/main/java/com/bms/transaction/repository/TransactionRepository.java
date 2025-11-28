package com.bms.transaction.repository;

import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.model.Transaction;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

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

	@Query("""
        SELECT COALESCE(SUM(t.amount), 0) 
        FROM Transaction t 
        WHERE t.accountNumber = :accountNumber
          AND t.transactionType = :transactionType
          AND t.transactionDate >= :weekStart
          AND t.transactionDate <= :weekEnd
        """)
	Optional<BigDecimal> sumWeeklyAmount(
			@Param("accountNumber") String accountNumber,
			@Param("transactionType") TransactionType transactionType,
			@Param("weekStart") LocalDateTime weekStart,
			@Param("weekEnd") LocalDateTime weekEnd
	);

	Optional<Transaction> findByIdempotencyKey(@NotBlank(message = "idempotencyKey is required") String idempotencyKey);

	boolean existsByLinkedTransactionId(String transactionId);
}
