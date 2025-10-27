package com.bms.customer.exception;

public class OtpValidationException extends RuntimeException {
    public OtpValidationException(String message) {
        super(message);
    }
}
