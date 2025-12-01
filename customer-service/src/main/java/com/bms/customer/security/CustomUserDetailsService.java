package com.bms.customer.security;

import com.bms.customer.entities.Customer;
import com.bms.customer.repositories.CustomerRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String cifNumber) throws UsernameNotFoundException {
        Customer user = customerRepository.findByCifNumber(cifNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with CIF: " + cifNumber));

        String roleName = "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getCifNumber())
                .password(user.getPassword())
                .authorities(roleName)
                .build();
    }
}