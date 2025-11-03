package com.bms.account.services.impl;

import com.bms.account.constant.AccountStatus;
import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.dtos.*;
import com.bms.account.dtos.accountPin.ChangePinRequest;
import com.bms.account.dtos.accountType.CurrentAccountDetailsDTO;
import com.bms.account.dtos.accountType.CurrentAccountRequestDTO;
import com.bms.account.dtos.accountType.SavingsAccountDetailsDTO;
import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
import com.bms.account.entities.Account;
import com.bms.account.entities.AccountType;
import com.bms.account.entities.accountType.CurrentAccount;
import com.bms.account.entities.accountType.SavingsAccount;
import com.bms.account.exception.ResourceNotFoundException;
import com.bms.account.feign.CustomerClient;
import com.bms.account.feign.NotificationClient;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.repositories.AccountTypeRepository;
import com.bms.account.repositories.accountType.CurrentAccountRepository;
import com.bms.account.repositories.accountType.SavingsAccountRepository;
import com.bms.account.services.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    private int generateAccountPin() {
        return (int) (Math.random() * 9000) + 1000;
    }

    private AccountResponseDTO mapToResponse(Account account) {
        AccountResponseDTO.AccountResponseDTOBuilder builder = AccountResponseDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .cifNumber(account.getCifNumber())
                .accountType(account.getAccountType().getType().name())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .kycId(account.getKycId())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .accountPin(account.getAccountPin())
                //New fields added
                .occupation(account.getOccupation() != null ? account.getOccupation().name() : null)
                .sourceOfIncome(account.getSourceOfIncome() != null ? account.getSourceOfIncome().name() : null)
                .grossAnnualIncome(account.getGrossAnnualIncome())
                .nomineeName(account.getNomineeName())
                .nomineeRelation(account.getNomineeRelation())
                .nomineeAge(account.getNomineeAge())
                .nomineeContact(account.getNomineeContact());

        // Include subtype details
        if (account instanceof SavingsAccount sa) {
            builder.savingsDetails(SavingsAccountDetailsDTO.builder()
                    .minimumBalance(sa.getMinimumBalance())
                    .withdrawalLimitPerMonth(sa.getWithdrawalLimitPerMonth())
                    .chequeBookAvailable(sa.getChequeBookAvailable())
                    .interestRate(sa.getInterestRate())
                    .build());
        } else if (account instanceof CurrentAccount ca) {
            builder.currentDetails(CurrentAccountDetailsDTO.builder()
                    .businessName(ca.getBusinessName())
                    .overdraftLimit(ca.getOverdraftLimit())
                    .monthlyServiceCharge(ca.getMonthlyServiceCharge())
                    .hasOverdraftFacility(ca.getHasOverdraftFacility())
                    .chequeBookAvailable(ca.getChequeBookAvailable())
                    .build());
        }

        return builder.build();
    }


    // ----------------- CREATE SAVINGS ACCOUNT -----------------
    @Override
    public AccountResponseDTO createSavingsAccount(SavingsAccountRequestDTO dto) {
        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
        if (customer == null)
            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());

        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.SAVINGS)
                .orElseThrow(() -> new ResourceNotFoundException("Account type SAVINGS not found"));

        if (savingsAccountRepository.existsByCifNumber(dto.getCifNumber())) {
            throw new IllegalStateException("Customer already has a Savings Account with CIF: " + dto.getCifNumber());
        }

        BigDecimal minimumBalance = BigDecimal.valueOf(5000.00);

        if (dto.getInitialDeposit() == null || dto.getInitialDeposit().compareTo(minimumBalance) < 0) {
            throw new IllegalArgumentException("Initial deposit must be at least ₹" + minimumBalance);
        }

        Long kycId = customerClient.checkKycExists(customer.getCustomerId());
        if (kycId == 0) {
            KycUploadRequest uploadRequest = new KycUploadRequest();
            uploadRequest.setCustomerId(customer.getCustomerId());
            uploadRequest.setKyc(dto.getKycDetails());
            KycResponseDTO uploadedKyc = customerClient.uploadKyc(uploadRequest);
            kycId = uploadedKyc.getId();
        }

        //  Create Savings Account with new fields
        SavingsAccount account = SavingsAccount.builder()
                .cifNumber(dto.getCifNumber())
                .accountType(accountType)
                .balance(dto.getInitialDeposit())
                .status(AccountStatus.PENDING)
                .kycId(kycId)
                .minimumBalance(minimumBalance)
                .withdrawalLimitPerMonth(10)
                .chequeBookAvailable(true)
                .interestRate(BigDecimal.valueOf(3.5))
                .accountPin(generateAccountPin())
                .occupation(dto.getOccupationType())
                .sourceOfIncome(dto.getIncomeSourceType())
                .grossAnnualIncome(dto.getGrossAnnualIncome())
                .nomineeName(dto.getNominee().getNomineeName())
                .nomineeRelation(dto.getNominee().getRelationship())
                .nomineeAge(dto.getNominee().getAge())
                .nomineeContact(dto.getNominee().getContactNumber())
                .build();

        SavingsAccount saved = savingsAccountRepository.save(account);

        // send email notification
        try {
            notificationClient.sendAccountCreationEmail(new AccountCreationNotificationRequest(
                    customer.getFirstName() + " " + customer.getLastName(),
                    customer.getEmail(),
                    dto.getCifNumber(),
                    saved.getAccountNumber(),
                    "SAVINGS",
                    saved.getAccountPin()
            ));
        } catch (Exception e) {
            log.error("Failed to send account creation notification for CIF: {}", dto.getCifNumber(), e);
        }

        return mapToResponse(saved);
    }

    // ----------------- CREATE CURRENT ACCOUNT -----------------
    @Override
    public AccountResponseDTO createCurrentAccount(CurrentAccountRequestDTO dto) {
        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
        if (customer == null)
            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());

        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.CURRENT)
                .orElseThrow(() -> new ResourceNotFoundException("Account type CURRENT not found"));

        if (currentAccountRepository.existsByCifNumber(dto.getCifNumber())) {
            throw new IllegalStateException("Customer already has a Current Account with CIF: " + dto.getCifNumber());
        }

        BigDecimal minimumDeposit = BigDecimal.valueOf(10000.00);

        if (dto.getInitialDeposit() == null || dto.getInitialDeposit().compareTo(minimumDeposit) < 0) {
            throw new IllegalArgumentException("Initial deposit must be at least ₹" + minimumDeposit);
        }

        Long kycId = customerClient.checkKycExists(customer.getCustomerId());
        if (kycId == 0) {
            KycUploadRequest uploadRequest = new KycUploadRequest();
            uploadRequest.setCustomerId(customer.getCustomerId());
            uploadRequest.setKyc(dto.getKycDetails());
            KycResponseDTO uploadedKyc = customerClient.uploadKyc(uploadRequest);
            kycId = uploadedKyc.getId();
        }

        //  Create Current Account with new fields
        CurrentAccount account = CurrentAccount.builder()
                .cifNumber(dto.getCifNumber())
                .accountType(accountType)
                .balance(dto.getInitialDeposit())
                .status(AccountStatus.PENDING)
                .kycId(kycId)
                .businessName(dto.getBusinessName())
                .overdraftLimit(BigDecimal.valueOf(50000))
                .monthlyServiceCharge(BigDecimal.valueOf(250))
                .hasOverdraftFacility(true)
                .chequeBookAvailable(true)
                .accountPin(generateAccountPin())
                .occupation(dto.getOccupationType())
                .sourceOfIncome(dto.getIncomeSourceType())
                .grossAnnualIncome(dto.getGrossAnnualIncome())
                .nomineeName(dto.getNominee().getNomineeName())
                .nomineeRelation(dto.getNominee().getRelationship())
                .nomineeAge(dto.getNominee().getAge())
                .nomineeContact(dto.getNominee().getContactNumber())
                .build();

        CurrentAccount saved = currentAccountRepository.save(account);

        // send email notification
        try {
            notificationClient.sendAccountCreationEmail(new AccountCreationNotificationRequest(
                    customer.getFirstName() + " " + customer.getLastName(),
                    customer.getEmail(),
                    dto.getCifNumber(),
                    saved.getAccountNumber(),
                    "CURRENT",
                    saved.getAccountPin()
            ));
        } catch (Exception e) {
            log.error("Failed to send account creation notification for CIF: {}", dto.getCifNumber(), e);
        }

        return mapToResponse(saved);
    }

    // ----------------- OTHER METHODS -----------------
    @Override
    public List<AccountResponseDTO> getAccountsByCif(String cifNumber) {
        List<Account> accounts = accountRepository.findByCifNumber(cifNumber);
        if (accounts.isEmpty()) throw new ResourceNotFoundException("No accounts found for CIF: " + cifNumber);
        return accounts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return mapToResponse(account);
    }

    @Override
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
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
        throw new UnsupportedOperationException("Account update not implemented yet");
    }

    @Override
    public BigDecimal getBalanceByPin(int accountPin) {
        Account account = accountRepository.findByAccountPin(accountPin)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with PIN: " + accountPin));
        return account.getBalance();
    }

    @Override
    public String activateAccountsByCif(String cifNumber) {
        List<Account> accounts = accountRepository.findByCifNumber(cifNumber);
        if (accounts.isEmpty()) throw new RuntimeException("No accounts found for CIF: " + cifNumber);

        accounts.forEach(acc -> {
            acc.setStatus(AccountStatus.ACTIVE);
            acc.setUpdatedAt(LocalDateTime.now());
        });

        accountRepository.saveAll(accounts);
        return "All accounts for CIF " + cifNumber + " are now ACTIVE.";
    }

    @Override
    public String changeAccountPin(String accountNumber, ChangePinRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));

        if (!account.getAccountPin().equals(request.getOldPin())) {
            throw new IllegalArgumentException("Incorrect old PIN");
        }

        if (request.getNewPin() == null || request.getNewPin() < 1000 || request.getNewPin() > 9999) {
            throw new IllegalArgumentException("PIN must be a 4-digit number");
        }

        account.setAccountPin(request.getNewPin());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        return "Account PIN updated successfully for account: " + accountNumber;
    }
}
