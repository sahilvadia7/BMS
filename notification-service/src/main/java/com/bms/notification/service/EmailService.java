package com.bms.notification.service;

import com.bms.notification.dto.OtpEmailDTO;
import com.bms.notification.dto.request.account.AccountCreationNotificationRequest;
import com.bms.notification.dto.request.account.pin.OtpEmailRequest;
import com.bms.notification.dto.request.loan.ApplyLoanEmailDTO;
import com.bms.notification.dto.request.loan.DisbursementEmailDTO;
import com.bms.notification.dto.request.EmailRequestDTO;
import com.bms.notification.dto.request.loan.SanctionEmailDTO;

public interface EmailService {
    void sendRegistrationEmail(EmailRequestDTO requestDTO);
    void sendOtpEmail(OtpEmailDTO requestDTO);
    void sendSanctionLetterEmail(SanctionEmailDTO request);
    void sendDisbursementEmail(DisbursementEmailDTO request);
    void sendApplyLoanEmail(ApplyLoanEmailDTO request);

    void sendAccountCreatedEmail(AccountCreationNotificationRequest request);

    void sendOtpEmailPin(OtpEmailRequest request);

    void downloadTransactionStatement(String accountNumber,String name,String toEmail,byte[] file);
}
