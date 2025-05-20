package com.deepak.registration.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AllowedCommunicationMethodsValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface AllowedCommunicationMethods {
  String message() default
      "Only one communication method is allowed. Please select one of: Email, SMS, or Whatsapp";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
