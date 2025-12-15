package com.bms.account.services.impl;

import com.bms.account.constant.AccountClosureDecision;
import com.bms.account.constant.AccountStatus;
import com.bms.account.constant.AccountTypeEnum;
import com.bms.account.dtos.*;
import com.bms.account.dtos.accountPin.BalanceRequestDTO;
import com.bms.account.dtos.accountPin.ChangePinRequest;
import com.bms.account.dtos.accountType.CurrentAccountDetailsDTO;
import com.bms.account.dtos.accountType.CurrentAccountRequestDTO;
import com.bms.account.dtos.accountType.SavingsAccountDetailsDTO;
import com.bms.account.dtos.accountType.SavingsAccountRequestDTO;
import com.bms.account.entities.Account;
import com.bms.account.entities.AccountType;
import com.bms.account.entities.accountType.CurrentAccount;
import com.bms.account.entities.accountType.SavingsAccount;
import com.bms.account.exception.*;
import com.bms.account.feign.CustomerClient;
import com.bms.account.feign.NotificationClient;
import com.bms.account.repositories.AccountRepository;
import com.bms.account.repositories.AccountTypeRepository;
import com.bms.account.repositories.accountType.CurrentAccountRepository;
import com.bms.account.repositories.accountType.SavingsAccountRepository;
import com.bms.account.services.AccountService;
import com.bms.account.utility.*;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;
    private final PinEncoder pinEncoder;
    private final KycFileUtil kycFileUtil;
    private final DocumentValidationService documentValidationService;
