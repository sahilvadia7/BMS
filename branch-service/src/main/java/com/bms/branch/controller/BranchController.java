package com.bms.branch.controller;

import com.bms.branch.dto.request.BranchRequestDto;
import com.bms.branch.dto.request.RegisterRequest;
import com.bms.branch.dto.response.BranchResponseDto;
import com.bms.branch.dto.response.EmployeeDto;
import com.bms.branch.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "APIs for managing branches and their employees")
public class BranchController {

    private final BranchService branchService;

    @Operation(
            summary = "Create a new branch",
            description = "Creates a new branch with name, address, and status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Branch created successfully",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    public ResponseEntity<BranchResponseDto> createBranch(
            @RequestBody @Valid BranchRequestDto requestDto) {
        BranchResponseDto response = branchService.createBranch(requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update an existing branch",
            description = "Updates branch details by branch ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Branch updated successfully",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Branch not found")
            }
    )
    @PutMapping("/{branchId}")
    public ResponseEntity<BranchResponseDto> updateBranch(
            @Parameter(description = "Branch ID") @PathVariable Long branchId,
            @RequestBody @Valid BranchRequestDto requestDto) {
        BranchResponseDto response = branchService.updateBranch(branchId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete branch by ID",
            description = "Removes a branch from the system by its ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Branch deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Branch not found")
            }
    )
    @DeleteMapping("/{branchId}")
    public ResponseEntity<Void> deleteBranch(
            @Parameter(description = "Branch ID") @PathVariable Long branchId) {
        branchService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get branch by ID",
            description = "Fetch branch details by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Branch found",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Branch not found")
            }
    )
    @GetMapping("/{branchId}")
    public ResponseEntity<BranchResponseDto> getBranchById(
            @Parameter(description = "Branch ID") @PathVariable Long branchId) {
        BranchResponseDto response = branchService.getBranchById(branchId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all branches",
            description = "Retrieves all branches, optionally filtering only active ones",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of branches",
                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<BranchResponseDto>> getAllBranches(
            @Parameter(description = "Whether to return only active branches")
            @RequestParam(defaultValue = "true") boolean onlyActive) {
        List<BranchResponseDto> response = branchService.getAllBranches(onlyActive);
        return ResponseEntity.ok(response);
    }
//
//    @PostMapping("/{branchId}/employees")
//    public ResponseEntity<BranchResponseDto> createAndAssignEmployee(
//            @PathVariable Long branchId,
//            @RequestBody RegisterRequest registerRequest) {
//
//        BranchResponseDto updatedBranch = branchService.createAndAssignEmployee(branchId, registerRequest);
//        return ResponseEntity.ok(updatedBranch);
//    }
//
//    @Operation(
//            summary = "Add employee to a branch",
//            description = "Assigns an employee to a branch by their IDs",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Employee added successfully",
//                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
//                    @ApiResponse(responseCode = "404", description = "Branch not found or employee invalid")
//            }
//    )
//    @PostMapping("/{branchId}/employees/{employeeId}")
//    public ResponseEntity<BranchResponseDto> addEmployeeToBranch(
//            @Parameter(description = "Branch ID") @PathVariable Long branchId,
//            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
//        BranchResponseDto response = branchService.addEmployeeToBranch(branchId, employeeId);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(
//            summary = "Remove employee from branch",
//            description = "Removes an assigned employee from a branch",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Employee removed successfully",
//                            content = @Content(schema = @Schema(implementation = BranchResponseDto.class))),
//                    @ApiResponse(responseCode = "404", description = "Branch or employee not found")
//            }
//    )
//    @DeleteMapping("/{branchId}/employees/{employeeId}")
//    public ResponseEntity<BranchResponseDto> removeEmployeeFromBranch(
//            @Parameter(description = "Branch ID") @PathVariable Long branchId,
//            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
//        BranchResponseDto response = branchService.removeEmployeeFromBranch(branchId, employeeId);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(
//            summary = "Get employees of a branch",
//            description = "Returns all employee IDs assigned to a given branch",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "List of employee IDs",
//                            content = @Content(schema = @Schema(implementation = Long.class))),
//                    @ApiResponse(responseCode = "404", description = "Branch not found")
//            }
//    )
//    @GetMapping("/{branchId}/employees")
//    public ResponseEntity<List<Long>> getBranchEmployees(
//            @Parameter(description = "Branch ID") @PathVariable Long branchId) {
//        List<Long> employees = branchService.getBranchEmployees(branchId);
//        return ResponseEntity.ok(employees);
//    }

    @Operation(
            summary = "Check if branch exists",
            description = "Verifies if a branch exists by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Boolean result (true/false)")
            }
    )
    @GetMapping("/{branchId}/exists")
    public ResponseEntity<Boolean> existsById(
            @Parameter(description = "Branch ID") @PathVariable Long branchId) {
        boolean exists = branchService.existsById(branchId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/code/{branchCode}")
    public ResponseEntity<BranchResponseDto> getBranchByCode(@PathVariable String branchCode) {
        BranchResponseDto response = branchService.getBranchByCode(branchCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ifsc/{ifscCode}")
    public ResponseEntity<BranchResponseDto> getBranchByIfsc(@PathVariable String ifscCode) {
        BranchResponseDto response = branchService.getBranchByIfsc(ifscCode);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{branchId}/toggle-status")
    public ResponseEntity<String> toggleBranchStatus(@PathVariable Long branchId) {
        branchService.toggleBranchStatus(branchId);
        return ResponseEntity.ok("Branch status toggled successfully");
    }
//    @GetMapping("/{branchId}/employees/details")
//    public ResponseEntity<List<EmployeeDto>> getBranchEmployeeDetails(@PathVariable Long branchId) {
//        List<EmployeeDto> employees = branchService.getBranchEmployeeDetails(branchId);
//        return ResponseEntity.ok(employees);
//    }

}
