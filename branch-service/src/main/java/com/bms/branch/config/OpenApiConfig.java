package com.bms.branch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI branchServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Branch Service API")
                        .description("API documentation for Branch Service in BMS (Bank Management System)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BMS Team")
                                .email("support@bms.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8083").description("Local Service")));
    }
}
