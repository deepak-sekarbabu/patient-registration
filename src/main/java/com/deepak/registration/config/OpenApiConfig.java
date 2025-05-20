package com.deepak.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Registration API")
                        .description("API documentation for Patient Registration Service.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Deepak Sekarbabu")
                                .email("deepak.sekarbabu@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Wiki")
                        .url("https://github.com/your-org/registration/wiki"));
    }
}