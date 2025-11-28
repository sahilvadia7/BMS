package com.bms.transaction.exception;

public class InvalidPinException extends RuntimeException {
	public InvalidPinException(String message) {
		super(message);
	}
}
