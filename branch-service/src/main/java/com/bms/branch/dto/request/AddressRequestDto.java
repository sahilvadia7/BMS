package com.bms.branch.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record AddressRequestDto(

        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "ZIP/Postal code is required")
        @Pattern(regexp = "\\d{5,6}", message = "ZIP code must be 5 or 6 digits")
        String zipCode

) {}