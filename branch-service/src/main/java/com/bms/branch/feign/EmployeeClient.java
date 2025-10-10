package com.bms.branch.feign;

import com.bms.branch.dto.response.EmployeeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface EmployeeClient {

    @GetMapping("/api/v1/auth/employees/{employeeId}")
    EmployeeDto getEmployeeById(@PathVariable("employeeId") Long employeeId);
}
