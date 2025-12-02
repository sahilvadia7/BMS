package com.bms.customer.services;

import com.bms.customer.dtos.request.ChangePwdDTO;
import com.bms.customer.dtos.request.CustomerRegisterRequestDTO;
import com.bms.customer.dtos.request.LoginRequest;
import com.bms.customer.dtos.request.LogoutRequest;
import com.bms.customer.dtos.response.AuthResponseDTO;
import com.bms.customer.dtos.response.CustomerRegistrationResponseDTO;
import com.bms.customer.dtos.response.CustomerDetailsResponseDTO;
import java.util.List;
import java.util.Map;

public interface CustomerService {

    CustomerRegistrationResponseDTO registerCustomer(CustomerRegisterRequestDTO requestDTO);

    AuthResponseDTO login(LoginRequest loginRequest);

    void logout(LogoutRequest logoutRequest);

    void changePassword(ChangePwdDTO changePwdDTO);

    CustomerDetailsResponseDTO getCustomerById(Long id);

    CustomerDetailsResponseDTO getCustomerByCifNumber(String cifNumber);

    List<CustomerDetailsResponseDTO> getAllCustomers();

    Map<String, Object> getLimitedCustomerInfo(String cifNumber);
}
