package com.deepak.appointment.registration.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up caching with Caffeine. Cache configurations are externalized
 * through application properties.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

  public static final String CLINIC_INFO_CACHE = "clinicInfo";
  public static final String CLINIC_DOCTORS_CACHE = "clinicDoctors";
  public static final String DEFAULT_CACHE = "default";

  private final CacheProperties cacheProperties;

  public CacheConfig(CacheProperties cacheProperties) {
    this.cacheProperties = cacheProperties;
  }

  @Bean
  public Caffeine<Object, Object> defaultCaffeineConfig() {
    CacheProperties.CacheSpec spec = cacheProperties.getSpec(DEFAULT_CACHE);
    return Caffeine.newBuilder()
        .expireAfterWrite(spec.getTtl())
        .initialCapacity(spec.getInitialCapacity())
        .maximumSize(spec.getMaxSize())
        .recordStats();
  }

  @Bean
  public Caffeine<Object, Object> clinicInfoCaffeineConfig() {
    CacheProperties.CacheSpec spec = cacheProperties.getSpec(CLINIC_INFO_CACHE);
    return Caffeine.newBuilder()
        .expireAfterWrite(spec.getTtl())
        .initialCapacity(spec.getInitialCapacity())
        .maximumSize(spec.getMaxSize())
        .recordStats();
  }

  @Bean
  public Caffeine<Object, Object> clinicDoctorsCaffeineConfig() {
    CacheProperties.CacheSpec spec = cacheProperties.getSpec(CLINIC_DOCTORS_CACHE);
    return Caffeine.newBuilder()
        .expireAfterWrite(spec.getTtl())
        .initialCapacity(spec.getInitialCapacity())
        .maximumSize(spec.getMaxSize())
        .recordStats();
  }

  @Bean
  public CompositeCacheManager cacheManager(
      Caffeine<Object, Object> defaultCaffeineConfig,
      Caffeine<Object, Object> clinicInfoCaffeineConfig,
      Caffeine<Object, Object> clinicDoctorsCaffeineConfig) {
    CaffeineCacheManager defaultCacheManager = new CaffeineCacheManager(DEFAULT_CACHE);
    defaultCacheManager.setCaffeine(defaultCaffeineConfig);

    CaffeineCacheManager clinicInfoCacheManager = new CaffeineCacheManager(CLINIC_INFO_CACHE);
    clinicInfoCacheManager.setCaffeine(clinicInfoCaffeineConfig);

    CaffeineCacheManager clinicDoctorsCacheManager = new CaffeineCacheManager(CLINIC_DOCTORS_CACHE);
    clinicDoctorsCacheManager.setCaffeine(clinicDoctorsCaffeineConfig);

    CompositeCacheManager compositeCacheManager =
        new CompositeCacheManager(
            defaultCacheManager, clinicInfoCacheManager, clinicDoctorsCacheManager);
    compositeCacheManager.setFallbackToNoOpCache(true);

    return compositeCacheManager;
  }
}
