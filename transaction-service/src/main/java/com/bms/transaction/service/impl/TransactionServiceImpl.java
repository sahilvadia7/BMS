package com.bms.transaction.service.impl;

import com.bms.transaction.dto.request.TransactionRequest;
import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.response.AccountResponseDTO;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.Channel;
import com.bms.transaction.enums.Currency;
import com.bms.transaction.enums.TransactionStatus;
import com.bms.transaction.enums.TransactionType;
import com.bms.transaction.exception.ResourceNotFoundException;
import com.bms.transaction.feing.AccountClient;
import com.bms.transaction.feing.CustomerClient;
import com.bms.transaction.feing.NotificationClient;
import com.bms.transaction.model.StatementResult;
import com.bms.transaction.model.Transaction;
import com.bms.transaction.repository.TransactionRepository;
import com.bms.transaction.service.TransactionService;
import com.bms.transaction.util.ByteArrayMultipartFile;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;



@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountClient accountClient,
            CustomerClient customerClient,
            NotificationClient notificationClient) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.customerClient = customerClient;
        this.notificationClient = notificationClient;
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
        if (Objects.equals(request.accountNumber(), request.destinationAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        boolean isValidPin = verifyPinWithAccountService(
                request.accountNumber(),
                Integer.parseInt(request.pin())
        );
        if (!isValidPin) {
            throw new IllegalArgumentException("Invalid account PIN");
        }

        validateTransactionLimits(request.accountNumber(), type, request.amount());

        BigDecimal fee = calculateFee(request.accountNumber(), type, request.amount());

        Transaction txn = createPendingTransaction(request, type);

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            txn.setChargeable(true);
            txn.setFee(fee);
        }

        try {
            executeTransactionSteps(type, request, fee, txn);

            txn.setStatus(TransactionStatus.COMPLETED);
            txn.setRemarks("Transaction successful");
            transactionRepository.save(txn);

        } catch (Exception e) {

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


    private void executeTransactionSteps(
            TransactionType type,
            TransactionRequest request,
            BigDecimal fee,
            Transaction mainTxn
    ) {

        BigDecimal principalAmount = request.amount();
        BigDecimal totalDeduct = principalAmount.add(fee);

        switch (type) {

            case WITHDRAWAL, EMI_DEDUCTION -> {

                accountClient.updateBalance(request.accountNumber(), totalDeduct, "WITHDRAW");


                if (fee.compareTo(BigDecimal.ZERO) > 0) {
                    accountClient.updateBalance("AC0000000001", fee, "DEPOSIT");
                    createFeeTransaction(request.accountNumber(), fee, mainTxn.getTransactionId());
                }
            }

            case TRANSFER -> {
                accountClient.updateBalance(request.accountNumber(), totalDeduct, "WITHDRAW");

                validateAccount(request.destinationAccountNumber());
                accountClient.updateBalance(request.destinationAccountNumber(), principalAmount, "DEPOSIT");

                if (fee.compareTo(BigDecimal.ZERO) > 0) {
                    accountClient.updateBalance("AC3631614340", fee, "DEPOSIT");
                    createFeeTransaction(request.accountNumber(), fee, mainTxn.getTransactionId());
                }
            }

            case DEPOSIT, LOAN_DISBURSEMENT, REFUND -> {
                validateAccount(request.destinationAccountNumber());
                accountClient.updateBalance(request.destinationAccountNumber(), principalAmount, "DEPOSIT");
            }
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

    private BigDecimal calculateFee(String accountNumber, TransactionType type, BigDecimal amount) {

        LocalDateTime weekStart = getWeekStart();
        LocalDateTime weekEnd = getWeekEnd();

        BigDecimal weeklyUsed = transactionRepository
                .sumWeeklyAmount(accountNumber, type, weekStart, weekEnd)
                .orElse(BigDecimal.ZERO);

        BigDecimal limit = WEEKLY_LIMITS.get(type);

        // If weekly boundary crossed → 1% fee applies
        if (weeklyUsed.add(amount).compareTo(limit) > 0) {
            return amount.multiply(new BigDecimal("0.01"));
        }

        return BigDecimal.ZERO;
    }


    private Transaction createFeeTransaction(String accountNumber, BigDecimal feeAmount, String originalTxnId) {

        Transaction feeTxn = new Transaction();
        feeTxn.setAccountNumber(accountNumber);
        feeTxn.setDestinationAccountNumber("AC0000000001");
        feeTxn.setTransactionType(TransactionType.FEE);
        feeTxn.setAmount(feeAmount);
        feeTxn.setCurrency(Currency.INR);
        feeTxn.setStatus(TransactionStatus.COMPLETED);
        feeTxn.setDescription("Fee for exceeding weekly limit");
        feeTxn.setChargeable(true);
        feeTxn.setLinkedTransactionId(originalTxnId);
        feeTxn.setTransactionDate(LocalDateTime.now());
        feeTxn.setReferenceId(UUID.randomUUID().toString());

        return transactionRepository.save(feeTxn);
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

    @Override
    public String sendStatement(String accountNumber) throws Exception {

        StatementResult result = generateStatement(accountNumber);

        AccountResponseDTO account = accountClient.getAccountByNumber(accountNumber).getBody();
        if (account == null) {
            throw new RuntimeException("Account not found: " + accountNumber);
        }

        Map<String, Object> customer = customerClient
                .getLimitedInfoByCif(account.getCifNumber())
                .getBody();

        if (customer == null) {
            throw new RuntimeException("Customer not found for CIF: " + account.getCifNumber());
        }


        MultipartFile file = new ByteArrayMultipartFile(
                result.getPdfBytes(),
                "statement"+"_" + accountNumber + ".pdf",
                "application/pdf"
        );

        ResponseEntity<String> response =  notificationClient.sendStatement(
                accountNumber,
                result.getCustomerName(),
                result.getEmail(),
                file
        );

        return response.getBody();
    }


    public StatementResult generateStatement(String accountNumber) throws Exception {

        AccountResponseDTO account = accountClient
                .getAccountByNumber(accountNumber)
                .getBody();

        if (account == null) {
            throw new RuntimeException("Account not found for number: " + accountNumber);
        }

        Map<String, Object> customer =
                customerClient.getLimitedInfoByCif(account.getCifNumber()).getBody();

        if (customer == null) {
            throw new RuntimeException("Customer not found for CIF: " + account.getCifNumber());
        }

        List<Transaction> transactions =
                transactionRepository.findByAccountNumber(accountNumber);

        String dob = String.valueOf(customer.get("dob"));
        if (dob == null || dob.length() < 10) {
            throw new RuntimeException("Invalid DOB for PDF password");
        }
        String birthYear = dob.substring(0, 4);

        String phone = String.valueOf(customer.get("phoneNo"));
        if (phone == null || phone.length() < 4) {
            throw new RuntimeException("Invalid phone number for PDF password");
        }
        String last4 = phone.substring(phone.length() - 4);

        String password = birthYear + last4;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        writer.setEncryption(
                password.getBytes(),
                password.getBytes(),
                PdfWriter.ALLOW_PRINTING,
                PdfWriter.ENCRYPTION_AES_128
        );

        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        document.add(new Paragraph("Account Statement", titleFont));
        document.add(new Paragraph("\n"));

        PdfPTable details = new PdfPTable(2);
        details.setWidthPercentage(100);

        details.addCell(makeCell("Customer Name:", true));
        details.addCell(makeCell(customer.get("firstName") + " " + customer.get("lastName"), false));

        details.addCell(makeCell("Email:", true));
        details.addCell(makeCell(String.valueOf(customer.get("email")), false));

        details.addCell(makeCell("Phone:", true));
        details.addCell(makeCell(String.valueOf(customer.get("phoneNo")), false));

        details.addCell(makeCell("Address:", true));
        details.addCell(makeCell(String.valueOf(customer.get("address")), false));

        details.addCell(makeCell("DOB:", true));
        details.addCell(makeCell(String.valueOf(customer.get("dob")), false));

        details.addCell(makeCell("CIF Number:", true));
        details.addCell(makeCell(account.getCifNumber(), false));

        details.addCell(makeCell("Account Number:", true));
        details.addCell(makeCell(accountNumber, false));

        details.addCell(makeCell("Generated On:", true));
        details.addCell(makeCell(LocalDate.now().toString(), false));

        document.add(details);

        document.add(new Paragraph("\n\n"));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        Stream.of("Date", "Txn ID", "Type", "Amount", "Status", "Description")
                .forEach(col -> table.addCell(makeCell(col, true)));

        for (Transaction txn : transactions) {
            table.addCell(makeCell(txn.getTransactionDate().toString(), false));
            table.addCell(makeCell(txn.getTransactionId(), false));
            table.addCell(makeCell(txn.getTransactionType().name(), false));
            table.addCell(makeCell(txn.getAmount().toString(), false));
            table.addCell(makeCell(txn.getStatus().name(), false));
            table.addCell(makeCell(
                    txn.getDescription() != null ? txn.getDescription() : "-", false
            ));
        }

        document.add(table);
        document.close();

        return new StatementResult(
                baos.toByteArray(),
                customer.get("firstName") + " " + customer.get("lastName"),
                String.valueOf(customer.get("email"))
        );
    }


    private PdfPCell makeCell(String text, boolean bold) {
        Font font = new Font(Font.HELVETICA, 12, bold ? Font.BOLD : Font.NORMAL);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }


    private void validateAccount(String accountNumber) {
        if (Boolean.FALSE.equals(accountClient.accountExists(accountNumber).getBody())) {
            throw new ResourceNotFoundException("Account not found with Number: " + accountNumber);
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

    private TransactionType validateTransactionType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
    }

    private LocalDateTime getWeekStart() {
        return LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
    }

    private LocalDateTime getWeekEnd() {
        return LocalDate.now().with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
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
