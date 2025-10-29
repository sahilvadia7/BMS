//package com.bms.account.services.impl;
//
//import com.bms.account.constant.AccountStatus;
//import com.bms.account.constant.AccountTypeEnum;
//import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
//import com.bms.account.entities.AccountType;
//import com.bms.account.entities.accountType.SavingsAccount;
//import com.bms.account.exception.ResourceNotFoundException;
//import com.bms.account.repositories.AccountTypeRepository;
//import com.bms.account.repositories.accountType.SavingsAccountRepository;
//import com.bms.account.services.SavingsAccountService;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//
//
//@Service
//@RequiredArgsConstructor
//public class SavingsAccountServiceImpl implements SavingsAccountService {
//
//    private final SavingsAccountRepository savingsAccountRepository;
//    private final AccountTypeRepository accountTypeRepository;
//
//    @Transactional
//    public SavingsAccount openSavingsAccount(SavingsAccountRequestDTO dto) {
//        // Fetch AccountType (must exist)
//        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.SAVINGS)
//                .orElseThrow(() -> new ResourceNotFoundException("Savings AccountType not found"));
//
////        if (!accountType.getActive()) {
////            throw new IllegalStateException("Savings account type is currently inactive");
//        }
//
//        // Minimum balance validation
//        if (dto.getInitialDeposit().compareTo(dto.getMinimumBalance()) < 0) {
//            throw new IllegalArgumentException("Initial deposit cannot be less than minimum balance");
//        }
//
//        // Build SavingsAccount
//        SavingsAccount account = SavingsAccount.builder()
//                .customerId(dto.getCustomerId())
//                .branchId(dto.getBranchId())
//                .balance(dto.getInitialDeposit())
//                .minimumBalance(dto.getMinimumBalance() != null ? dto.getMinimumBalance() : new BigDecimal("10000"))
//                .withdrawalLimitPerMonth(dto.getWithdrawalLimitPerMonth())
//                .chequeBookAvailable(dto.getChequeBookAvailable() != null ? dto.getChequeBookAvailable() : true)
//                .interestRate(dto.getInterestRate() != null ? dto.getInterestRate() : accountType.getInterest())
//                .accountType(accountType)
//                .status(AccountStatus.ACTIVE)
//                .build();
//
//        return savingsAccountRepository.save(account);
//    }
//}
