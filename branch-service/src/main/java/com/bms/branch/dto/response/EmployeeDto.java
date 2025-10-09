package com.bms.branch.dto.response;

public record EmployeeDto(
        Long employeeId,
        String name,
        String role
) {}