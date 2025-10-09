package com.bms.branch.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record BranchResponseDto(

        Long id,
        String branchCode,
        String branchName,
        String ifscCode,
        String email,
        String contactNumber,
        Boolean status,
        LocalDate openingDate,
        AddressResponseDto address,
        List<EmployeeDto> employees,
        LocalDate createdAt,
        LocalDate updatedAt,
        boolean isActive

) {}
