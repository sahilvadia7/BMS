package com.bms.account.dtos;

import lombok.Data;

@Data
public class KycExistsResponse {
    private boolean exists;
    private Long kycId;
}
