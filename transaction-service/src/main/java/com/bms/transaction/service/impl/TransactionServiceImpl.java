package com.bms.transaction.service.impl;

import com.bms.transaction.advice.customexception.ResourceNotFoundException;
import com.bms.transaction.dto.request.CreateTransactionRequest;
import com.bms.transaction.dto.request.TransferRequest;
import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.feing.AccountClient;
import com.bms.transaction.feing.BranchClient;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final BranchClient branchClient;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountClient accountClient,
            BranchClient branchClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.branchClient = branchClient;
    }

    @Transactional
    public TransactionResponseDto createTransaction(CreateTransactionRequest request) {

        validateAccount(request.accountId());
        validateBranch(request.branchId());
        validateTransactionType(request.transactionType());

        if (request.referenceId() != null && transactionRepository.findByReferenceId((request.referenceId()))){
            throw new IllegalStateException("Duplicate transaction reference ID: " + request.referenceId());
        }

        if ("WITHDRAWAL".equalsIgnoreCase(request.transactionType())) {
            BigDecimal balance = accountClient.getAccountBalance(request.accountId());
            if (balance.compareTo(request.amount()) < 0) {
                throw new IllegalStateException("Insufficient funds");
            }
        }

        Transaction transaction = new Transaction();
        transaction.setAccountId(request.accountId());
        transaction.setBranchId(request.branchId());
        transaction.setTransactionType(TransactionType.valueOf(request.transactionType().toUpperCase()));
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(request.description());
        transaction.setReferenceId(request.referenceId());

        transaction = transactionRepository.save(transaction);

        accountClient.updateBalance(request.accountId(), request.amount(), request.transactionType());

        return mapToResponseDto(transaction);
    }

    @Transactional
    public TransactionResponseDto createTransfer(TransferRequest request) {

        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("From and to accounts must be different");
        }
        validateAccount(request.fromAccountId());
        validateAccount(request.toAccountId());
        validateBranch(request.branchId());


        BigDecimal balance = accountClient.getAccountBalance(request.fromAccountId());
        if (balance.compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds in from account");
        }


        Transaction transaction = new Transaction();
        transaction.setAccountId(request.fromAccountId());
        transaction.setBranchId(request.branchId());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(request.description());
        transaction.setReferenceId(generateReferenceId());

        transaction = transactionRepository.save(transaction);


        accountClient.updateBalance(request.fromAccountId(), request.amount().negate(), "WITHDRAWAL");
        accountClient.updateBalance(request.toAccountId(), request.amount(), "DEPOSIT");

        return mapToResponseDto(transaction);
    }

    public TransactionResponseDto getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        return mapToResponseDto(transaction);
    }

    public List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request) {
        // Implement dynamic query using Specification or Criteria API
        // Example omitted for brevity
        return List.of(); // Placeholder
    }

    public TransactionSummaryDto getTransactionSummary(Long accountId, Long branchId) {
        validateAccount(accountId);
        validateBranch(branchId);
        // Implement summary logic
        return new TransactionSummaryDto(accountId, branchId, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private void validateAccount(Long accountId) {
        if (!accountClient.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found with ID: " + accountId);
        }
    }

    private void validateBranch(Long branchId) {
        if (!branchClient.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found with ID: " + branchId);
        }
    }

    private void validateTransactionType(String type) {
        try {
            TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
    }

    private String generateReferenceId() {
        return "TXN-" + System.currentTimeMillis();
    }

    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getBranchId(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getReferenceId()
        );
    }
}
