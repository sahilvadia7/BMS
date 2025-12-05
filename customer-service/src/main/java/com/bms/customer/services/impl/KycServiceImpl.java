package com.bms.customer.services.impl;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.entities.*;
import com.bms.customer.enums.*;
import com.bms.customer.exception.BadRequestException;
import com.bms.customer.exception.ResourceNotFoundException;
import com.bms.customer.feign.AccountClient;
import com.bms.customer.repositories.*;
import com.bms.customer.services.KycService;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;
    private final CustomerRepository customerRepository;
    private final CustomerKycMappingRepository mappingRepository;
    private final AccountClient accountClient;

    public KycServiceImpl(KycRepository kycRepository,
            CustomerRepository customerRepository,
            CustomerKycMappingRepository mappingRepository,
            AccountClient accountClient) {
        this.kycRepository = kycRepository;
        this.customerRepository = customerRepository;
        this.mappingRepository = mappingRepository;
        this.accountClient = accountClient;
    }

    private KycResponseDTO mapToKycResponse(Kyc kyc) {
        return new KycResponseDTO(
                kyc.getId(),
                kyc.getDocumentType(),
                kyc.getDocumentNumber(),
                kyc.getDocumentUrl(),
                kyc.getDocumentFileName(),
                kyc.getStatus(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt());
    }

    private CustomerResponseDTO mapToCustomerResponse(Customer customer) {
        return CustomerResponseDTO.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .cifNumber(customer.getCifNumber())
                .status(customer.getStatus())
                .build();
    }

    public KycResponseDTO createKycDocument(KycRequestDTO requestDTO) {
        if (kycRepository.findByDocumentTypeAndDocumentNumber(
                requestDTO.documentType(),
                requestDTO.documentNumber()).isPresent()) {
            throw new BadRequestException(
                    "A KYC document of type '" + requestDTO.documentType() +
                            "' with this number already exists.");
        }

        Kyc kyc = Kyc.builder()
                .documentType(requestDTO.documentType())
                .documentNumber(requestDTO.documentNumber())
                .status(KycStatus.PENDING)
                .build();

        return mapToKycResponse(kycRepository.save(kyc));
    }

    @Override
    public KycResponseDTO getKycById(Long id) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found for ID: " + id));
        return mapToKycResponse(kyc);
    }

    @Override
    public List<KycResponseDTO> getAllKyc() {
        return kycRepository.findAll()
                .stream()
                .map(this::mapToKycResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO) {
        Kyc existingKyc = kycRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found for ID: " + id));

        if (!existingKyc.getDocumentType().equals(requestDTO.documentType()) ||
                !existingKyc.getDocumentNumber().equals(requestDTO.documentNumber())) {

            if (kycRepository.findByDocumentTypeAndDocumentNumber(
                    requestDTO.documentType(),
                    requestDTO.documentNumber()).isPresent()) {
                throw new BadRequestException(
                        "Another KYC document with this type and number already exists.");
            }
        }

        existingKyc.setDocumentType(requestDTO.documentType());
        existingKyc.setDocumentNumber(requestDTO.documentNumber());
        return mapToKycResponse(kycRepository.save(existingKyc));
    }

    @Override
    public void deleteKyc(Long id) {
        if (!kycRepository.existsById(id)) {
            throw new ResourceNotFoundException("KYC document not found for ID: " + id);
        }
        kycRepository.deleteById(id);
    }

    @Override
    public CustomerResponseDTO linkKycToCustomer(Long customerId, Long kycId) {
        return null;
    }

    // branch manager only
    @Override
    @Transactional
    public CustomerResponseDTO approveKyc(Long kycId, String approvedBy) {
        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found"));

        if (kyc.getStatus() != KycStatus.PENDING) {
            throw new BadRequestException("Only PENDING KYC can be approved.");
        }

        Customer customer = customerRepository.findCustomerByKycId(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("No customer linked to this KYC"));

        // 1. Approve the KYC
        kyc.setStatus(KycStatus.APPROVED);
        kycRepository.save(kyc);

        // 2. Activate the customer
        customer.setStatus(UserStatus.ACTIVE);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        // 3. Trigger Account Activation via Feign
        try {
            accountClient.activateAccountByCif(customer.getCifNumber());
        } catch (Exception e) {
            // Optional: handle gracefully if Account Service is down
            throw new BadRequestException("KYC approved, but failed to activate account: " + e.getMessage());
        }

        return mapToCustomerResponse(customer);
    }

    // branch manager only
    @Override
    @Transactional
    public void rejectKyc(Long kycId, String reason) {
        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found"));

        kyc.setStatus(KycStatus.REJECTED);
        kycRepository.save(kyc);

        Customer customer = customerRepository.findCustomerByKycId(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("No customer linked to this KYC"));

        customer.setStatus(UserStatus.REJECTED);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    public CustomerResponseDTO verifyAndLinkKyc(Long customerId, Long kycId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for ID: " + customerId));

        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found for ID: " + kycId));

        if (kyc.getStatus() == KycStatus.REJECTED) {
            throw new BadRequestException("Rejected KYC cannot be linked.");
        }

        Optional<Customer> existingOwner = customerRepository.findCustomerByKycId(kycId);
        if (existingOwner.isPresent() && !existingOwner.get().getCustomerId().equals(customerId)) {
            throw new BadRequestException(
                    "This KYC document is already linked to another customer (CIF: " +
                            existingOwner.get().getCifNumber() + ").");
        }

        Optional<Customer> duplicateCustomer = customerRepository.findCustomerByKycDocument(
                kyc.getDocumentType(),
                kyc.getDocumentNumber());
        if (duplicateCustomer.isPresent() && !duplicateCustomer.get().getCustomerId().equals(customerId)) {
            throw new BadRequestException(
                    "A customer with this " + kyc.getDocumentType() +
                            " already exists (CIF: " + duplicateCustomer.get().getCifNumber() + ").");
        }

        kyc.setStatus(KycStatus.PENDING);
        kycRepository.save(kyc);

        mappingRepository.deleteByCustomerId(customerId);

        CustomerKycMapping mapping = CustomerKycMapping.builder()
                .customer(customer)
                .kyc(kyc)
                .isPrimary(true)
                .build();

        mappingRepository.save(mapping);

        customer.setStatus(UserStatus.PENDING);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return mapToCustomerResponse(customer);
    }

    @Override
    public KycResponseDTO getKycByCustomerId(Long customerId) {
        Optional<CustomerKycMapping> mapping = mappingRepository.findByCustomer_CustomerId(customerId);

        if (mapping.isEmpty()) {
            return null;
        }

        Kyc kyc = mapping.get().getKyc();

        return new KycResponseDTO(
                kyc.getId(),
                kyc.getDocumentType(),
                kyc.getDocumentNumber(),
                kyc.getDocumentUrl(),
                kyc.getDocumentFileName(),
                kyc.getStatus(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt());
    }

    @Override
    @Transactional
    public boolean existsByCustomer_CustomerId(Long customerId) {
        return mappingRepository.existsByCustomerId(customerId);
    }

    @Override
    public Long findKycIdByCustomerId(Long customerId) {
        return mappingRepository.findKycIdByCustomerId(customerId)
                .orElse(0L); // return 0 if not found
    }

    @Override
    @Transactional
    public KycResponseDTO uploadKycForCustomer(Long customerId, KycRequestDTO requestDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Account is already active. KYC cannot be re-uploaded.");
        }

//        if (kycRepository.findByDocumentTypeAndDocumentNumber(
//                requestDTO.documentType(),
//                requestDTO.documentNumber()).isPresent()) {
//            throw new BadRequestException(
//                    "This " + requestDTO.documentType() + " is already registered to another customer.");
//        }

        Kyc kyc = Kyc.builder()
                .documentType(requestDTO.documentType())
                .documentNumber(requestDTO.documentNumber())
                .documentUrl(requestDTO.documentUrl())
                .documentFileName(requestDTO.documentFileName())
                .status(KycStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        Kyc savedKyc = kycRepository.save(kyc);

        CustomerKycMapping mapping = CustomerKycMapping.builder()
                .customer(customer)
                .kyc(savedKyc)
                .isPrimary("PAN".equals(requestDTO.documentType()))
                .build();
        mappingRepository.save(mapping);

        customer.setStatus(UserStatus.PENDING);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return mapToKycResponse(savedKyc);
    }

    @Override
    public List<KycResponseDTO> getAllKycStatusByCustomerId(Long customerId) {

        List<Kyc> kycs = mappingRepository.findAllKycStatusByCustomerId(customerId);

        if (kycs.isEmpty()) {
            throw new ResourceNotFoundException("No KYC documents found for customer: " + customerId);
        }

        return kycs.stream()
                .map(k -> KycResponseDTO.builder()
                        .id(k.getId())
                        .documentType(k.getDocumentType())
                        .documentNumber(k.getDocumentNumber())
                        .documentUrl(k.getDocumentUrl())
                        .documentFileName(k.getDocumentFileName())
                        .status(k.getStatus())
                        .build())
                .toList();
    }


}