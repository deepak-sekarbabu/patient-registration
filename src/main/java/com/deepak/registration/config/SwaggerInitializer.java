package com.deepak.registration.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SwaggerInitializer {
  private final OpenAPI openAPI;

  public SwaggerInitializer(OpenAPI openAPI) {
    this.openAPI = openAPI;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    // Force initialization of the OpenAPI documentation at startup.
    openAPI.hashCode();
    System.out.println("Swagger API documentation initialized on startup.");
  }
}
