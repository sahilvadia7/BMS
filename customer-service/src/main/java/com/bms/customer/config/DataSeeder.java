package com.bms.customer.config;

import com.bms.customer.entities.Customer;
import com.bms.customer.enums.Gender;
import com.bms.customer.enums.Roles;
import com.bms.customer.enums.UserStatus;
import com.bms.customer.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@RequiredArgsConstructor
@Configuration
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.cif}")
    private String adminCif;

    @Value("${admin.password}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        if(customerRepository.findByCifNumber(adminCif).isEmpty()) {
            Customer admin = Customer.builder()
                    .cifNumber(adminCif)
                    .firstName("Super Admin")
                    .lastName("System")
                    .email("sahilvadia7776@gmail.com")
                    .phoneNo("9999999999")
                    .password(passwordEncoder.encode(password))
                    .role(Roles.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .gender(Gender.MALE)
                    .address("Head Office")
                    .dob(LocalDate.of(1990, 1, 1)) // required
                    .build();
            customerRepository.save(admin);

            System.out.println("✔ Admin user created with CIF: " + adminCif);
        }
        else {
            System.out.println("✔ Admin already exists. Skipping seeding.");
        }
    }
}
