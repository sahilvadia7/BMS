package com.bms.customer.controller;

import com.bms.customer.dtos.request.*;
import com.bms.customer.dtos.response.AuthResponseDTO;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerDetailsResponseDTO;
import com.bms.customer.security.JwtService;
import com.bms.customer.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Customer Management", description = "Endpoints for managing customer profiles and authentication")
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final JwtService jwtService;

    public CustomerController(CustomerService customerService, JwtService jwtService) {
        this.customerService = customerService;
        this.jwtService = jwtService;
    }

    @GetMapping("/health")
    public String greet() {
        log.info("customer service up and running");
        return "customer service up and running";
    }

    @Operation(summary = "Register a new customer", description = "Access: Public")
    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponseDTO> register(
            @Valid @RequestBody CustomerRegisterRequestDTO dto) {
        CustomerRegistrationResponseDTO response = customerService.registerCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Customer login", description = "Access: Public")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponseDTO response = customerService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout customer", description = "Access: Customer")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        customerService.logout(logoutRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Change customer password", description = "Access: Customer")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePwdDTO changePwdDTO) {
        customerService.changePassword(changePwdDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get customer by ID", description = "Access: Admin, Customer")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailsResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Get customer by CIF number", description = "Access: Admin, Customer")
    @GetMapping("/cif/{cifNumber}")
    public ResponseEntity<CustomerDetailsResponseDTO> getByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(customerService.getCustomerByCifNumber(cifNumber));
    }

    @Operation(summary = "Get limited customer info by CIF", description = "Access: Public/Internal")
    @GetMapping("/info/{cifNumber}")
    public ResponseEntity<Map<String, Object>> getLimitedInfoByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(customerService.getLimitedCustomerInfo(cifNumber));
    }

    @Operation(summary = "Get all customers", description = "Access: Admin")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerDetailsResponseDTO>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String cifNumber = jwtService.extractCifNumber(refreshToken);
        if (jwtService.isTokenValid(refreshToken,
                org.springframework.security.core.userdetails.User
                        .withUsername(cifNumber)
                        .password("")
                        .authorities("ROLE_USER")
                        .build())) {

            String newAccessToken = jwtService.generateToken(cifNumber);
            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", refreshToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired refresh token"));
    }

}
