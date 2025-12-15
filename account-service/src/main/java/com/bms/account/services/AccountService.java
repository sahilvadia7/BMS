package com.bms.account.services;

import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.dtos.accountPin.BalanceRequestDTO;
import com.bms.account.dtos.accountPin.ChangePinRequest;
import com.bms.account.dtos.accountType.CurrentAccountRequestDTO;
import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // Create Accounts
    AccountResponseDTO createSavingsAccount(SavingsAccountRequestDTO requestDTO, MultipartFile file);
    AccountResponseDTO createCurrentAccount(CurrentAccountRequestDTO requestDTO, MultipartFile file);

    //  Common Operations
    AccountResponseDTO getAccountById(Long id);
    List<AccountResponseDTO> getAllAccounts();
    AccountResponseDTO getAccountByNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    BigDecimal getBalance(String accountNumber);
    void updateBalance(String accountNumber, BigDecimal amount, String transactionType);
    String deleteAccount(Long id);

    //  Optional update (can handle both account types)
    AccountResponseDTO updateAccount(Long id, Object requestDTO);

    List<AccountResponseDTO> getAccountsByCif(String cifNumber);

    BigDecimal getBalanceByPin(BalanceRequestDTO request);

    String activateAccountsByCif(String cifNumber);

    String changeAccountPin(String accountNumber, ChangePinRequest request);

    boolean verifyAccountPin(String accountNumber, int accountPin);

    BigDecimal updateBalanceAndReturn(
            String accountNumber,
            BigDecimal amount,
            String transactionType
    );
}
