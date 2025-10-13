package com.bms.branch.feign;

import com.bms.branch.dto.request.RegisterRequest;
import com.bms.branch.dto.response.EmployeeDto;
import com.bms.branch.dto.response.RegisterResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/api/v1/auth")
public interface EmployeeClient {

    @GetMapping("/employees/{employeeId}")
    EmployeeDto getEmployeeById(@PathVariable("employeeId") Long employeeId);

    @PostMapping("/register")
    RegisterResponse createUser(@RequestBody RegisterRequest registerRequest);
}