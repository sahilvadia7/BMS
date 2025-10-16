package com.bms.customer.controller;

import com.bms.customer.dtos.request.ChangePwdDTO;
import com.bms.customer.dtos.request.CustomerRegisterRequestDTO;
import com.bms.customer.dtos.request.LoginRequest;
import com.bms.customer.dtos.request.LogoutRequest;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer APIs", description = "Endpoints for customer registration, authentication, and profile management")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Register a new customer")
    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponseDTO> registerCustomer(@Valid @RequestBody CustomerRegisterRequestDTO requestDTO) {
        return new ResponseEntity<>(customerService.registerCustomer(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Login a customer")
    @PostMapping("/login")
    public ResponseEntity<CustomerResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(customerService.login(loginRequest));
    }

    @Operation(summary = "Logout a customer")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        return ResponseEntity.ok(customerService.logout(logoutRequest));
    }

    @Operation(summary = "Change a customer's password")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePwdDTO changePwdDTO) {
        return ResponseEntity.ok(customerService.changePassword(changePwdDTO));
    }

    @Operation(summary = "Get a customer by internal ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Get customer by CIF Number (For internal service use)")
    @GetMapping("/cif/{cifNumber}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByCifNumber(@PathVariable String cifNumber) {
        return ResponseEntity.ok(customerService.getCustomerByCifNumber(cifNumber));
    }

    @Operation(summary = "Get all customers")
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}