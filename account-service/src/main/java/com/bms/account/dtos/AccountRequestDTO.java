package com.bms.account.dtos;

import com.bms.account.constant.AccountTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequestDTO(

        @Valid
        @NotNull(message = "Customer details are required")
        CustomerRegisterRequestDTO customer,

        @NotNull(message = "Account type is required")
        Long accountTypeId,

        @NotNull(message = "Balance is required")
        BigDecimal balance

//        @NotNull(message = "Customer ID is required")
//        Long customerId,

//        @NotNull(message = "Branch ID is required")
//        Long branchId
) { }
