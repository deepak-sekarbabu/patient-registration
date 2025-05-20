package com.deepak.registration.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedCommunicationMethodsValidator
        implements ConstraintValidator<AllowedCommunicationMethods, List<String>> {
    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList("Email", "SMS", "Whatsapp"));

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        return value.stream().allMatch(ALLOWED::contains);
    }
}
