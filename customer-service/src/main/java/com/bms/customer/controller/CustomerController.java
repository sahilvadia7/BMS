package com.bms.customer.controller;

import com.bms.customer.dtos.request.ChangePwdDTO;
import com.bms.customer.dtos.request.CustomerRegisterRequestDTO;
import com.bms.customer.dtos.request.LoginRequest;
import com.bms.customer.dtos.request.LogoutRequest;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Register a new customer")
    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponseDTO> register(@Valid @RequestBody CustomerRegisterRequestDTO dto) {
        CustomerRegistrationResponseDTO response = customerService.registerCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Customer login")
    @PostMapping("/login")
    public ResponseEntity<CustomerResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        CustomerResponseDTO response = customerService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout customer")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        customerService.logout(logoutRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Change customer password")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePwdDTO changePwdDTO) {
        customerService.changePassword(changePwdDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Get customer by CIF number")
    @GetMapping("/cif/{cifNumber}")
    public ResponseEntity<CustomerResponseDTO> getByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(customerService.getCustomerByCifNumber(cifNumber));
    }

    @Operation(summary = "Get all customers")
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}
