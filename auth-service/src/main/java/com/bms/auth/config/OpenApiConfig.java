package com.bms.auth.config;

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
        public OpenAPI authServiceOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Auth Service API")
                                                .description("API documentation for Auth Service in BMS")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("BMS Team")
                                                                .email("support@bms.com")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080").description("API Gateway"),
                                                new Server().url("http://localhost:8081")
                                                                .description("Local Service")));
        }
}
