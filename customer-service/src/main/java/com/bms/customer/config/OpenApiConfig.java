package com.bms.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import java.util.List;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customerServiceOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Customer Service API")
                                                .description("API documentation for Customer Service in BMS (Bank Management System)")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("BMS Team")
                                                                .email("support@bms.com")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080").description("API Gateway"),
                                                new Server().url("http://localhost:8082")
                                                                .description("Local Service")));
        }
}
