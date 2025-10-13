package com.bms.branch.service;

import com.bms.branch.dto.request.BranchRequestDto;
import com.bms.branch.dto.request.RegisterRequest;
import com.bms.branch.dto.response.BranchResponseDto;
import com.bms.branch.dto.response.EmployeeDto;

import java.util.List;

public interface BranchService {

    public BranchResponseDto createBranch(BranchRequestDto branchRequestDto);
    public BranchResponseDto updateBranch(Long branchId, BranchRequestDto branchRequestDto);
    public void deleteBranch(Long branchId);
    public BranchResponseDto getBranchById(Long branchId);
    public List<BranchResponseDto> getAllBranches(boolean onlyActive);
    public BranchResponseDto createAndAssignEmployee(Long branchId, RegisterRequest registerRequest);
    public BranchResponseDto addEmployeeToBranch(Long branchId, Long employeeId);
    public BranchResponseDto removeEmployeeFromBranch(Long branchId, Long employeeId);
    public List<Long> getBranchEmployees(Long branchId);
    public boolean existsById(Long branchId);

    public BranchResponseDto getBranchByCode(String branchCode);
    public BranchResponseDto getBranchByIfsc(String ifscCode);

    public String toggleBranchStatus(Long branchId);

    public List<EmployeeDto> getBranchEmployeeDetails(Long branchId);
}
