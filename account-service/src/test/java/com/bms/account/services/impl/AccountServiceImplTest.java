package com.bms.account.services.impl;

import com.bms.account.constant.AccountStatus;
import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.dtos.AccountResponseDTO;
import com.bms.account.entities.Account;
import com.bms.account.entities.AccountType;
import com.bms.account.exception.ResourceNotFoundException;
import com.bms.account.feign.CustomerClient;
import com.bms.account.feign.NotificationClient;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.repositories.AccountTypeRepository;
import com.bms.account.repositories.accountType.CurrentAccountRepository;
import com.bms.account.repositories.accountType.SavingsAccountRepository;
import com.bms.account.utility.DocumentValidationService;
import com.bms.account.utility.KycFileUtil;
import com.bms.account.utility.PinEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.valueOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {


    @Mock private AccountTypeRepository accountTypeRepository;
    @Mock private SavingsAccountRepository savingsAccountRepository;
    @Mock private CurrentAccountRepository currentAccountRepository;
    @Mock private CustomerClient customerClient;
    @Mock private NotificationClient notificationClient;
    @Mock private PinEncoder pinEncoder;
    @Mock private KycFileUtil kycFileUtil;
    @Mock private DocumentValidationService documentValidationService;
    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    // ----------- TEST 1: SUCCESS CASE ----------------
    @Test
    void getAccountsByCif_ShouldReturnList_WhenAccountsExist() {

        String cif = "CIF123";

        // --------- Mock AccountType --------
        AccountType accountType = AccountType.builder()
                .id(1L)
                .type(AccountTypeEnum.SAVINGS)
                .interest(new BigDecimal("3.50"))
                .active(true)
                .build();

        // --------- Mock Account --------
        Account account = Account.builder()
                .id(100L)
                .cifNumber(cif)
                .accountNumber("AC1234567890")
                .accountType(accountType)
                .balance(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .accountPin("encoded-pin")
                .build();

        when(accountRepository.findByCifNumber(cif))
                .thenReturn(List.of(account));

        // --------- Call Service ----------
        List<AccountResponseDTO> result = accountService.getAccountsByCif(cif);

        // --------- Assertions -------------
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("AC1234567890", result.get(0).getAccountNumber());
        assertEquals("SAVINGS", result.get(0).getAccountType());

        verify(accountRepository, times(1)).findByCifNumber(cif);
    }

    @Test
    void getAccountsByCif_ShouldThrowException_WhenNoAccountsFound() {
        // Arrange
        String cif = "CIF999";

        when(accountRepository.findByCifNumber(cif))
                .thenReturn(List.of());  // empty list → no accounts

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountsByCif(cif));

        verify(accountRepository, times(1)).findByCifNumber(cif);
    }

}
