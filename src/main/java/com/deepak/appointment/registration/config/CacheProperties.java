package com.deepak.appointment.registration.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {
  private final Map<String, CacheSpec> specs = new HashMap<>();

  @Data
  public static class CacheSpec {
    private Duration ttl = Duration.ofHours(1);
    private Integer maxSize = 100;
    private Integer initialCapacity = 10;
  }

  public Map<String, CacheSpec> getSpecs() {
    return specs;
  }

  public CacheSpec getSpec(String name) {
    return specs.getOrDefault(name, new CacheSpec());
  }
}
