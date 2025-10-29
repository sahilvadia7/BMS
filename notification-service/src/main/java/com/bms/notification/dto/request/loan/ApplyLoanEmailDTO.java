package com.bms.notification.dto.request.loan;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyLoanEmailDTO {
    private String email;
    private String customerName;
    private Long loanId;
    private String cifNumber;
}