//    private final OcrService ocrService;
//    private final PinAttemptService pinAttemptService;
//
    public AccountServiceImpl(AccountRepository accountRepository,
                              AccountTypeRepository accountTypeRepository,
                              SavingsAccountRepository savingsAccountRepository,
                              CurrentAccountRepository currentAccountRepository,
                              CustomerClient customerClient,
                              NotificationClient notificationClient, PinEncoder pinEncoder,
                              KycFileUtil kycFileUtil,
                              DocumentValidationService documentValidationService
//                              OcrService ocrService
//                              PinAttemptService pinAttemptService
    ) {
        this.accountRepository = accountRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.currentAccountRepository = currentAccountRepository;
        this.customerClient = customerClient;
        this.notificationClient = notificationClient;
        this.pinEncoder = pinEncoder;
        this.kycFileUtil = kycFileUtil;
        this.documentValidationService = documentValidationService;
//        this.ocrService = ocrService;
//        this.pinAttemptService = pinAttemptService;
    }

    // Generate random 4-digit PIN as String
    private String generateAccountPin() {
        int pin = (int) (Math.random() * 9000) + 1000;
        return String.valueOf(pin);
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
                // new fields
                .occupation(account.getOccupation() != null ? account.getOccupation().name() : null)
                .sourceOfIncome(account.getSourceOfIncome() != null ? account.getSourceOfIncome().name() : null)
                .grossAnnualIncome(account.getGrossAnnualIncome())
                .nomineeName(account.getNomineeName())
                .nomineeRelation(account.getNomineeRelation())
                .nomineeAge(account.getNomineeAge())
                .nomineeContact(account.getNomineeContact());

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

    // ---------------- CREATE SAVINGS ACCOUNT ----------------
    @Override
    public AccountResponseDTO createSavingsAccount(SavingsAccountRequestDTO dto, MultipartFile file) {

        long startTime = System.currentTimeMillis();
        log.info("CreateSavingsAccount API started...");

        System.out.println("Name: " + file.getOriginalFilename());

        // 1⃣ Fetch customer (blocking, must be synchronous)
        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
        if (customer == null)
            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());

        // 2️ Fetch account type (cached or fast DB)
        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.SAVINGS)
                .orElseThrow(() -> new ResourceNotFoundException("Account type SAVINGS not found"));

        // 3️ Check existing account
        if (savingsAccountRepository.existsByCifNumber(dto.getCifNumber())) {
            throw new AccountAlreadyExistsException("Customer already has a Savings Account with CIF: " + dto.getCifNumber());
        }

        // 4️ Validate minimum deposit
        BigDecimal minimumBalance = BigDecimal.valueOf(5000.00);
        if (dto.getInitialDeposit() == null || dto.getInitialDeposit().compareTo(minimumBalance) < 0) {
            throw new IllegalArgumentException("Initial deposit must be at least ₹" + minimumBalance);
        }

        // 🔹 Run OCR validation & file saving in parallel (async)
        CompletableFuture<Boolean> documentValidationFuture = CompletableFuture.supplyAsync(() ->
                documentValidationService.validateDocumentType(
                        file,
                        dto.getKycDetails().getDocumentType(),
                        dto.getKycDetails().getDocumentNumber()
                )
        );

        CompletableFuture<String> fileSaveFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return kycFileUtil.saveFile(file, customer.getCifNumber());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save KYC file", e);
            }
        });

        boolean validType;
        try {
            validType = documentValidationFuture.get(); // wait only when needed
        } catch (Exception e) {
            throw new RuntimeException("KYC validation failed", e);
        }

        if (!validType) {
            throw new KycValidationException("Uploaded document does not match declared type: " + dto.getKycDetails().getDocumentType());
        }

        String savedFilePath;
        try {
            savedFilePath = fileSaveFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("File saving failed", e);
        }

        // ️5️ Upload KYC microservice call (same business logic)
        KycUploadRequest uploadRequest = new KycUploadRequest();
        uploadRequest.setCustomerId(customer.getCustomerId());
        uploadRequest.setKyc(dto.getKycDetails());
        uploadRequest.getKyc().setDocumentType(dto.getKycDetails().getDocumentType());
        uploadRequest.getKyc().setDocumentNumber(dto.getKycDetails().getDocumentNumber());
        uploadRequest.getKyc().setDocumentUrl(savedFilePath);
        uploadRequest.getKyc().setDocumentFileName(file.getOriginalFilename());
        KycResponseDTO uploadedKyc = customerClient.uploadKyc(uploadRequest);
        Long kycId = uploadedKyc.getId();

        // 6️ Generate account PIN
        String rawPin = generateAccountPin();
        String encodedPin = pinEncoder.encode(rawPin);

        // 7⃣ Build Account Entity
        SavingsAccount account = SavingsAccount.builder()
                .cifNumber(dto.getCifNumber())
                .accountType(accountType)
                .balance(dto.getInitialDeposit())
                .status(AccountStatus.PENDING)
                .kycId(kycId)
                .minimumBalance(minimumBalance)
                .withdrawalLimitPerMonth(5)
                .chequeBookAvailable(false)
                .interestRate(BigDecimal.valueOf(3.5))
                .accountPin(encodedPin)
                .occupation(dto.getOccupationType())
                .sourceOfIncome(dto.getIncomeSourceType())
                .grossAnnualIncome(dto.getGrossAnnualIncome())
                .nomineeName(dto.getNominee().getNomineeName())
                .nomineeRelation(dto.getNominee().getRelationship())
                .nomineeAge(dto.getNominee().getAge())
                .nomineeContact(dto.getNominee().getContactNumber())
                .build();

        SavingsAccount saved = savingsAccountRepository.save(account);

        // 📨 Email sending moved to async (does NOT block API response)
        CompletableFuture.runAsync(() -> {
            try {
                notificationClient.sendAccountCreationEmail(
                        new AccountCreationNotificationRequest(
                                customer.getFirstName() + " " + customer.getLastName(),
                                customer.getEmail(),
                                dto.getCifNumber(),
                                saved.getAccountNumber(),
                                "SAVINGS",
                                rawPin
                        )
                );
            } catch (Exception e) {
                log.error("Failed to send account creation notification for CIF: {}", dto.getCifNumber(), e);
            }
        });

        log.info("CreateSavingsAccount API completed in {} ms",
                System.currentTimeMillis() - startTime);

        return mapToResponse(saved);
    }

