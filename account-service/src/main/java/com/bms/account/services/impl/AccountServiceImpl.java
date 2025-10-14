package com.bms.account.services.impl;

//import com.bms.account.config.RabbitMQConfig;
import com.bms.account.dtos.AccountRequestDTO;
import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.entities.Account;
import com.bms.account.enums.AccountStatus;
import com.bms.account.exception.ResourceNotFoundException;
import com.bms.account.repositories.AccountRepository;
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
//    private final RabbitTemplate rabbitTemplate;

    private AccountResponseDTO mapToResponse(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCustomerId(),
                account.getBranchId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO requestDTO) {
        Account account = new Account();
        account.setAccountNumber(UUID.randomUUID().toString()); // auto-generate
        account.setAccountType(requestDTO.accountType());
        account.setBalance(requestDTO.balance());
        account.setCustomerId(requestDTO.customerId());
        account.setBranchId(requestDTO.branchId());
        account.setStatus(AccountStatus.PENDING);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);

        // Publish account created event
//        rabbitTemplate.convertAndSend(
//                "account_exchange",
//                "account.created",
//                mapToResponse(saved)
//        );

        return mapToResponse(saved);
    }

    @Override
    public AccountResponseDTO getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO updateAccount(Long id, AccountRequestDTO requestDTO) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setAccountType(requestDTO.accountType());
        account.setBalance(requestDTO.balance());
        account.setCustomerId(requestDTO.customerId());
        account.setBranchId(requestDTO.branchId());
        account.setUpdatedAt(LocalDateTime.now());

        Account updated = accountRepository.save(account);

        // Publish balance updated event
//        rabbitTemplate.convertAndSend(
//                "account_exchange",
//                "account.balance.updated",
//                mapToResponse(updated)
//        );

        return mapToResponse(updated);
    }

    @Override
    public void deleteAccount(Long id) {
        if(!accountRepository.existsById(id)) throw new RuntimeException("Account not found");
        accountRepository.deleteById(id);
    }

    @Override
    public AccountResponseDTO getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Account not found with accountNumber: " + accountNumber));
    }

    @Transactional
    public BigDecimal getBalance(Long accountId) {
        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return acc.getBalance();
    }

    @Transactional
    public void updateBalance(Long accountId, BigDecimal amount, String transactionType) {
        Account acc = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal newBalance = acc.getBalance().add(amount); // caller passes negative for withdrawals OR you interpret type
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        acc.setBalance(newBalance);
        accountRepository.save(acc);
    }

}
