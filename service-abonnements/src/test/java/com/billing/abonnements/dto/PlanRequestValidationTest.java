package com.billing.abonnements.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PlanRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankNameAndMissingQuota() {
        PlanRequest request = new PlanRequest(" ", BigDecimal.TEN, null, true);

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("nom", "quotaUtilisateurs"), invalidProperties);
    }

    @Test
    void acceptsValidPlan() {
        PlanRequest request = new PlanRequest("Professionnel", BigDecimal.TEN, 5, true);

        assertTrue(validator.validate(request).isEmpty());
    }
}
