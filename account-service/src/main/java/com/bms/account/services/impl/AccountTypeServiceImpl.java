package com.bms.account.services.impl;

import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.dtos.accountType.AccountTypeRequestDto;
import com.bms.account.dtos.accountType.AccountTypeResponseDto;
import com.bms.account.dtos.accountType.AccountTypeUpdateDto;
import com.bms.account.entities.AccountType;
import com.bms.account.repositories.AccountTypeRepository;
import com.bms.account.services.AccountTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountTypeServiceImpl implements AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;

    private AccountTypeResponseDto mapToResponse(AccountType entity) {
        return new AccountTypeResponseDto(
                entity.getId(),
                entity.getType(),
                entity.getInterest(),
                entity.getActive()
        );
    }

    @Override
    public AccountTypeResponseDto createAccountType(AccountTypeRequestDto requestDTO) {
        AccountType entity = AccountType.builder()
                .type(requestDTO.type()) // directly enum
                .interest(requestDTO.interest())
                .active(requestDTO.active() != null ? requestDTO.active() : true)
                .build();

        return mapToResponse(accountTypeRepository.save(entity));
    }

    @Override
    public AccountTypeResponseDto updateAccountType(Long id, AccountTypeUpdateDto requestDTO) {
        AccountType entity = accountTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account type not found with ID: " + id));

        if (requestDTO.type() != null && !requestDTO.type().isEmpty()) {
            entity.setType(AccountTypeEnum.valueOf(requestDTO.type().toUpperCase()));
        }
        if (requestDTO.interest() != null) {
            entity.setInterest(requestDTO.interest());
        }
        if (requestDTO.active() != null) {
            entity.setActive(requestDTO.active());
        }

        return mapToResponse(accountTypeRepository.save(entity));
    }

    @Override
    public void deleteAccountType(Long id) {
        AccountType entity = accountTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account type not found with ID: " + id));
        accountTypeRepository.delete(entity);
    }

    @Override
    public AccountTypeResponseDto getAccountTypeById(Long id) {
        return accountTypeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Account type not found with ID: " + id));
    }

    @Override
    public List<AccountTypeResponseDto> getAllAccountTypes() {
        return accountTypeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
