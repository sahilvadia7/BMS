package com.bms.transaction.service;

import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.model.Transaction;

import java.math.BigDecimal;


public interface InternalTransactionService {
	Object createTransaction(TransactionRequest request);
	void debitAccount(String account, BigDecimal amount, String txnId);
	void creditAccount(String account, BigDecimal amount, String txnId);
	void sendTransactionAlert(Transaction txn);
}
