package com.bms.account.dtos.accountPin;

import lombok.Data;

@Data
public class BalanceRequestDTO {

    private String accountNumber;
    private String accountPin;
}
