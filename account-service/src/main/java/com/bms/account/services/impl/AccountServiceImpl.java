package com.bms.account.services.impl;

import com.bms.account.dtos.AccountRequestDTO;
import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.entities.Account;
import com.bms.account.enums.AccountStatus;
import com.bms.account.exception.ResourceNotFoundException;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.services.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO requestDTO) {
        Account account = new Account();
        account.setAccountType(requestDTO.accountType());
        account.setBalance(requestDTO.balance());
        account.setCustomerId(requestDTO.customerId());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    @Override
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return mapToResponse(account);
    }

    @Override
    public AccountResponseDTO updateAccount(Long id, AccountRequestDTO requestDTO) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        account.setAccountType(requestDTO.accountType());
        account.setBalance(requestDTO.balance());
        account.setCustomerId(requestDTO.customerId());
        account.setUpdatedAt(LocalDateTime.now());

        Account updated = accountRepository.save(account);
        return mapToResponse(updated);
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        accountRepository.delete(account);
    }

    @Override
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AccountResponseDTO mapToResponse(Account account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
//                account.getCurrency(),
                account.getStatus(),
                account.getCustomerId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
