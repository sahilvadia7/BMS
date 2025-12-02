package com.bms.transaction.service;

import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    List<TransactionResponseDto> getLatestMonthTransactions(String accountNumber);
    List<TransactionResponseDto> getTransactionsByStatus(TransactionStatus status);
    TransactionResponseDto getTransactionById(Long transactionId);
    List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request);
    List<Map<String, Object>> getDailyTransactionSummary();
    Map<String, BigDecimal> getAccountSummary(String accountNumber);
    Map<String, Object> getTransactionCharges(String transactionId);
    String sendStatement(String accountNumber) throws Exception;
}