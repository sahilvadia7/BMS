package com.bms.account.dtos.accountPin;

import lombok.Data;

@Data
public class ChangePinRequest {
    private Integer oldPin;
    private Integer newPin;
}
