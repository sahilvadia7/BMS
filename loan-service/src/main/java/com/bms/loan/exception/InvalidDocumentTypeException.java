package com.bms.loan.exception;

public class InvalidDocumentTypeException extends RuntimeException {

    public InvalidDocumentTypeException(String message) {
        super(message);
    }
}
