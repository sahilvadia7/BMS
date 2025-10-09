package com.bms.auth.service.impl;

import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public void sendSms(String toPhoneNumber, String message) {
        Message.creator(
                new PhoneNumber("+91" + toPhoneNumber), // For Indian numbers
                new PhoneNumber(fromPhoneNumber),
                message
        ).create();
    }
}
