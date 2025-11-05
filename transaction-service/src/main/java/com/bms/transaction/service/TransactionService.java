package com.bms.transaction.service;

import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.request.TransferRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.model.Transaction;

import java.util.List;

public interface TransactionService {
    TransactionResponseDto createTransaction(TransactionRequest request);
    TransactionResponseDto transfer(TransferRequest request);
    TransactionResponseDto getTransactionById(Long transactionId);
    List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request);
    TransactionSummaryDto getTransactionSummary(Long accountId, Long branchId);
    void handleCompensation(TransactionType type, TransactionRequest request, Transaction txn);

}