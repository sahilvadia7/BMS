package com.bms.branch.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    @JsonCreator
    public static Gender fromString(String value) {
        return value == null ? null : Gender.valueOf(value.toUpperCase());
    }
}
