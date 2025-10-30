package com.bms.loan.enums;

import java.util.Arrays;
import java.util.List;

public enum DocumentType {
    AADHAAR,
    PAN,
    SALARY_SLIP,
    INCOME_TAX_RETURN,
    PROPERTY_PAPER,
    VEHICLE_INVOICE,
    EDUCATION_ADMISSION_LETTER,
    BUSINESS_PROOF,
    ADDRESS_PROOF,
    PHOTO,
    BANK_STATEMENT;


    // This method tells which documents are required based on LoanType
    public static List<DocumentType> getRequiredForLoan(LoanType loanType) {
        switch (loanType) {
            case PERSONAL:
                return Arrays.asList(AADHAAR, PAN, SALARY_SLIP, BANK_STATEMENT);
            case HOME:
                return Arrays.asList(AADHAAR, PAN, ADDRESS_PROOF, PROPERTY_PAPER ,SALARY_SLIP ,BANK_STATEMENT, PHOTO);
            case EDUCATION:
                return Arrays.asList(AADHAAR, PAN, BANK_STATEMENT, EDUCATION_ADMISSION_LETTER);
            case CAR:
                return Arrays.asList(AADHAAR, PAN, BANK_STATEMENT, VEHICLE_INVOICE ,SALARY_SLIP, PHOTO);
            default:
                return Arrays.asList(AADHAAR, PAN);
        }
    }
}
