package com.bms.account.exception;

import lombok.Getter;

@Getter
public class InvalidPinException extends RuntimeException {
    private final int attempts;
    private final int remaining;
    private final boolean locked;
    private final Long lockExpiresInSeconds;



    public InvalidPinException(String message, int attempts, int remaining, boolean locked, Long lockExpiresInSeconds) {
        super(message);
        this.attempts = attempts;
        this.remaining = remaining;
        this.locked = locked;
        this.lockExpiresInSeconds = lockExpiresInSeconds;
    }

    // Overloaded constructor for simple PIN check
    public InvalidPinException(String message) {
        super(message);
        this.attempts = 0;
        this.remaining = 0;
        this.locked = false;
        this.lockExpiresInSeconds = null;
    }


}

