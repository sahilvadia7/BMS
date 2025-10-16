package com.bms.customer.services.impl;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.entities.Customer;
import com.bms.customer.entities.Kyc;
import com.bms.customer.entities.CustomerKycMapping;
import com.bms.customer.enums.KycStatus;
import com.bms.customer.enums.UserStatus;
import com.bms.customer.repositories.CustomerRepository;
import com.bms.customer.repositories.KycRepository;
import com.bms.customer.repositories.CustomerKycMappingRepository;
import com.bms.customer.services.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;
    private final CustomerRepository customerRepository;
    private final CustomerKycMappingRepository mappingRepository;

    private KycResponseDTO mapToKycResponse(Kyc kyc) {
        return new KycResponseDTO(
                kyc.getId(),
                kyc.getDocumentType(),
                kyc.getDocumentNumber(),
                kyc.getStatus(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt()
        );
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

    @Override
    public KycResponseDTO createKycDocument(KycRequestDTO requestDTO) {
        if (kycRepository.findByDocumentNumber(requestDTO.documentNumber()).isPresent()) {
            throw new RuntimeException("KYC document number already exists.");
        }

        Kyc kyc = Kyc.builder()
                .documentType(requestDTO.documentType())
                .documentNumber(requestDTO.documentNumber())
                .status(KycStatus.PENDING)
                .build();

        Kyc savedKyc = kycRepository.save(kyc);
        return mapToKycResponse(savedKyc);
    }

    @Override
    public KycResponseDTO getKycById(Long id) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC document not found for ID: " + id));
        return mapToKycResponse(kyc);
    }

    @Override
    public List<KycResponseDTO> getAllKyc() {
        return kycRepository.findAll().stream()
                .map(this::mapToKycResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC document not found for ID: " + id));

        if (!kyc.getDocumentNumber().equals(requestDTO.documentNumber()) &&
                kycRepository.findByDocumentNumber(requestDTO.documentNumber()).isPresent()) {
            throw new RuntimeException("Cannot update document number; new number already exists.");
        }

        kyc.setDocumentType(requestDTO.documentType());
        kyc.setDocumentNumber(requestDTO.documentNumber());

        Kyc updatedKyc = kycRepository.save(kyc);
        return mapToKycResponse(updatedKyc);
    }

    @Override
    public void deleteKyc(Long id) {
        if (!kycRepository.existsById(id)) {
            throw new RuntimeException("KYC document not found for ID: " + id);
        }
        kycRepository.deleteById(id);
    }

    @Override
    public CustomerResponseDTO verifyAndLinkKyc(Long customerId, Long kycId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found for ID: " + customerId));

        Kyc kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC document not found for ID: " + kycId));

        if (kyc.getStatus() != KycStatus.PENDING && kyc.getStatus() != KycStatus.VERIFIED) {
            throw new RuntimeException("KYC status cannot be linked. Current status: " + kyc.getStatus());
        }
        kyc.setStatus(KycStatus.VERIFIED);
        kycRepository.save(kyc);

        CustomerKycMapping mapping = CustomerKycMapping.builder()
                .customer(customer)
                .kyc(kyc)
                .isPrimary(true)
                .build();

        mappingRepository.save(mapping);

        customer.getKycDocuments().add(mapping);

        customer.setStatus(UserStatus.ACTIVE);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(customer);

        return mapToCustomerResponse(updatedCustomer);
    }
}