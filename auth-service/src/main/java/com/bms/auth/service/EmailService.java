package com.bms.auth.service;

import com.bms.auth.dto.request.MailRequest;

public interface EmailService {

    public void sendSimpleEmail(String to, String subject, String text);

    }
