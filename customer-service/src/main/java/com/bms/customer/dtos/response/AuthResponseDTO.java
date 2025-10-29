package com.bms.customer.dtos.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private CustomerResponseDTO customer;
    private TokenResponseDTO tokens;
    private String message;
}
