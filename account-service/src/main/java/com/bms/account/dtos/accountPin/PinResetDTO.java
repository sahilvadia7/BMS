package com.bms.account.dtos.accountPin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinResetDTO {
    private String cifNumber;
    private String accountNumber;  //  add this
    private String accountType;
    private String otp;
    private Integer newPin;
}
