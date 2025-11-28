package com.bms.report.config;

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
    public OpenAPI reportServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Report Service API")
                        .description("API documentation for Report Service in BMS")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BMS Team")
                                .email("support@bms.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8091").description("Local Service")));
    }
}
