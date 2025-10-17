package com.bms.customer.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerRegistrationResponseDTO {

    private Long cId;
    private String message;
    private String cifNumber;
    private String status;
}