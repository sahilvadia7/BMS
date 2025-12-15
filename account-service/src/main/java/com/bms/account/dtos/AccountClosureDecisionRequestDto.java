package com.bms.account.dtos;

import com.bms.account.constant.AccountClosureDecision;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountClosureDecisionRequestDto {
    private AccountClosureDecision decision;
    private String reason;
}
