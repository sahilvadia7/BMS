package com.bms.customer.dtos.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}

