package com.bms.customer.controller;

import com.bms.customer.dtos.request.*;
import com.bms.customer.dtos.response.AuthResponseDTO;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.entities.Customer;
import com.bms.customer.security.JwtService;
import com.bms.customer.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final JwtService jwtService;


    @GetMapping("/greet")
    public String greet(){
        log.info("greet called it here in customer service");
        return "Greetings from Spring Boot!";
    }

    @Operation(summary = "Register a new customer")
    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponseDTO> register(@Valid @RequestBody CustomerRegisterRequestDTO dto) {
        CustomerRegistrationResponseDTO response = customerService.registerCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Customer login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponseDTO response = customerService.login(loginRequest);
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

    @Operation(summary = "Get limited customer info by CIF")
    @GetMapping("/info/{cifNumber}")
    public ResponseEntity<Map<String, Object>> getLimitedInfoByCif(@PathVariable String cifNumber) {
        return ResponseEntity.ok(customerService.getLimitedCustomerInfo(cifNumber));
    }

    @Operation(summary = "Get all customers")
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAll() {
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
                    "refreshToken", refreshToken
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired refresh token"));
    }

}
