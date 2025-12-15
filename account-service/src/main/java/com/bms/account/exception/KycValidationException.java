package com.bms.account.exception;

public class KycValidationException extends RuntimeException{
    public KycValidationException(String message) {
        super(message);
    }
}