//    @Override
//    public AccountResponseDTO createSavingsAccount(SavingsAccountRequestDTO dto, MultipartFile file) {
//        System.out.println("Name: "+file.getOriginalFilename());
//        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
//        if (customer == null)
//            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());
//
//        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.SAVINGS)
//                .orElseThrow(() -> new ResourceNotFoundException("Account type SAVINGS not found"));
//
//        if (savingsAccountRepository.existsByCifNumber(dto.getCifNumber())) {
//            throw new AccountAlreadyExistsException("Customer already has a Savings Account with CIF: " + dto.getCifNumber());
//        }
//
//        BigDecimal minimumBalance = BigDecimal.valueOf(5000.00);
//        if (dto.getInitialDeposit() == null || dto.getInitialDeposit().compareTo(minimumBalance) < 0) {
//            throw new IllegalArgumentException("Initial deposit must be at least ₹" + minimumBalance);
//        }
//
////        Long kycId = customerClient.checkKycExists(customer.getCustomerId());
////        if (kycId == 0) {
//
//            // 5b️⃣ Extract text using OCR
//            boolean validType;
//            System.out.println("Name: "+file.getOriginalFilename());
//            validType = documentValidationService.validateDocumentType(file , dto.getKycDetails().getDocumentType(), dto.getKycDetails().getDocumentNumber());
//            log.info("Extracted text from KYC file: {}", validType);
//
//            if (!validType) {
//                throw new KycValidationException("Uploaded document does not match the declared type: " + dto.getKycDetails().getDocumentType());
//            }
//
//            // Save file locally
//            String savedFilePath;
//            try {
////                log.info("Upload dir: {}", uploadDir);
//                log.info("Original filename: {}", file.getOriginalFilename());
//
//                savedFilePath = kycFileUtil.saveFile(file, customer.getCifNumber());
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to save KYC file", e);
//            }
//            KycUploadRequest uploadRequest = new KycUploadRequest();
//            uploadRequest.setCustomerId(customer.getCustomerId());
//            uploadRequest.setKyc(dto.getKycDetails());
//            uploadRequest.getKyc().setDocumentType(dto.getKycDetails().getDocumentType());
//            uploadRequest.getKyc().setDocumentNumber(dto.getKycDetails().getDocumentNumber());
//            uploadRequest.getKyc().setDocumentUrl(savedFilePath);
//            uploadRequest.getKyc().setDocumentFileName(file.getOriginalFilename());
//            System.out.println(uploadRequest.toString());
//            KycResponseDTO uploadedKyc = customerClient.uploadKyc(uploadRequest);
//            Long kycId = uploadedKyc.getId();
////        }
//
//        String rawPin = generateAccountPin();
//        String encodedPin = pinEncoder.encode(rawPin);
//
//        SavingsAccount account = SavingsAccount.builder()
//                .cifNumber(dto.getCifNumber())
//                .accountType(accountType)
//                .balance(dto.getInitialDeposit())
//                .status(AccountStatus.PENDING)
//                .kycId(kycId)
//                .minimumBalance(minimumBalance)
//                .withdrawalLimitPerMonth(5)
//                .chequeBookAvailable(false)
//                .interestRate(BigDecimal.valueOf(3.5))
//                .accountPin(encodedPin) // store encoded PIN
//                .occupation(dto.getOccupationType())
//                .sourceOfIncome(dto.getIncomeSourceType())
//                .grossAnnualIncome(dto.getGrossAnnualIncome())
//                .nomineeName(dto.getNominee().getNomineeName())
//                .nomineeRelation(dto.getNominee().getRelationship())
//                .nomineeAge(dto.getNominee().getAge())
//                .nomineeContact(dto.getNominee().getContactNumber())
//                .build();
//
//        SavingsAccount saved = savingsAccountRepository.save(account);
//
//        try {
//            notificationClient.sendAccountCreationEmail(new AccountCreationNotificationRequest(
//                    customer.getFirstName() + " " + customer.getLastName(),
//                    customer.getEmail(),
//                    dto.getCifNumber(),
//                    saved.getAccountNumber(),
//                    "SAVINGS",
//                    rawPin // send plain PIN to user email
//            ));
//        } catch (Exception e) {
//            log.error("Failed to send account creation notification for CIF: {}", dto.getCifNumber(), e);
//        }
//
//        return mapToResponse(saved);
//    }

    // ---------------- CREATE CURRENT ACCOUNT ----------------
    @Override
    public AccountResponseDTO createCurrentAccount(CurrentAccountRequestDTO dto, MultipartFile file) {
        CustomerResponseDTO customer = customerClient.getByCif(dto.getCifNumber());
        if (customer == null)
            throw new ResourceNotFoundException("Customer not found with CIF: " + dto.getCifNumber());

        AccountType accountType = accountTypeRepository.findByType(AccountTypeEnum.CURRENT)
                .orElseThrow(() -> new ResourceNotFoundException("Account type CURRENT not found"));

        if (currentAccountRepository.existsByCifNumber(dto.getCifNumber())) {
            throw new AccountAlreadyExistsException("Customer already has a Current Account with CIF: " + dto.getCifNumber());
        }

        BigDecimal minimumDeposit = BigDecimal.valueOf(10000.00);
        if (dto.getInitialDeposit() == null || dto.getInitialDeposit().compareTo(minimumDeposit) < 0) {
            throw new IllegalArgumentException("Initial deposit must be at least ₹" + minimumDeposit);
        }

            // 5b️⃣ Extract text using OCR
            boolean validType;
            System.out.println("Name: "+file.getOriginalFilename());
            validType = documentValidationService.validateDocumentType(file , dto.getKycDetails().getDocumentType(), dto.getKycDetails().getDocumentNumber());
            log.info("Extracted text from KYC file: {}", validType);

            if (!validType) {
                throw new KycValidationException("Uploaded document does not match the declared type: " + dto.getKycDetails().getDocumentType());
            }

            // Save file locally
            String savedFilePath;
            System.out.println(file.getOriginalFilename());
            try {
//                log.info("Upload dir: {}", uploadDir);
                log.info("Original filename: {}", file.getOriginalFilename());

                savedFilePath = kycFileUtil.saveFile(file, customer.getCifNumber());
            } catch (IOException e) {
                throw new RuntimeException("Failed to save KYC file", e);
            }

            KycUploadRequest uploadRequest = new KycUploadRequest();
            uploadRequest.setCustomerId(customer.getCustomerId());
            uploadRequest.setKyc(dto.getKycDetails());
            uploadRequest.getKyc().setDocumentType(dto.getKycDetails().getDocumentType());
            uploadRequest.getKyc().setDocumentNumber(dto.getKycDetails().getDocumentNumber());
            uploadRequest.getKyc().setDocumentUrl(savedFilePath);
            uploadRequest.getKyc().setDocumentFileName(file.getOriginalFilename());
            System.out.println(uploadRequest);
            KycResponseDTO uploadedKyc = customerClient.uploadKyc(uploadRequest);
            Long kycId = uploadedKyc.getId();

        String rawPin = generateAccountPin();
        String encodedPin = pinEncoder.encode(rawPin);

        CurrentAccount account = CurrentAccount.builder()
                .cifNumber(dto.getCifNumber())
                .accountType(accountType)
                .balance(dto.getInitialDeposit())
                .status(AccountStatus.PENDING)
                .kycId(kycId)
                .businessName(dto.getBusinessName())
                .overdraftLimit(BigDecimal.valueOf(50000))
                .monthlyServiceCharge(BigDecimal.valueOf(80))
                .hasOverdraftFacility(false)
                .chequeBookAvailable(false)
                .accountPin(encodedPin) // encoded
                .occupation(dto.getOccupationType())
                .sourceOfIncome(dto.getIncomeSourceType())
                .grossAnnualIncome(dto.getGrossAnnualIncome())
                .nomineeName(dto.getNominee().getNomineeName())
                .nomineeRelation(dto.getNominee().getRelationship())
                .nomineeAge(dto.getNominee().getAge())
                .nomineeContact(dto.getNominee().getContactNumber())
                .build();

        CurrentAccount saved = currentAccountRepository.save(account);

        try {
            notificationClient.sendAccountCreationEmail(new AccountCreationNotificationRequest(
                    customer.getFirstName() + " " + customer.getLastName(),
                    customer.getEmail(),
                    dto.getCifNumber(),
                    saved.getAccountNumber(),
                    "CURRENT",
                    rawPin // send plain PIN only in email
            ));
        } catch (Exception e) {
            log.error("Failed to send account creation notification for CIF: {}", dto.getCifNumber(), e);
        }

        return mapToResponse(saved);
    }

    // ---------------- OTHER METHODS ----------------
    @Override
