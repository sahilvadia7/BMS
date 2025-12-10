package com.bms.account.dtos;

import com.bms.account.dtos.accountType.CurrentAccountDetailsDTO;
import com.bms.account.dtos.accountType.SavingsAccountDetailsDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponseDTO implements Serializable {

    private Long id;
    private String accountNumber;
    private String cifNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private Long kycId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String accountPin;

    //  New fields
    private String occupation;
    private String sourceOfIncome;
    private BigDecimal grossAnnualIncome;

    private String nomineeName;
    private String nomineeRelation;
    private Integer nomineeAge;
    private String nomineeContact;

    private SavingsAccountDetailsDTO savingsDetails;
    private CurrentAccountDetailsDTO currentDetails;
}
