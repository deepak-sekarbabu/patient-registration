package com.deepak.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.deepak.registration", "com.logging.registration"})
public class RegistrationApplication {

  public static void main(String[] args) {
    SpringApplication.run(RegistrationApplication.class, args);
  }
}
