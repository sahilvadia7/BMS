package com.bms.transaction.service;

import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    TransactionResponseDto createTransaction(TransactionRequest request);
    List<TransactionResponseDto> getTransactionsForMonth(String accountNumber, int year, int month);
    List<TransactionResponseDto> getTransactionsByStatus(TransactionStatus status);
    TransactionResponseDto getTransactionById(Long transactionId);
    List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request);
    void handleCompensation(TransactionType type, TransactionRequest request, Transaction txn);
    List<Map<String, Object>> getDailyTransactionSummary();
    Map<String, BigDecimal> getAccountSummary(String accountNumber);
    Map<String, Object> getTransactionCharges(String transactionId);
    String sendStatement(String accountNumber) throws Exception;
}