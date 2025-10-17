package com.bms.loan.service.impl;

import com.bms.loan.Repository.CarLoanRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.entity.CarLoanDetails;
import com.bms.loan.entity.Loans;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SanctionLetterService {

    private final CarLoanRepository carLoanRepo;
    private final LoanRepository loansRepository;
    private final EmailService emailService;
    private final LoanApplicationServiceImpl loanApplicationServiceImpl;



    public SanctionLetterService(CarLoanRepository carLoanRepo, LoanRepository loansRepository, EmailService emailService, LoanApplicationServiceImpl loanApplicationServiceImpl) {
        this.carLoanRepo = carLoanRepo;
        this.loansRepository = loansRepository;
        this.emailService = emailService;
        this.loanApplicationServiceImpl = loanApplicationServiceImpl;
    }


    public void generateAndSend(Long loanId) throws IOException {

//        Loans loan = loansRepository.findByLoanId(loanId)
//                .orElseThrow(EntityNotFoundException::new);

        CarLoanDetails carLoan = carLoanRepo.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Car loan not found"));

        Loans loan = carLoan.getLoans();

        // Generate PDF
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(pdfStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Loan Sanction Letter")
                .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Customer ID: " + loan.getCustomerId()));
        document.add(new Paragraph("Loan ID: " + loan.getLoanId()));
        document.add(new Paragraph("Car Model: " + carLoan.getCarModel()));
        document.add(new Paragraph("Approved Amount: " + loan.getRequestedAmount()));
        document.add(new Paragraph("Tenure (Months): " + loan.getRequestedTenureMonths()));
        document.add(new Paragraph("EMI: " + loanApplicationServiceImpl.calculateEmi(loan.getRequestedAmount(), loan.getInterestRate(), loan.getRequestedTenureMonths())));
        document.add(new Paragraph("Terms & Conditions: [Insert your T&C here]"));

        document.close();

        // Send email with attachment
        emailService.sendEmailWithAttachment(
                "customer@example.com", // Replace with actual customer email from loan entity
                "Your Loan Sanction Letter",
                "Dear Customer,\n\nPlease find attached your loan sanction letter.\n\nRegards, BMS",
                pdfStream.toByteArray(),
                "SanctionLetter.pdf"
        );
    }


}
