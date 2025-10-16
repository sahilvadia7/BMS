package com.bms.customer.services;

import com.bms.customer.dtos.request.ChangePwdDTO;
import com.bms.customer.dtos.request.CustomerRegisterRequestDTO;
import com.bms.customer.dtos.request.LoginRequest;
import com.bms.customer.dtos.request.LogoutRequest;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import java.util.List;
import java.util.Map;

public interface CustomerService {

    CustomerRegistrationResponseDTO registerCustomer(CustomerRegisterRequestDTO requestDTO);

    CustomerResponseDTO login(LoginRequest loginRequest);

    Map<String, String> logout(LogoutRequest logoutRequest);

    Map<String, String> changePassword(ChangePwdDTO changePwdDTO);

    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO getCustomerByCifNumber(String cifNumber);

    List<CustomerResponseDTO> getAllCustomers();
}