package com.bms.branch.service.impl;

import com.bms.branch.dto.request.BranchRequestDto;
import com.bms.branch.dto.response.AddressResponseDto;
import com.bms.branch.dto.response.BranchResponseDto;
import com.bms.branch.dto.response.EmployeeDto;
import com.bms.branch.model.*;
import com.bms.branch.repository.BranchEmployeeMappingRepository;
import com.bms.branch.repository.BranchRepository;
import com.bms.branch.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchEmployeeMappingRepository mappingRepository;

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
                .address(branchRequestDto.address() != null ?
                        new Address(branchRequestDto.address().street(),
                                branchRequestDto.address().city(),
                                branchRequestDto.address().state(),
                                branchRequestDto.address().country(),
                                branchRequestDto.address().zipCode()) : null)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .isActive(true)
                .build();

        branchRepository.save(branch);

        return BranchResponseDto.builder()
                .branchCode(branchRequestDto.branchCode())
                .build();
    }

    @Override
    public BranchResponseDto updateBranch(Long branchId, BranchRequestDto branchRequestDto) {
        return null;
    }

    @Override
    public void deleteBranch(Long branchId) {

    }

    @Override
    public BranchResponseDto getBranchById(Long branchId) {
        return null;
    }

    @Override
    public List<BranchResponseDto> getAllBranches(boolean onlyActive) {
        return List.of();
    }

    @Override
    public BranchResponseDto addEmployeeToBranch(Long branchId, Long employeeId) {
        return null;
    }

    @Override
    public BranchResponseDto removeEmployeeFromBranch(Long branchId, Long employeeId) {
        return null;
    }

    @Override
    public List<Long> getBranchEmployees(Long branchId) {
        return List.of();
    }

    @Override
    public boolean existsById(Long branchId) {
        return false;
    }


}

