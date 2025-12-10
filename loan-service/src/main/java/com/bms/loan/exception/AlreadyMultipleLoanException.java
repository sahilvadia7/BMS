package com.bms.loan.exception;

public class AlreadyMultipleLoanException extends RuntimeException{

    public AlreadyMultipleLoanException(String message){
        super(message);
    }
}
