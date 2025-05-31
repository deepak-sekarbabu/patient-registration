package com.deepak.registration.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class OpenApiSwaggerConfig {
  private static final Logger logger = LoggerFactory.getLogger(OpenApiSwaggerConfig.class);

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Patient Registration API")
                .description("API documentation for Patient Registration Service.")
                .version("1.0.0")
                .contact(new Contact().name("Deepak Sekarbabu").email("deepak.sekarbabu@gmail.com"))
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
        .externalDocs(
            new ExternalDocumentation()
                .description("Project Wiki")
                .url("https://github.com/deepak-sekarbabu/patient-registration"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearer-key",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .security(Collections.singletonList(new SecurityRequirement().addList("bearer-key")));
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init(ApplicationReadyEvent event) {
    // Fetch OpenAPI bean from the application context
    OpenAPI openAPI = event.getApplicationContext().getBean(OpenAPI.class);
    // Force initialization of the OpenAPI documentation at startup.
    openAPI.hashCode();
    logger.info("Swagger API documentation initialized on startup.");
  }
}
