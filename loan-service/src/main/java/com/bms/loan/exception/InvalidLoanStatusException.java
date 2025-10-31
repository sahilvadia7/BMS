package com.bms.loan.exception;

public class InvalidLoanStatusException extends RuntimeException {
    public InvalidLoanStatusException(String message) {
        super(message);
    }
}
