package com.bms.account.services;

import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.dtos.accountType.CurrentAccountRequestDTO;
import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // Create Accounts
    AccountResponseDTO createSavingsAccount(SavingsAccountRequestDTO requestDTO);
    AccountResponseDTO createCurrentAccount(CurrentAccountRequestDTO requestDTO);

    //  Common Operations
    AccountResponseDTO getAccountById(Long id);
    List<AccountResponseDTO> getAllAccounts();
    AccountResponseDTO getAccountByNumber(String accountNumber);
    BigDecimal getBalance(Long accountId);
    void updateBalance(Long accountId, BigDecimal amount, String transactionType);
    String deleteAccount(Long id);

    //  Optional update (can handle both account types)
    AccountResponseDTO updateAccount(Long id, Object requestDTO);

    List<AccountResponseDTO> getAccountsByCif(String cifNumber);
}
