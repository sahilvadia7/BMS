package com.bms.account.dtos;

import lombok.Data;

@Data
public class CustomerResponseDTO {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String message;
    private String cifNumber;
    private String status;
    private String email;
}
