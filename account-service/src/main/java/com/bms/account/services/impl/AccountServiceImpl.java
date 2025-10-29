package com.bms.account.services.impl;

import com.bms.account.constant.AccountStatus;
import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.dtos.*;
import com.bms.account.dtos.accountType.CurrentAccountRequestDTO;
import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
import com.bms.account.entities.Account;
import com.bms.account.entities.AccountType;
import com.bms.account.entities.accountType.CurrentAccount;
import com.bms.account.entities.accountType.SavingsAccount;
import com.bms.account.exception.ResourceNotFoundException;
import com.bms.account.feign.CustomerClient;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.repositories.AccountTypeRepository;
import com.bms.account.repositories.accountType.CurrentAccountRepository;
import com.bms.account.repositories.accountType.SavingsAccountRepository;
import com.bms.account.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerClient customerClient;

    // 🔹 Convert entity → DTO
    private AccountResponseDTO mapToResponse(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().getType().name())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .cifNumber(account.getCifNumber())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    // 🟢 CREATE SAVINGS ACCOUNT
    @Override
    public AccountResponseDTO createSavingsAccount(SavingsAccountRequestDTO dto) {
        //  1. Verify Customer exists
        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());
        }

        //  2. Get AccountType
        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.SAVINGS)
                .orElseThrow(() -> new ResourceNotFoundException("Account type SAVINGS not found"));

        //  3. Upload KYC
        KycUploadRequest kycUploadRequest = new KycUploadRequest();
        kycUploadRequest.setCustomerId(customer.getCustomerId());
        kycUploadRequest.setKyc(dto.getKycDetails());
        KycResponseDTO uploadedKyc = customerClient.uploadKyc(kycUploadRequest);

        //  4. Create account
        SavingsAccount account = SavingsAccount.builder()
                .cifNumber(dto.getCifNumber())
                .accountType(accountType)
                .balance(dto.getInitialDeposit() != null ? dto.getInitialDeposit() : BigDecimal.ZERO)
                .status(AccountStatus.PENDING)
                .kycId(uploadedKyc.getId())
                .minimumBalance(dto.getMinimumBalance())
                .withdrawalLimitPerMonth(dto.getWithdrawalLimitPerMonth())
                .chequeBookAvailable(dto.getChequeBookAvailable())
                .interestRate(dto.getInterestRate())
                .build();

        SavingsAccount saved = savingsAccountRepository.save(account);
        return mapToResponse(saved);
    }

    // 🟠 CREATE CURRENT ACCOUNT
    @Override
    public AccountResponseDTO createCurrentAccount(CurrentAccountRequestDTO dto) {
        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());
        }

        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.CURRENT)
                .orElseThrow(() -> new ResourceNotFoundException("Account type CURRENT not found"));

        KycUploadRequest kycUploadRequest = new KycUploadRequest();
        kycUploadRequest.setCustomerId(customer.getCustomerId());
        kycUploadRequest.setKyc(dto.getKycDetails());
        KycResponseDTO uploadedKyc = customerClient.uploadKyc(kycUploadRequest);

        CurrentAccount account = CurrentAccount.builder()
                .cifNumber(dto.getCifNumber())
                .accountType(accountType)
                .balance(dto.getInitialDeposit() != null ? dto.getInitialDeposit() : BigDecimal.ZERO)
                .status(AccountStatus.PENDING)
                .kycId(uploadedKyc.getId())
                .businessName(dto.getBusinessName())
                .overdraftLimit(dto.getOverdraftLimit())
                .monthlyServiceCharge(dto.getMonthlyServiceCharge())
                .hasOverdraftFacility(dto.getHasOverdraftFacility())
                .chequeBookAvailable(dto.getChequeBookAvailable())
                .build();

        CurrentAccount saved = currentAccountRepository.save(account);
        return mapToResponse(saved);
    }

    // 🟣 COMMON METHODS
    @Override
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return mapToResponse(account);
    }

    @Override
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));
        return mapToResponse(account);
    }

    @Override
    public BigDecimal getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
        return account.getBalance();
    }

    @Override
    public void updateBalance(Long accountId, BigDecimal amount, String transactionType) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Insufficient balance for withdrawal");
            }
            account.setBalance(newBalance);
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }

        accountRepository.save(account);
    }

    @Override
    public String deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        accountRepository.delete(account);
        return "Account deleted successfully with ID: " + id;
    }

    @Override
    public AccountResponseDTO updateAccount(Long id, Object requestDTO) {
        // For now — just a placeholder
        throw new UnsupportedOperationException("Account update not implemented yet");
    }
}
