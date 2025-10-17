package com.bms.customer.services;

import com.bms.customer.dtos.request.ChangePwdDTO;
import com.bms.customer.dtos.request.CustomerRegisterRequestDTO;
import com.bms.customer.dtos.request.LoginRequest;
import com.bms.customer.dtos.request.LogoutRequest;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import java.util.List;

public interface CustomerService {

    CustomerRegistrationResponseDTO registerCustomer(CustomerRegisterRequestDTO requestDTO);

    CustomerResponseDTO login(LoginRequest loginRequest);

    void logout(LogoutRequest logoutRequest);

    void changePassword(ChangePwdDTO changePwdDTO);

    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO getCustomerByCifNumber(String cifNumber);

    List<CustomerResponseDTO> getAllCustomers();
}
