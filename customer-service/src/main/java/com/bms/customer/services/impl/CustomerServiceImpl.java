package com.bms.customer.services.impl;

import com.bms.customer.dtos.request.*;
import com.bms.customer.dtos.response.*;
import com.bms.customer.entities.Customer;
import com.bms.customer.enums.Roles;
import com.bms.customer.enums.UserStatus;
import com.bms.customer.exception.*;
import com.bms.customer.feign.NotificationClient;
import com.bms.customer.repositories.CustomerRepository;
import com.bms.customer.security.JwtService;
import com.bms.customer.services.CustomerService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    private final JwtService jwtService;

    // ✅ 1️⃣ Register new customer
    @Override
    public CustomerRegistrationResponseDTO registerCustomer(CustomerRegisterRequestDTO requestDTO) {
        String cifNumber = generateUniqueCifNumber();

        Customer customer = Customer.builder()
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .phoneNo(requestDTO.getPhoneNo())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .address(requestDTO.getAddress())
                .dob(LocalDate.parse(requestDTO.getDob()))
                .gender(requestDTO.getGender().orElse(null))
                .role(Roles.CUSTOMER)
                .status(UserStatus.PENDING)
                .cifNumber(cifNumber)
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        // Send email notification
        if (savedCustomer != null) {
            EmailRequestDTO emailRequestDTO = EmailRequestDTO.builder()
                    .toEmail(savedCustomer.getEmail())
                    .customerName(savedCustomer.getFirstName())
                    .cifId(savedCustomer.getCifNumber())
                    .build();

            notificationClient.sendRegistrationEmail(emailRequestDTO);
        }

        return CustomerRegistrationResponseDTO.builder()
                .customerId(savedCustomer.getCustomerId())
                .cifNumber(savedCustomer.getCifNumber())
                .message("Registration successful. Please check your email to continue.")
                .status(savedCustomer.getStatus().name())
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequest loginRequest) {
        Customer customer = customerRepository.findByCifNumber(loginRequest.getLoginId())
                                .orElseThrow(() -> new CustomerNotFoundException("User not found or invalid login ID."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials. Please try again.");
        }

        // Optional check
//        if (customer.getStatus() != UserStatus.ACTIVE) {
//            throw new AccountNotActiveException("Account not active. Please complete KYC.");
//        if (customer.getStatus() != UserStatus.ACTIVE) {
//            throw new AccountNotActiveException("Account is not active. Status: " + customer.getStatus().name() + ". Please complete KYC.");
//        }

        String accessToken = jwtService.generateToken(customer.getCifNumber());
        String refreshToken = jwtService.generateRefreshToken(customer.getCifNumber());

        TokenResponseDTO tokenResponse = TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();

        CustomerResponseDTO customerResponse = mapToResponse(customer);

        return AuthResponseDTO.builder()
                .message("Login successful")
                .tokens(tokenResponse)
                .customer(customerResponse)
                .build();
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        // JWT logout = frontend just discards token
    }

    @Override
    public void changePassword(ChangePwdDTO changePwdDTO) {
        Customer customer = customerRepository.findByCifNumber(changePwdDTO.getCifNumber())
                .orElseThrow(() -> new CustomerNotFoundException("User not found with the provided CIF number."));

        if (!passwordEncoder.matches(changePwdDTO.getCurrentPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("The current password provided is incorrect.");
        }

        customer.setPassword(passwordEncoder.encode(changePwdDTO.getNewPassword()));
        customerRepository.save(customer);
    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findWithKycByCustomerId(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for ID: " + id));
        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDTO getCustomerByCifNumber(String cifNumber) {
        Customer customer = customerRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for CIF: " + cifNumber));
        return mapToResponse(customer);
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ helper: map entity to DTO
    private CustomerResponseDTO mapToResponse(Customer customer) {
        Set<CustomerKycMappingDTO> kycMappings = customer.getKycDocuments().stream()
                .map(mapping -> CustomerKycMappingDTO.builder()
                        .kycId(mapping.getKyc().getId())
                        .documentType(mapping.getKyc().getDocumentType())
                        .documentNumber(mapping.getKyc().getDocumentNumber())
                        .documentStatus(mapping.getKyc().getStatus())
                        .isPrimary(mapping.isPrimary())
                        .approvalDate(mapping.getVerificationDate())
                        .build())
                .collect(Collectors.toSet());

        return CustomerResponseDTO.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNo(customer.getPhoneNo())
                .role(customer.getRole())
                .status(customer.getStatus())
                .gender(customer.getGender())
                .cifNumber(customer.getCifNumber())
                .address(customer.getAddress())
                .dob(customer.getDob())
                .kycDocuments(kycMappings)
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private String generateUniqueCifNumber() {
        String cif;
        int attempts = 0;
        do {
            long nano = System.nanoTime();
            int random = (int) (Math.random() * 1000);
            cif = "CIF" + nano + random;
            if (cif.length() > 20) {
                cif = cif.substring(0, 20);
            }
            attempts++;
            if (attempts > 5) {
                throw new IllegalStateException("Failed to generate unique CIF after multiple attempts");
            }
        } while (customerRepository.findByCifNumber(cif).isPresent());
        return cif;
    }
}
