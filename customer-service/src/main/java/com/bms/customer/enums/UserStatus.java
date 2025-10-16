package com.bms.customer.enums;

public enum UserStatus {

    ACTIVE("Active and allowed to use all services"),
    INACTIVE("Temporarily disabled, can be reactivated later"),
    BLOCKED("Blocked due to suspicious activity or policy violation"),
    CLOSED("Permanently closed account or user record"),
    PENDING("Awaiting verification or approval");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

