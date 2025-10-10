package com.bms.customer.services.impl;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.entities.Kyc;
import com.bms.customer.enums.KycStatus;
import com.bms.customer.exception.ResourceNotFoundException;
import com.bms.customer.repositories.KycRepository;
import com.bms.customer.services.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;

    private KycResponseDTO mapToResponse(Kyc kyc) {
        return new KycResponseDTO(
                kyc.getId(),
                kyc.getDocumentType(),
                kyc.getDocumentNumber(),
                kyc.getStatus(),
                kyc.getCreatedAt(),
                kyc.getUpdatedAt()
        );
    }

    @Override
    public KycResponseDTO createKyc(KycRequestDTO requestDTO) {
        // Check if KYC with same document type and number already exists
        Optional<Kyc> existingKyc = kycRepository.findByDocumentTypeAndDocumentNumber(
                requestDTO.documentType(),
                requestDTO.documentNumber()
        );

        if (existingKyc.isPresent()) {
            throw new ResourceNotFoundException(
                    "KYC already exists with Document Type: " + requestDTO.documentType()
                            + " and Document Number: " + requestDTO.documentNumber()
            );
        }

        // If not found, create a new KYC record
        Kyc kyc = Kyc.builder()
                .documentType(requestDTO.documentType())
                .documentNumber(requestDTO.documentNumber())
                .status(requestDTO.status() != null ? requestDTO.status() : KycStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Kyc saved = kycRepository.save(kyc);
        return mapToResponse(saved);
    }


    @Override
    public KycResponseDTO getKycById(Long id) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found with id: " + id));
        return mapToResponse(kyc);
    }

    @Override
    public List<KycResponseDTO> getAllKyc() {
        return kycRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC not found with id: " + id));

        kyc.setDocumentType(requestDTO.documentType());
        kyc.setDocumentNumber(requestDTO.documentNumber());
        kyc.setStatus(requestDTO.status() != null ? requestDTO.status() : kyc.getStatus());
        kyc.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(kycRepository.save(kyc));
    }

    @Override
    public void deleteKyc(Long id) {
        if (!kycRepository.existsById(id)) {
            throw new ResourceNotFoundException("KYC not found with id: " + id);
        }
        kycRepository.deleteById(id);
    }
}
