package com.bms.account.services;

import com.bms.account.dtos.AccountRequestDTO;
import com.bms.account.dtos.AccountResponseDTO;

import java.util.List;

public interface AccountService {

    AccountResponseDTO createAccount(AccountRequestDTO requestDTO);

    AccountResponseDTO getAccountById(Long id);

    List<AccountResponseDTO> getAllAccounts();

    AccountResponseDTO updateAccount(Long id, AccountRequestDTO requestDTO);

    void deleteAccount(Long id);

    AccountResponseDTO getAccountByNumber(String accountNumber);
}
