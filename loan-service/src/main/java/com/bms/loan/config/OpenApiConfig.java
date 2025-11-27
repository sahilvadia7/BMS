package com.bms.loan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Service API")
                        .description("API documentation for Loan Service in BMS")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BMS Team")
                                .email("support@bms.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8086").description("Local Service")));
    }
}
