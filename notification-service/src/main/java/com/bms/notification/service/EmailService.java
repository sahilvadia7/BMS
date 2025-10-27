package com.bms.notification.service;

import com.bms.notification.dto.OtpEmailDTO;
import com.bms.notification.dto.request.EmailRequestDTO;

public interface EmailService {
    void sendRegistrationEmail(EmailRequestDTO requestDTO);
    void sendOtpEmail(OtpEmailDTO requestDTO);
}
