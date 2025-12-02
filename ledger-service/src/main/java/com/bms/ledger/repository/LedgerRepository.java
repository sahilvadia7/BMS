package com.bms.ledger.repository;

import com.bms.ledger.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

	Optional<LedgerEntry> findByTransactionIdAndAccountNumber(String transactionId, String accountNumber);

	List<LedgerEntry> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

	Optional<LedgerEntry> findTopByAccountNumberOrderByCreatedAtDesc(String accountNumber);
}