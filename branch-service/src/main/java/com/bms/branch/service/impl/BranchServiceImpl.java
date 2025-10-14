package com.bms.branch.service.impl;

import com.bms.auth.exception.customException.ResourceNotFoundException;
import com.bms.branch.dto.request.BranchRequestDto;
import com.bms.branch.dto.request.RegisterRequest;
import com.bms.branch.dto.response.AddressResponseDto;
import com.bms.branch.dto.response.BranchResponseDto;
import com.bms.branch.dto.response.EmployeeDto;
import com.bms.branch.feign.EmployeeClient;
import com.bms.branch.model.*;
import com.bms.branch.repository.BranchEmployeeMappingRepository;
import com.bms.branch.repository.BranchRepository;
import com.bms.branch.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.rmi.server.LogStream.log;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchEmployeeMappingRepository mappingRepository;
    private final EmployeeClient employeeClient;

    @Override
    public BranchResponseDto createBranch(BranchRequestDto branchRequestDto) {
        Branch branch = Branch.builder()
                .branchCode(branchRequestDto.branchCode())
                .branchName(branchRequestDto.branchName())
                .ifscCode(branchRequestDto.ifscCode())
                .email(branchRequestDto.email())
                .contactNumber(branchRequestDto.contactNumber())
                .status(branchRequestDto.status())
                .openingDate(branchRequestDto.openingDate())
                .address(branchRequestDto.address() != null
                        ? new Address(
                        branchRequestDto.address().street(),
                        branchRequestDto.address().city(),
                        branchRequestDto.address().state(),
                        branchRequestDto.address().country(),
                        branchRequestDto.address().zipCode())
                        : null)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();

        Branch savedBranch = branchRepository.save(branch);

        return convertToResponseDto(savedBranch);
    }

    @Override
    public BranchResponseDto updateBranch(Long branchId, BranchRequestDto branchRequestDto) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

        branch.setBranchName(branchRequestDto.branchName());
        branch.setIfscCode(branchRequestDto.ifscCode());
        branch.setEmail(branchRequestDto.email());
        branch.setContactNumber(branchRequestDto.contactNumber());
        branch.setStatus(branchRequestDto.status());
        branch.setOpeningDate(branchRequestDto.openingDate());
        branch.setUpdatedAt(LocalDate.now());
        branch.setAddress(branchRequestDto.address() != null
                ? new Address(
                branchRequestDto.address().street(),
                branchRequestDto.address().city(),
                branchRequestDto.address().state(),
                branchRequestDto.address().country(),
                branchRequestDto.address().zipCode())
                : null);

        Branch updatedBranch = branchRepository.save(branch);
        return convertToResponseDto(updatedBranch);
    }

    @Override
    public void deleteBranch(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new RuntimeException("Branch not found with id: " + branchId);
        }
        branchRepository.deleteById(branchId);
    }

    @Override
    public BranchResponseDto getBranchById(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));
        return convertToResponseDto(branch);
    }

    @Override
    public List<BranchResponseDto> getAllBranches(boolean onlyActive) {
        List<Branch> branches = onlyActive
                ? branchRepository.findAll().stream().filter(Branch::isActive).collect(Collectors.toList())
                : branchRepository.findAll();

        return branches.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponseDto createAndAssignEmployee(Long branchId, RegisterRequest registerRequest) {
        try {
            Long newEmployeeId = employeeClient.createUser(registerRequest).getUserId();
            return addEmployeeToBranch(branchId, newEmployeeId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create employee in auth-service: " + e.getMessage());
        }
    }

    @Override
    public BranchResponseDto addEmployeeToBranch(Long branchId, Long employeeId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

        BranchEmployeeMapping mapping = BranchEmployeeMapping.builder()
                .branch(branch)
                .employeeId(employeeId)
                .assignedDate(LocalDate.now())
                .build();

        mappingRepository.save(mapping);
        branch.getEmployees().add(mapping);

        return convertToResponseDto(branch);
    }

    @Override
    public BranchResponseDto removeEmployeeFromBranch(Long branchId, Long employeeId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

        mappingRepository.deleteByBranchIdAndEmployeeId(branchId, employeeId);

        branch.setEmployees(
                branch.getEmployees().stream()
                        .filter(e -> !e.getEmployeeId().equals(employeeId))
                        .collect(Collectors.toSet())
        );

        return convertToResponseDto(branch);
    }

    @Override
    public List<Long> getBranchEmployees(Long branchId) {
        return mappingRepository.findByBranchId(branchId)
                .stream()
                .map(BranchEmployeeMapping::getEmployeeId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long branchId) {
        return branchRepository.existsById(branchId);
    }

    @Override
    public BranchResponseDto getBranchByCode(String branchCode) {
        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException("IFSC code must not be null or empty");
        }
        return branchRepository.findByBranchCode(branchCode)
                .map(this::convertToResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with code: " + branchCode));
    }

    @Override
    public BranchResponseDto getBranchByIfsc(String ifscCode) {
        if (ifscCode == null || ifscCode.isBlank()) {
            throw new IllegalArgumentException("IFSC code must not be null or empty");
        }
        return branchRepository.findByIfscCode(ifscCode)
                .map(this::convertToResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with IFSC code: " + ifscCode));
    }

    @Override
    public String toggleBranchStatus(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));

        branch.setStatus(!branch.getStatus());
        branchRepository.save(branch);
        return "Branch status toggled successfully";
    }

    @Override
    public List<EmployeeDto> getBranchEmployeeDetails(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));

        List<Long> employeeIds = mappingRepository.findEmployeeIdsByBranchId(branchId);

        return employeeIds.stream()
                .map(employeeClient::getEmployeeById)
                .collect(Collectors.toList());
    }
    private BranchResponseDto convertToResponseDto(Branch branch) {
        AddressResponseDto addressDto = null;
        if (branch.getAddress() != null) {
            addressDto = new AddressResponseDto(
                    branch.getAddress().getStreet(),
                    branch.getAddress().getCity(),
                    branch.getAddress().getState(),
                    branch.getAddress().getCountry(),
                    branch.getAddress().getPostalCode()
            );
        }

        List<EmployeeDto> employees = branch.getEmployees().stream()
                .map(e -> new EmployeeDto(e.getEmployeeId(),null,e.getAssignedDate(),null))
                .collect(Collectors.toList());

        return new BranchResponseDto(
                branch.getId(),
                branch.getBranchCode(),
                branch.getBranchName(),
                branch.getIfscCode(),
                branch.getEmail(),
                branch.getContactNumber(),
                branch.getStatus(),
                branch.getOpeningDate(),
                addressDto,
                employees,
                branch.getCreatedAt(),
                branch.getUpdatedAt(),
                branch.isActive()
        );
    }
}
