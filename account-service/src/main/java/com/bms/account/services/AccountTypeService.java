package com.bms.account.services;

import com.bms.account.dtos.accountType.AccountTypeRequestDto;
import com.bms.account.dtos.accountType.AccountTypeResponseDto;
import com.bms.account.dtos.accountType.AccountTypeUpdateDto;

import java.util.List;

public interface AccountTypeService {

    AccountTypeResponseDto createAccountType(AccountTypeRequestDto requestDTO);

    AccountTypeResponseDto updateAccountType(Long id, AccountTypeUpdateDto requestDTO);

    void deleteAccountType(Long id);

    AccountTypeResponseDto getAccountTypeById(Long id);

    List<AccountTypeResponseDto> getAllAccountTypes();
}
