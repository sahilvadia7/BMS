package com.bms.loan.enums;

public enum PrepaymentOption {
    REDUCE_TENURE,
    REDUCE_EMI
}

/*

Case 1: REDUCE_TENURE

New principal = old principal - prepayment amount
Keep same EMI
Recalculate new tenure → using amortization formula until principal = 0
Update EMI schedule accordingly (shorter loan)

Case 2: REDUCE_EMI

New principal = old principal - prepayment amount
Keep same remaining tenure (e.g., 180 months left)
Recalculate new EMI based on updated principal and remaining months
Update EMI schedule (same length, lower EMI)


 */