//    @Cacheable(value = "getAllAccounts",key = "#cifNumber")
    public List<AccountResponseDTO> getAccountsByCif(String cifNumber) {
        List<Account> accounts = accountRepository.findByCifNumber(cifNumber);
        if (accounts.isEmpty())
            throw new ResourceNotFoundException("No accounts found for CIF: " + cifNumber);
        return accounts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return mapToResponse(account);
    }

    @Override
    public BigDecimal getBalanceByPin(BalanceRequestDTO request) {
        System.out.println("Account Number : "+request.getAccountNumber());
        System.out.println("Account Number : "+request.getAccountPin());
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(
                        () -> new ResourceNotFoundException("No account found with Account Number: " + request.getAccountNumber()));

        // verify hashed PIN
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(request.getAccountPin(), account.getAccountPin())) {
            // throw new InvalidPinException("Invalid PIN entered.");
            throw new InvalidPinException("Invalid PIN entered");
        }

        return account.getBalance();
    }
//@Override
//public BigDecimal getBalanceByPin(BalanceRequestDTO request) {
//
//    String accountNumber = request.getAccountNumber();
//    String pin = request.getAccountPin();
//
//    //  Check lock before DB call
//    if (pinAttemptService.isAccountLocked(accountNumber)) {
//        throw new InvalidPinException(
//                "Account locked due to failed PIN attempts!",
//                pinAttemptService.getAttempts(accountNumber),
//                0,
//                true,
//                pinAttemptService.getRemainingLockTime(accountNumber)
//        );
//    }
//
//    Account account = accountRepository.findByAccountNumber(accountNumber)
//            .orElseThrow(() -> new ResourceNotFoundException("Invalid account number"));
//
//    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//    if (!encoder.matches(pin, account.getAccountPin())) {
//
//        int attempts = pinAttemptService.recordFailedAttempt(accountNumber);
//        int remaining = pinAttemptService.getMaxAttempts() - attempts;
//        boolean locked = remaining <= 0;
//
//        throw new InvalidPinException(
//                locked ? "Account locked due to too many wrong attempts!" : "Wrong PIN! Try again.",
//                attempts,
//                Math.max(remaining, 0),
//                locked,
//                locked ? pinAttemptService.getRemainingLockTime(accountNumber) : null
//        );
//    }
//
//    // 🟢 Success — Reset Attempts
//    pinAttemptService.resetAttempts(accountNumber);
//
//    return account.getBalance();
//}




    @Override
    public String changeAccountPin(String accountNumber, ChangePinRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));

        // verify using encoder
        if (!pinEncoder.matches(String.valueOf(request.getOldPin()), account.getAccountPin())) {
            throw new InvalidPinException("Incorrect old PIN");
        }

        if (request.getNewPin() == null || request.getNewPin() < 1000 || request.getNewPin() > 9999) {
            throw new IllegalArgumentException("PIN must be a 4-digit number");
        }

        String encodedNewPin = pinEncoder.encode(String.valueOf(request.getNewPin()));
        account.setAccountPin(encodedNewPin);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        return "Account PIN updated successfully for account: " + accountNumber;
    }

    @Override
    public boolean verifyAccountPin(String accountNumber, int accountPin) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return pinEncoder.matches(String.valueOf(accountPin), account.getAccountPin());
    }

    @Override
    @Transactional
    public String updateAccountStatus(String accountNumber, AccountStatusRequestDTO request) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found: " + accountNumber));

        CustomerResponseDTO customer = customerClient.getByCif(account.getCifNumber());
        //  Closed accounts cannot be changed
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStateException("Closed account cannot be modified");
        }

        //  No unnecessary update
        if (account.getStatus().name().equals(request.getStatus())) {
            throw new AccountStateException(
                    "Account is already " + request.getStatus()
            );
        }

        account.setStatus(AccountStatus.valueOf(request.getStatus()));

        // Optional: save audit log
        log.info("Account {} status changed to {} | Reason: {}",
                accountNumber,
                request.getStatus(),
                request.getReason());

        accountRepository.save(account);
        //  SEND EMAIL VIA NOTIFICATION SERVICE
        AccountStatusChangeNotificationRequest accountStatusChangeNotificationRequest =
                new AccountStatusChangeNotificationRequest();

        accountStatusChangeNotificationRequest.setCustomerName(customer.getFirstName()+" "+customer.getLastName());
        accountStatusChangeNotificationRequest.setEmail(customer.getEmail());
        accountStatusChangeNotificationRequest.setAccountNumber(account.getAccountNumber());
        accountStatusChangeNotificationRequest.setNewStatus(request.getStatus());
        accountStatusChangeNotificationRequest.setReason(request.getReason());

        notificationClient.sendAccountStatusChangedEmail(accountStatusChangeNotificationRequest);

        return "Account " + request.getStatus() + " successfully";
    }

    @Override
    public String closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found: " + accountNumber));
        CustomerResponseDTO customer = customerClient.getByCif(account.getCifNumber());
        //  Already closed
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountStateException("Account is already closed");
        }

        //  Already requested
        if (account.getStatus() == AccountStatus.CLOSE_REQUESTED) {
            throw new AccountStateException("Account closure already requested");
        }

        //  Balance must be zero
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountStateException(
                    "Account balance must be zero before closing"
            );
        }

        //  Soft close
        account.setStatus(AccountStatus.CLOSE_REQUESTED);
        accountRepository.save(account);

        //  Send notification
        AccountCloseRequestNotification notification =
                new AccountCloseRequestNotification();

        notification.setCustomerName(customer.getFirstName()+" "+customer.getLastName());
        notification.setEmail(customer.getEmail());
        notification.setAccountNumber(account.getAccountNumber());
