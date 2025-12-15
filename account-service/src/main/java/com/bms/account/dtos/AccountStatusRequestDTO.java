package com.bms.account.dtos;


import com.bms.account.constant.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatusRequestDTO {

    @NotNull
    private String status;

    @NotBlank
    private String reason;   // Audit / compliance

}
