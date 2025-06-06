package com.deepak.patient.registration.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllowedCommunicationMethodsValidator
    implements ConstraintValidator<AllowedCommunicationMethods, List<String>> {
  private static final Set<String> ALLOWED =
      new HashSet<>(Arrays.asList("Email", "SMS", "Whatsapp"));

  @Override
  public boolean isValid(List<String> value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return false; // At least one method must be selected
    }

    // Only one method can be selected
    if (value.size() != 1) {
      return false;
    }

    // The selected method must be one of the allowed values
    return ALLOWED.contains(value.getFirst());
  }
}