//        notification.setReason(request.getReason());

        notificationClient.sendAccountCloseRequestEmail(notification);

        return "Account closure request submitted successfully";
    }

    @Override
    public List<AccountResponseDTO> getPendingCloseRequests() {

        List<Account> accounts =
                accountRepository.findByStatus(AccountStatus.CLOSE_REQUESTED);

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No pending close requests found");
        }

        return accounts.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void decideAccountClosure(
            String accountNumber,
            AccountClosureDecisionRequestDto request) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.CLOSE_REQUESTED) {
            throw new AccountStateException(
                    "Account closure not requested"
            );
        }

        CustomerResponseDTO customer =
                customerClient.getByCif(account.getCifNumber());

        AccountClosureDecision decision = request.getDecision();

        if (decision == AccountClosureDecision.APPROVED) {

            account.setStatus(AccountStatus.CLOSED);
            accountRepository.save(account);

            //  APPROVAL EMAIL
            notificationClient.sendAccountClosureDecisionEmail(
                    new AccountClosureDecisionNotification(
                            customer.getEmail(),
                            customer.getFirstName() + " " + customer.getLastName(),
                            account.getAccountNumber(),
                            request.getDecision().name(),
                            null
                    )
            );

        } else if (decision == AccountClosureDecision.REJECTED) {

            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new AccountStateException(
                        "Rejection reason is mandatory"
                );
            }

            account.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(account);

            //  REJECTION EMAIL

            notificationClient.sendAccountClosureDecisionEmail(
                    new AccountClosureDecisionNotification(
                            customer.getEmail(),
                            customer.getFirstName() + " " + customer.getLastName(),
                            account.getAccountNumber(),
                            request.getDecision().name(),
                            request.getReason()
                    )
            );

        } else {
            throw new AccountStateException("Invalid closure decision");
        }
    }


    @Override
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with Number : " + accountNumber));
        return account.getBalance();
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal amount, String transactionType) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Account not found with Account NO : " + accountNumber));

        if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            account.setBalance(newBalance);
        } else {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        accountRepository.save(account);
    }

    @Override
    public BigDecimal updateBalanceAndReturn(
            String accountNumber,
            BigDecimal amount,
            String transactionType) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with Account NO : " + accountNumber));

        if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        }
        else if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Insufficient balance");
            }
            account.setBalance(newBalance);
        }
        else {
            throw new IllegalArgumentException("Invalid transaction type");
        }

        accountRepository.save(account);
        return account.getBalance();
    }


    @Override
    public String deleteAccount(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        accountRepository.delete(acc);
        return "Account deleted successfully with ID: " + id;
    }

    @Override
    public AccountResponseDTO updateAccount(Long id, Object dto) {
        throw new UnsupportedOperationException("Account update not implemented yet");
    }

    @Override
    public String activateAccountsByCif(String cifNumber) {
        List<Account> accounts = accountRepository.findByCifNumber(cifNumber);
        if (accounts.isEmpty())
            throw new RuntimeException("No accounts found for CIF: " + cifNumber);

        for (Account acc : accounts) {
            acc.setStatus(AccountStatus.ACTIVE);
            acc.setUpdatedAt(LocalDateTime.now());
            if (acc instanceof SavingsAccount s)
                s.setChequeBookAvailable(true);
            if (acc instanceof CurrentAccount c) {
                c.setChequeBookAvailable(true);
                c.setHasOverdraftFacility(true);
            }
        }
        accountRepository.saveAll(accounts);
        return "Accounts for CIF " + cifNumber + " activated successfully.";
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
    public boolean existsByAccountNumber(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }
}
