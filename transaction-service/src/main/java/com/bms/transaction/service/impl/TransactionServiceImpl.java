package com.bms.transaction.service.impl;


import com.bms.transaction.dto.request.SearchTransactionsRequest;
import com.bms.transaction.dto.response.AccountResponseDTO;
import com.bms.transaction.dto.response.TransactionResponseDto;
import com.bms.transaction.dto.response.TransactionSummaryDto;
import com.bms.transaction.enums.TransactionStatus;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;



@Service
@Slf4j
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    public TransactionResponseDto getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        return mapToResponseDto(transaction);
    }


    public List<TransactionResponseDto> getLatestMonthTransactions(String accountNumber) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);

        List<Transaction> transactions = transactionRepository
                .findByAccountNumberAndTransactionDateBetween(accountNumber, startDate, endDate);

        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .map(this::mapToResponseDto)
                .toList();
    }





    public List<TransactionResponseDto> searchTransactions(SearchTransactionsRequest request) {
        return List.of();
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

        AccountResponseDTO account = accountClient.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found: " + accountNumber);
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
                .getAccountByNumber(accountNumber);

        if (account == null) {
            throw new RuntimeException("Account not found for number: " + accountNumber);
        }

        Map<String, Object> customer =
                customerClient.getLimitedInfoByCif(account.getCifNumber());

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
        if (Boolean.FALSE.equals(accountClient.accountExists(accountNumber))) {
            throw new ResourceNotFoundException("Account not found with Number: " + accountNumber);
        }
    }


    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getTransactionId(),
                "",
                transaction.getAccountNumber(),
                transaction.getDestinationAccountNumber(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getCurrency().name(),
                transaction.getChannelReferenceId(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getTransactionId()
        );
    }
}
