package com.bms.notification.service;

import com.bms.notification.dto.request.EmailRequestDTO;

public interface EmailService {
    void sendRegistrationEmail(EmailRequestDTO requestDTO);
}
