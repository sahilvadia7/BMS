package com.bms.customer.services.impl;

import com.bms.customer.dtos.request.ChangePwdDTO;
import com.bms.customer.dtos.request.CustomerRegisterRequestDTO;
import com.bms.customer.dtos.request.LoginRequest;
import com.bms.customer.dtos.request.LogoutRequest;
import com.bms.customer.dtos.response.CustomerKycMappingDTO;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.entities.Customer;
import com.bms.customer.enums.Roles;
import com.bms.customer.enums.UserStatus;
import com.bms.customer.repositories.CustomerRepository;
import com.bms.customer.repositories.KycRepository;
import com.bms.customer.repositories.CustomerKycMappingRepository;
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

    @Override
    public CustomerRegistrationResponseDTO registerCustomer(CustomerRegisterRequestDTO requestDTO) {
        if (customerRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }
        if (customerRepository.findByPhoneNo(requestDTO.getPhoneNo()).isPresent()) {
            throw new RuntimeException("Phone number is already registered");
        }

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
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerRegistrationResponseDTO.builder()
                .customerId(savedCustomer.getCustomerId())
                .cifNumber(savedCustomer.getCifNumber())
                .message("Registration successful. Please proceed with KYC verification to activate your account.")
                .status(savedCustomer.getStatus().name())
                .build();
    }

    @Override
    public CustomerResponseDTO login(LoginRequest loginRequest) {
        Customer customer = customerRepository.findByEmail(loginRequest.getLoginId())
                .orElseGet(() -> customerRepository.findByPhoneNo(loginRequest.getLoginId())
                        .orElseGet(() -> customerRepository.findByCifNumber(loginRequest.getLoginId())
                                .orElseThrow(() -> new RuntimeException("User not found or invalid login ID."))));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Account is not active. Status: " + customer.getStatus().name() + ". Please complete KYC.");
        }

        return mapToResponse(customer);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
    }

    @Override
    public void changePassword(ChangePwdDTO changePwdDTO) {
        Customer customer = customerRepository.findByCifNumber(changePwdDTO.getCifNumber())
                .orElseGet(() -> customerRepository.findByEmail(changePwdDTO.getEmail())
                        .orElseGet(() -> customerRepository.findByPhoneNo(changePwdDTO.getPhoneNo())
                                .orElseThrow(() -> new RuntimeException("User not found"))));

        if (!passwordEncoder.matches(changePwdDTO.getCurrentPassword(), customer.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        customer.setPassword(passwordEncoder.encode(changePwdDTO.getNewPassword()));
        customerRepository.save(customer);

    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findWithKycByCustomerId(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDTO getCustomerByCifNumber(String cifNumber) {
        Customer customer = customerRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found for CIF: " + cifNumber));
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
}
