package com.bms.customer.controller;

import com.bms.customer.dtos.CustomerRequestDTO;
import com.bms.customer.dtos.CustomerResponseDTO;
import com.bms.customer.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer APIs", description = "CRUD operations for customers")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Create a new customer")
    @PostMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @PathVariable Long id, @RequestBody CustomerRequestDTO requestDTO) {
        return new ResponseEntity<>(customerService.createCustomer(id,requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Get all customers")
    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @Operation(summary = "Update customer by ID")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO requestDTO) {
        return ResponseEntity.ok(customerService.updateCustomer(id, requestDTO));
    }

    @Operation(summary = "Delete customer by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check if customer exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> customerExists(@PathVariable Long id) {
        boolean exists = customerService.existsById(id);
        return ResponseEntity.ok(exists);
    }

}
