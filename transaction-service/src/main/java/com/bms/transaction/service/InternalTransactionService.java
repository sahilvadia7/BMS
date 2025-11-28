package com.bms.transaction.service;

import com.bms.transaction.dto.request.TransactionRequest;


public interface InternalTransactionService {
	Object createTransaction(TransactionRequest request);
}
