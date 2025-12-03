package com.bms.transaction.repository;

import com.bms.transaction.enums.LedgerType;
import com.bms.transaction.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

	boolean existsByTransactionIdAndType(String txnId, LedgerType ledgerType);
}