package com.bms.branch.dto.response;

public record AddressResponseDto(
        String street,
        String city,
        String state,
        String country,
        String zipCode
) {}