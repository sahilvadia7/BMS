package com.bms.loan.dto.request;

import com.bms.loan.enums.LoanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDto {
    @NotNull
    private Long customerId;

    @NotNull
    @NotBlank
    private String accountNumber;

    @NotNull
    private LoanType productCode;

    @Positive
    private BigDecimal principal;

    @Positive
    private Integer tenureMonths;

    @Positive
    private BigDecimal annualRate;

    private Integer creditScore;

    private String purpose;

}
