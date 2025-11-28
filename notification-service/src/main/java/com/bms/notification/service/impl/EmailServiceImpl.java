package com.bms.notification.service.impl;

import com.bms.notification.dto.OtpEmailDTO;
import com.bms.notification.dto.request.account.AccountCreationNotificationRequest;
import com.bms.notification.dto.request.account.pin.OtpEmailRequest;
import com.bms.notification.dto.request.loan.ApplyLoanEmailDTO;
import com.bms.notification.dto.request.loan.DisbursementEmailDTO;
import com.bms.notification.dto.request.EmailRequestDTO;
import com.bms.notification.dto.request.loan.EmiSummary;
import com.bms.notification.dto.request.loan.SanctionEmailDTO;
import com.bms.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendRegistrationEmail(EmailRequestDTO requestDTO) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(requestDTO.getToEmail());
            helper.setSubject("Welcome to BMS — Your Account is Ready!");

            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 20px;">
                        <div style="max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);">
                            <h2 style="color: #4a148c;">Dear %s,</h2>
                            <p>Welcome to <b>BMS Online Banking</b>!</p>
                            <p>Your <b>profile has been successfully created</b> in our system.</p>
                            <p><b>CIF ID:</b> %s</p>
                            <p>Your login credentials have been sent to your registered email address.
                               Please check your inbox and use those credentials to log in and complete your KYC verification.</p>
                            <br/>
                            <p style="margin-top: 30px;">Regards,<br/><b>BMS Team</b></p>
                        </div>
                    </body>
                    </html>
                    """
                    .formatted(requestDTO.getCustomerName(), requestDTO.getCifId());

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("Email sent successfully to: " + requestDTO.getToEmail());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public void sendOtpEmail(OtpEmailDTO request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getToEmail());
            helper.setSubject("BMS - Password Reset OTP");

            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                        <h2>Dear %s,</h2>
                        <p>We received a request to reset your password for your <b>BMS Online Banking</b> account.</p>
                        <p>Your One-Time Password (OTP) is:</p>
                        <div style="background: #f3e5f5; padding: 15px; border-radius: 8px; text-align: center;">
                            <h1 style="color: #4a148c; letter-spacing: 4px;">%s</h1>
                        </div>
                        <p>This OTP is valid for <b>5 minutes</b>. Please do not share it with anyone.</p>
                        <br/>
                        <p>If you did not request a password reset, you can safely ignore this email.</p>
                        <br/>
                        <p>Regards,<br/><b>BMS Team</b></p>
                    </body>
                    </html>
                    """.formatted(request.getCustomerName(), request.getOtp());

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("OTP email sent successfully to: " + request.getToEmail());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }

    public void sendSanctionLetterEmail(SanctionEmailDTO request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getToEmail());
            helper.setSubject("BMS - Home Loan Sanction Letter");

            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                        <h2>Dear %s,</h2>
                        <p>We are pleased to inform you that your <b>%s Loan</b> has been sanctioned successfully.</p>

                        <div style="background: #f3f4f6; padding: 15px; border-radius: 8px; margin: 10px 0;">
                            <table style="width:100%%; border-collapse: collapse;">
                                <tr><td><b>Sanctioned Amount:</b></td><td>₹ %s</td></tr>
                                <tr><td><b>Interest Rate:</b></td><td>%s %% p.a.</td></tr>
                                <tr><td><b>Tenure:</b></td><td>%s months</td></tr>
                                <tr><td><b>EMI Amount:</b></td><td>₹ %s</td></tr>
                                <tr><td><b>Sanction Date:</b></td><td>%s</td></tr>
                            </table>
                        </div>

                        <p>Next Steps:</p>
                        <ul>
                            <li>Review the details above carefully.</li>
                            <li>Please complete the <b>e-Sign</b> process to proceed with disbursement.</li>
                        </ul>

                        <p>If you have any questions, contact your relationship manager or visit the nearest BMS branch.</p>
                        <br/>
                        <p>Regards,<br/><b>BMS Loan Department</b></p>
                    </body>
                    </html>
                    """
                    .formatted(
                            request.getCustomerName(),
                            request.getLoanType(),
                            request.getSanctionedAmount().toPlainString(),
                            request.getInterestRate().toPlainString(),
                            request.getTenureMonths(),
                            request.getEmiAmount().toPlainString(),
                            request.getSanctionDate());

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("Sanction letter email sent successfully to: " + request.getToEmail());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send sanction letter email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendDisbursementEmail(DisbursementEmailDTO dto) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(dto.getToEmail());
            helper.setSubject("BMS - Loan Disbursement & EMI Schedule Details");

            StringBuilder emiTable = new StringBuilder();
            for (EmiSummary emi : dto.getFirstFewEmis()) {
                emiTable.append(String.format("""
                            <tr>
                                <td>%d</td>
                                <td>%s</td>
                                <td>₹%.2f</td>
                                <td>₹%.2f</td>
                                <td>₹%.2f</td>
                            </tr>
                        """, emi.getInstallmentNumber(), emi.getDueDate(),
                        emi.getEmiAmount(), emi.getPrincipalComponent(), emi.getInterestComponent()));
            }

            String content = """
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <h2>Dear %s,</h2>
                            <p>Congratulations! Your <b>%s Loan</b> has been successfully disbursed.</p>

                            <h3>📄 Loan Summary:</h3>
                            <ul>
                                <li><b>Sanctioned Amount:</b> ₹%.2f</li>
                                <li><b>Interest Rate:</b> %.2f%%</li>
                                <li><b>Tenure:</b> %d months</li>
                                <li><b>EMI Amount:</b> ₹%.2f</li>
                                <li><b>First EMI Date:</b> %s</li>
                            </ul>

                            <h3>🗓️ Upcoming EMI Schedule (First Few):</h3>
                            <table border="1" cellpadding="8" cellspacing="0" style="border-collapse: collapse;">
                                <tr style="background-color:#f2f2f2;">
                                    <th>No.</th><th>Due Date</th><th>EMI</th><th>Principal</th><th>Interest</th>
                                </tr>
                                %s
                            </table>

                            <p style="margin-top: 15px;">You can view your complete EMI schedule in the BMS customer portal.</p>
                            <p>Regards,<br/><b>BMS Loan Department</b></p>
                        </body>
                        </html>
                    """
                    .formatted(
                            dto.getCustomerName(),
                            dto.getLoanType(),
                            dto.getSanctionedAmount(),
                            dto.getInterestRate(),
                            dto.getTenureMonths(),
                            dto.getEmiAmount(),
                            dto.getFirstEmiDate(),
                            emiTable);

            helper.setText(content, true);
            mailSender.send(message);

            System.out.println("Disbursement email sent to: " + dto.getToEmail());

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send disbursement email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendApplyLoanEmail(ApplyLoanEmailDTO request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(request.getEmail());
            helper.setSubject("BMS - Loan Application Submitted");

            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <h3>Dear %s,</h3>
                        <p>Thank you for applying for a loan with <b>BMS</b>.</p>
                        <p>Your loan application has been successfully submitted.</p>
                        <p><b>Loan ID:</b> %s<br>
                        <b>CIF Number:</b> %s</p>
                        <p>You can use these details to track your loan status in the future.</p>
                        <br>
                        <p>Warm regards,<br>
                        <b>BMS Loan Department</b></p>
                    </body>
                    </html>
                    """.formatted(request.getCustomerName(), request.getLoanId(), request.getCifNumber());

            helper.setText(content, true);
            mailSender.send(message);

        } catch (Exception e) {
            // Log but don’t stop loan creation flow
            System.err.println("Failed to send loan application email: " + e.getMessage());
        }
    }

    public void sendAccountCreatedEmail(AccountCreationNotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getEmail());
        message.setSubject("Your New " + request.getAccountType() + " Account Has Been Created");

        message.setText(String.format("""
                Dear %s,

                Your %s account has been successfully created.

                CIF Number: %s
                Account Number: %s
                Account PIN: %s

                Please keep your PIN confidential and do not share it with anyone.

                Regards,
                Bank Management System
                """,
                request.getCustomerName(),
                request.getAccountType(),
                request.getCifNumber(),
                request.getAccountNumber(),
                request.getAccountPin()));

        mailSender.send(message);
    }

    public void sendOtpEmailPin(OtpEmailRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getEmail());
        message.setSubject("Your OTP for PIN Reset");
        message.setText("Dear customer (CIF: " + request.getCifNumber() + "),\n\n"
                + "Your OTP for PIN reset is: " + request.getOtp() + "\n\n"
                + "This OTP will expire in 5 minutes.\n\n"
                + "Regards,\nBMS Bank");
        mailSender.send(message);
    }

    @Override
    public void downloadTransactionStatement(String accountNumber, String name, String toEmail, byte[] file) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("BMS - Your Account Statement");

            String content = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <h3>Dear %s,</h3>
                        <p>Your account statement for <b>Account Number: %s</b> is attached.</p>
                        <p>This PDF may be password-protected.</p>
                        <p>
                            <b>Password Format:</b> Birth Year + Last 4 digits of Mobile Number
                        </p>
                        <br>
                        <p>Regards,<br>
                        <b>BMS Support Team</b></p>
                    </body>
                    </html>
                    """.formatted(name, accountNumber);

            helper.setText(content, true);

            helper.addAttachment(
                    "Account_Statement_" + accountNumber + ".pdf",
                    new ByteArrayResource(file));

            mailSender.send(message);

            System.out.println("Statement email sent successfully.");
        } catch (Exception e) {
            System.err.println("Failed to send statement email: " + e.getMessage());
        }
    }
}
