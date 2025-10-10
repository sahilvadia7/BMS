package com.bms.customer.services.impl;

import com.bms.customer.dtos.CustomerRequestDTO;
import com.bms.customer.dtos.CustomerResponseDTO;
import com.bms.customer.entities.Customer;
import com.bms.customer.entities.Kyc;
import com.bms.customer.enums.KycStatus;

import com.bms.customer.repositories.CustomerRepository;
import com.bms.customer.repositories.KycRepository;
import com.bms.customer.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final KycRepository kycRepository;

    private CustomerResponseDTO mapToResponse(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getUserId(),
                customer.getName(),
                customer.getAddress(),
                customer.getDob(),
                customer.getKycId(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {


        Kyc kyc;
        Optional<Kyc> existingKyc = kycRepository.findById(requestDTO.kycId());
        if (existingKyc.isPresent()) {
            kyc = existingKyc.get(); // use existing KYC
        } else {
            // If KYC not found → create default
            kyc = Kyc.builder()
                    .documentType("UNKNOWN")
                    .documentNumber("N/A")
                    .status(KycStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
//            kyc = kycRepository.save(kyc);
        }

        Customer customer = Customer.builder()
                .userId(requestDTO.userId())
                .name(requestDTO.name())
                .address(requestDTO.address())
                .dob(requestDTO.dob())
                .kycId(kyc.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }



    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToResponse(customer);
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setUserId(requestDTO.userId());
        customer.setName(requestDTO.name());
        customer.setAddress(requestDTO.address());
        customer.setDob(requestDTO.dob());
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) throw new RuntimeException("Customer not found");
        customerRepository.deleteById(id);
    }
}
