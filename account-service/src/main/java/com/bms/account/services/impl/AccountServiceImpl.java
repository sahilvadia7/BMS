package com.bms.account.services.impl;

//import com.bms.account.config.RabbitMQConfig;
import com.bms.account.dtos.AccountRequestDTO;
import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.dtos.CustomerResponseDTO;
import com.bms.account.entities.Account;
import com.bms.account.constant.AccountStatus;
import com.bms.account.entities.AccountType;
import com.bms.account.exception.ResourceNotFoundException;
import com.bms.account.feign.CustomerClient;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.repositories.AccountTypeRepository;
import com.bms.account.services.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final CustomerClient customerClient;

    // Helper: Map Account -> AccountResponseDTO
    private AccountResponseDTO mapToResponse(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType().toString())
                .balance(account.getBalance())
                .status(account.getStatus().toString())
                .cifNumber(account.getCustomerId())
                .branchId(account.getBranchId())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO requestDTO) {

        // 1️⃣ Call the Customer service to create/get customer
        CustomerResponseDTO customerResponse = customerClient.registerCustomer(requestDTO.customer());

        // 2️⃣ Fetch the account type from DB
//        AccountType accountType = accountTypeRepository.findById(requestDTO.getAccountTypeId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "AccountType not found with ID: " + requestDTO.getAccountTypeId()
//                ));

        // 3️⃣ Build Account entity using customer info
        Account account = Account.builder()
                .cifNumber(customerResponse.getCifNumber())
                .customerId(customerResponse.id())
//                .branchId(requestDTO.getBranchId())
                .accountType(requestDTO.accountType())
                .balance(requestDTO.balance() != null ? requestDTO.balance() : BigDecimal.ZERO)
//                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : AccountStatus.PENDING)
                .build();

        // 4️⃣ Save and return response
        return mapToResponse(accountRepository.save(account));
    }


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
    public AccountResponseDTO updateAccount(Long id, AccountRequestDTO requestDTO) {
//        Account account = accountRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
//
//        if (requestDTO.balance() != null) account.setBalance(requestDTO.balance());
////        if (requestDTO. != null) account.setStatus(requestDTO.getStatus());
//        if (requestDTO.accountType().toString() != null) {
//            AccountType accountType = accountTypeRepository.findById(requestDTO.getAccountTypeId())
//                    .orElseThrow(() -> new ResourceNotFoundException("AccountType not found with ID: " + requestDTO.getAccountTypeId()));
//            account.setAccountType(accountType);
//        }
//        return mapToResponse(accountRepository.save(account));
        return null;
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        accountRepository.delete(account);
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
            if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }

        accountRepository.save(account);
    }
}
