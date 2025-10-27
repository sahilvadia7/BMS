package com.bms.customer.services.impl;

import com.bms.customer.dtos.request.*;
import com.bms.customer.dtos.response.*;
import com.bms.customer.entities.Customer;
import com.bms.customer.enums.Roles;
import com.bms.customer.enums.UserStatus;
import com.bms.customer.exception.*;
import com.bms.customer.feign.NotificationClient;
import com.bms.customer.repositories.*;
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
    private final KycRepository kycRepository;
    private final CustomerKycMappingRepository mappingRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

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

        if(savedCustomer!=null) {
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
                .message("Registration successful. Your login credentials have been sent to your registered email. Please check your inbox and log in to continue the process.")
                .status(savedCustomer.getStatus().name())
                .build();
    }

    @Override
    public CustomerResponseDTO login(LoginRequest loginRequest) {
        Customer customer = customerRepository.findByEmail(loginRequest.getLoginId())
                .orElseGet(() -> customerRepository.findByPhoneNo(loginRequest.getLoginId())
                        .orElseGet(() -> customerRepository.findByCifNumber(loginRequest.getLoginId())
                                .orElseThrow(() -> new CustomerNotFoundException("User not found or invalid login ID."))));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials. Please try again.");
        }

        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not active. Status: " + customer.getStatus().name() + ". Please complete KYC.");
        }

        return mapToResponse(customer);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        // Example placeholder for logout logic (if JWT is used)
    }

    @Override
    public void changePassword(ChangePwdDTO changePwdDTO) {
        Customer customer = customerRepository.findByCifNumber(changePwdDTO.getCifNumber())
                .orElseGet(() -> customerRepository.findByEmail(changePwdDTO.getEmail())
                        .orElseGet(() -> customerRepository.findByPhoneNo(changePwdDTO.getPhoneNo())
                                .orElseThrow(() -> new CustomerNotFoundException("User not found."))));

        if (!passwordEncoder.matches(changePwdDTO.getCurrentPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
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

    private String generateUniqueCifNumber(){
        String cif;
        int attempts = 0;
        do{
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
        }while (customerRepository.findByCifNumber(cif).isPresent());
        return cif;
    }

}
