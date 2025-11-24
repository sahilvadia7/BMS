package com.bms.loan.dto.request.loan;

import com.bms.loan.enums.PrepaymentOption;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPrepaymentRequest {

    private BigDecimal prepaymentAmount;
    private PrepaymentOption option;  // REDUCE_TENURE or REDUCE_EMI
}
