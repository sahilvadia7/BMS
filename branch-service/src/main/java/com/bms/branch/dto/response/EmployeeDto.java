package com.bms.branch.dto.response;

import java.time.LocalDate;

public record EmployeeDto(
        Long employeeId,
        String name,
        LocalDate assignedDate,
        String role
) {}