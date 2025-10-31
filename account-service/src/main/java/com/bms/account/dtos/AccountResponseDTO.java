package com.bms.account.dtos;

import com.bms.account.constant.AccountStatus;
//import com.bms.account.enums.AccountTypeEnum;
import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.dtos.accountType.CurrentAccountDetailsDTO;
import com.bms.account.dtos.accountType.SavingsAccountDetailsDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponseDTO {

    private Long id;
    private String accountNumber;
    private String cifNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private Long kycId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer accountPin;

    private SavingsAccountDetailsDTO savingsDetails;
    private CurrentAccountDetailsDTO currentDetails;
}
