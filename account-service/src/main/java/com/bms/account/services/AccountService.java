package com.bms.account.services;

import com.bms.account.dtos.AccountRequestDTO;
import com.bms.account.dtos.AccountResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface AccountService {
    AccountResponseDTO createAccount(@Valid AccountRequestDTO requestDTO);

    List<AccountResponseDTO> getAllAccounts();

    AccountResponseDTO getAccountById(Long id);

    AccountResponseDTO updateAccount(Long id, @Valid AccountRequestDTO requestDTO);

    void deleteAccount(Long id);
}
