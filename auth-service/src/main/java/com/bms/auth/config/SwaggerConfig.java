package com.bms.auth.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI accountOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Management System - Account Microservice API")
                        .description("REST APIs for auth-services managing user register & login")
                        .version("v1.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("BMS Documentation")
                        .url("http://localhost:8080/swagger-ui.html"));
    }
}
