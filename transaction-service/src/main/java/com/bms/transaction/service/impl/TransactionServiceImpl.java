package com.bms.transaction.service.impl;

import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.request.TransferRequest;
import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.Channel;
import com.bms.transaction.enums.Currency;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.exception.ResourceNotFoundException;
import com.bms.transaction.feing.AccountClient;
import com.bms.transaction.feing.BranchClient;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountClient accountClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
    }

    private static final Map<TransactionType, BigDecimal> WEEKLY_LIMITS = Map.of(
            TransactionType.DEPOSIT, new BigDecimal("50000"),
            TransactionType.WITHDRAWAL, new BigDecimal("20000"),
            TransactionType.TRANSFER, new BigDecimal("20000"),
            TransactionType.LOAN_DISBURSEMENT, new BigDecimal("100000"),
            TransactionType.EMI_DEDUCTION, new BigDecimal("50000"),
            TransactionType.REFUND, new BigDecimal("100000")
    );

    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequest request) {

        validateAccount(request.accountId());

        TransactionType type = validateTransactionType(request.transactionType());
        if (type == TransactionType.TRANSFER &&
                Objects.equals(request.accountId(), request.destinationAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        validateTransactionLimits(request.accountId(), type, request.amount());

        Transaction txn = createPendingTransaction(request, type);

        try {
            executeTransactionSteps(type, request);

            txn.setStatus(TransactionStatus.COMPLETED);
            txn.setRemarks("Transaction successful");
            transactionRepository.save(txn);

        } catch (Exception e) {
            log.error("Transaction failed, starting compensation: {}", e.getMessage());

            txn.setStatus(TransactionStatus.FAILED);
            txn.setRemarks(e.getMessage());
            transactionRepository.save(txn);

            handleCompensation(type, request, txn);

            throw new IllegalStateException("Transaction failed: " + e.getMessage(), e);
        }

        return mapToResponseDto(txn);
    }


    private Transaction createPendingTransaction(TransactionRequest request, TransactionType type) {
        Transaction txn = new Transaction();
        txn.setAccountId(request.accountId());
        txn.setDestinationAccountId(request.destinationAccountId());
        txn.setTransactionType(type);
        txn.setAmount(request.amount());
        txn.setCurrency(Currency.valueOf(request.currency().toUpperCase()));
        txn.setStatus(TransactionStatus.PENDING);
        txn.setDescription(request.description());
        txn.setReferenceId(UUID.randomUUID().toString());
        txn.setChannel(Channel.valueOf(request.channel().toUpperCase()));
        txn.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(txn);
    }


    private void executeTransactionSteps(TransactionType type, TransactionRequest request) {
        switch (type) {
            case WITHDRAWAL, EMI_DEDUCTION -> {
                accountClient.updateBalance(request.accountId(), request.amount(), "WITHDRAW");
            }

            case DEPOSIT, LOAN_DISBURSEMENT, REFUND -> {
                if (request.destinationAccountId() != null) {
                    validateAccount(request.destinationAccountId());
                    accountClient.updateBalance(request.destinationAccountId(), request.amount(), "DEPOSIT");
                }
            }

            case TRANSFER -> {
                accountClient.updateBalance(request.accountId(), request.amount(), "WITHDRAW");

                if (request.destinationAccountId() != null) {
                    validateAccount(request.destinationAccountId());
                    accountClient.updateBalance(request.destinationAccountId(), request.amount(), "DEPOSIT");
                }
            }

            default -> throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCompensation(TransactionType type, TransactionRequest request, Transaction txn) {
        try {
            switch (type) {
                case TRANSFER, WITHDRAWAL, EMI_DEDUCTION -> {
                    log.warn("Compensating failed {} for account {}", type, request.accountId());
                    accountClient.updateBalance(request.accountId(), request.amount(), "DEPOSIT");
                }
                case DEPOSIT, LOAN_DISBURSEMENT, REFUND -> {
                    if (request.destinationAccountId() != null) {
                        log.warn("Reverting {} to destination {}", type, request.destinationAccountId());
                        accountClient.updateBalance(request.destinationAccountId(), request.amount(), "WITHDRAW");
                    }
                }
            }

            txn.setRemarks("Compensation executed successfully");
            transactionRepository.save(txn);

        } catch (Exception rollbackEx) {
            log.error("Compensation failed: {}", rollbackEx.getMessage());
            txn.setRemarks("Compensation failed: " + rollbackEx.getMessage());
            transactionRepository.save(txn);
        }
    }


    private void validateTransactionLimits(Long accountId, TransactionType type, BigDecimal newAmount) {
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<Transaction> weeklyTxns = transactionRepository.findByAccountIdAndTransactionDateBetween(
                accountId, weekStart, now
        );

        BigDecimal total = weeklyTxns.stream()
                .filter(txn -> txn.getTransactionType() == type && txn.getStatus() == TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limit = WEEKLY_LIMITS.getOrDefault(type, BigDecimal.valueOf(Long.MAX_VALUE));

        if (total.add(newAmount).compareTo(limit) > 0) {
            throw new IllegalStateException("Weekly " + type + " limit exceeded. Allowed: " + limit);
        }
    }


    @Transactional
    public TransactionResponseDto transfer(TransferRequest request) {

        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("From and to accounts must be different");
        }
        validateAccount(request.fromAccountId());
        validateAccount(request.toAccountId());

        BigDecimal balance = accountClient.getAccountBalance(request.fromAccountId());
        if (balance.compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds in from account");
        }


        Transaction transaction = new Transaction();
        transaction.setAccountId(request.fromAccountId());
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
        return List.of();
    }


    public TransactionSummaryDto getTransactionSummary(Long accountId, Long branchId) {
        validateAccount(accountId);
        return new TransactionSummaryDto(accountId, branchId, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }


    private void validateAccount(Long accountId) {
        if (!accountClient.getAccountById(accountId)) {
            throw new ResourceNotFoundException("Account not found with ID: " + accountId);
        }
    }


    private void validatePin(Long accountId) {
        if (!accountClient.getAccountById(accountId)) {
            throw new ResourceNotFoundException("Account not found with ID: " + accountId);
        }
    }


    private TransactionType validateTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
    }


    private String generateReferenceId() {
        return "TXN-" + System.currentTimeMillis();
    }


    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getCurrency().name(),
                transaction.getChannel().name(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getReferenceId()
        );
    }
}
