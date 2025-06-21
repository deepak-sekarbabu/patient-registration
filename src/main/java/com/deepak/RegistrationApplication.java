package com.deepak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
    basePackages = {"com.deepak.appointment.registration", "com.deepak.patient.registration"})
public class RegistrationApplication {

  public static void main(String[] args) {
    SpringApplication.run(RegistrationApplication.class, args);
  }
}
