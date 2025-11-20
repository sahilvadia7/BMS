package com.bms.transaction.service.impl;

import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.Channel;
import com.bms.transaction.enums.Currency;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.exception.ResourceNotFoundException;
import com.bms.transaction.feing.AccountClient;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.TransactionService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static java.awt.Color.LIGHT_GRAY;

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

        validateAccount(request.accountNumber());

        TransactionType type = validateTransactionType(request.transactionType());
        if (type == TransactionType.TRANSFER &&
                Objects.equals(request.accountNumber(), request.destinationAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        boolean isValidPin = verifyPinWithAccountService(String.valueOf(request.accountNumber()), Integer.parseInt(request.pin()));
        if (!isValidPin) {
            throw new IllegalArgumentException("Invalid account PIN");
        }

        validateTransactionLimits(request.accountNumber(), type, request.amount());

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
        txn.setAccountNumber(request.accountNumber());
        txn.setDestinationAccountNumber(request.destinationAccountNumber());
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
                accountClient.updateBalance(request.accountNumber(), request.amount(), "WITHDRAW");
            }

            case DEPOSIT, LOAN_DISBURSEMENT, REFUND -> {
                if (request.destinationAccountNumber() != null) {
                    validateAccount(request.destinationAccountNumber());
                    accountClient.updateBalance(request.destinationAccountNumber(), request.amount(), "DEPOSIT");
                }
            }

            case TRANSFER -> {
                accountClient.updateBalance(request.accountNumber(), request.amount(), "WITHDRAW");

                if (request.destinationAccountNumber() != null) {
                    validateAccount(request.destinationAccountNumber());
                    accountClient.updateBalance(request.destinationAccountNumber(), request.amount(), "DEPOSIT");
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
                    log.warn("Compensating failed {} for account {}", type, request.accountNumber());
                    accountClient.updateBalance(request.accountNumber(), request.amount(), "DEPOSIT");
                }
                case DEPOSIT, LOAN_DISBURSEMENT, REFUND -> {
                    if (request.destinationAccountNumber() != null) {
                        log.warn("Reverting {} to destination {}", type, request.destinationAccountNumber());
                        accountClient.updateBalance(request.destinationAccountNumber(), request.amount(), "WITHDRAW");
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


    private void validateTransactionLimits(String accountNumber, TransactionType type, BigDecimal newAmount) {
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<Transaction> weeklyTxns = transactionRepository.findByAccountNumberAndTransactionDateBetween(
                accountNumber, weekStart, now
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

    public TransactionResponseDto getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        return mapToResponseDto(transaction);
    }


    public List<TransactionResponseDto> getTransactionsForMonth(String accountNumber, int year, int month) {
        LocalDateTime startDate = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endDate = startDate.plusMonths(1).minusNanos(1);

        List<Transaction> transactions = transactionRepository.findByAccountNumberAndTransactionDateBetween(accountNumber, startDate, endDate);

        return transactions.stream()
                .map(this::mapToResponseDto)
                .toList();
    }


    public List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request) {
        return List.of();
    }

    public TransactionSummaryDto getTransactionSummary(String accountNumber, Long branchId) {
        validateAccount(accountNumber);
        return new TransactionSummaryDto(accountNumber, branchId, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }


    public List<TransactionResponseDto> getTransactionsByStatus(TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findByStatus(status);

        return transactions.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public List<Map<String, Object>> getDailyTransactionSummary() {
        List<Object[]> results = transactionRepository.getDailySummary();

        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0]);
            map.put("transactionType", row[1]);
            map.put("totalAmount", row[2]);
            return map;
        }).toList();
    }


    public Map<String, BigDecimal> getAccountSummary(String accountNumber) {
        List<Object[]> results = transactionRepository.getAccountSummary(accountNumber);

        Map<String, BigDecimal> summary = new HashMap<>();
        for (Object[] row : results) {
            String type = row[0].toString();
            BigDecimal total = (BigDecimal) row[1];
            summary.put(type, total);
        }
        return summary;
    }


    public Map<String, Object> getTransactionCharges(String transactionId) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        Map<String, Object> charges = new HashMap<>();
        charges.put("transactionId", txn.getTransactionId());
        charges.put("amount", txn.getAmount());
        charges.put("fee", txn.getFee());
        charges.put("netAmount", txn.getAmount().subtract(
                txn.getFee() != null ? txn.getFee() : BigDecimal.ZERO));
        charges.put("currency", txn.getCurrency());
        charges.put("transactionType", txn.getTransactionType());
        return charges;
    }


    public byte[] generateStatement(String accountNumber, String customerName, String branchName) throws Exception {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph("Account Statement", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Customer Name: " + customerName, normalFont));
        document.add(new Paragraph("Account Number: " + accountNumber, normalFont));
        document.add(new Paragraph("Branch: " + branchName, normalFont));
        document.add(new Paragraph("Generated On: " + java.time.LocalDate.now(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 2f, 2f, 2f, 3f});

        Stream.of("Date", "Txn ID", "Type", "Amount", "Status", "Description")
                .forEach(headerTitle -> {
                    PdfPCell header = new PdfPCell();
                    Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                    header.setBackgroundColor(LIGHT_GRAY);
                    header.setPhrase(new Phrase(headerTitle, headFont));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });

        for (Transaction txn : transactions) {
            table.addCell(txn.getTransactionDate().toString());
            table.addCell(txn.getTransactionId());
            table.addCell(txn.getTransactionType().name());
            table.addCell(String.valueOf(txn.getAmount()));
            table.addCell(txn.getStatus().name());
            table.addCell(txn.getDescription() != null ? txn.getDescription() : "-");
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void validateAccount(String accountNumber) {
        if (!accountClient.accountExists(accountNumber).getBody()) {
            throw new ResourceNotFoundException("Account not found with ID: " + accountNumber);
        }
    }

    private boolean verifyPinWithAccountService(String accountId, int  PIN) {
        try {
            ResponseEntity<Boolean> response =
                    accountClient.verifyAccountPin(accountId, PIN);

            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception ex) {
            log.error("Error verifying PIN: {}", ex.getMessage());
            throw new IllegalStateException("PIN verification failed", ex);
        }
    }


    private void validatePin(String accountNumber) {
        if (!accountClient.accountExists(accountNumber).getBody()) {
            throw new ResourceNotFoundException("Account not found with Number: " + accountNumber);
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
                transaction.getAccountNumber(),
                transaction.getDestinationAccountNumber(),
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
