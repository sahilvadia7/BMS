package com.bms.transaction.service;

import com.bms.transaction.dto.request.CreateTransactionRequest;
import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.request.TransferRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;

import java.util.List;

public interface TransactionService {
    TransactionResponseDto createTransaction(CreateTransactionRequest request);
    TransactionResponseDto createTransfer(TransferRequest request);
    TransactionResponseDto getTransactionById(Long transactionId);
    List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request);
    TransactionSummaryDto getTransactionSummary(Long accountId, Long branchId);
}