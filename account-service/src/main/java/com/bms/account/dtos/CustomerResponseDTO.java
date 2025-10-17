package com.bms.account.dtos;

import lombok.Data;

@Data
public class CustomerResponseDTO {

    private Long cId;
    private String message;
    private String cifNumber;
    private String status;
}
