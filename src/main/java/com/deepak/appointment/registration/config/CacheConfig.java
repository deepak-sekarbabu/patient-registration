package com.deepak.appointment.registration.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String CLINIC_INFO_CACHE = "clinicInfo";
  public static final String CLINIC_DOCTORS_CACHE = "clinicDoctors";
  public static final String DEFAULT_CACHE = "default";

  @Bean
  public Caffeine<Object, Object> defaultCaffeineConfig() {
    return Caffeine.newBuilder()
        .expireAfterWrite(8, TimeUnit.HOURS)
        .initialCapacity(100)
        .maximumSize(500);
  }

  @Bean
  public Caffeine<Object, Object> clinicInfoCaffeineConfig() {
    return Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .initialCapacity(10)
        .maximumSize(50);
  }

  @Bean
  public Caffeine<Object, Object> clinicDoctorsCaffeineConfig() {
    return Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .initialCapacity(20)
        .maximumSize(100);
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
