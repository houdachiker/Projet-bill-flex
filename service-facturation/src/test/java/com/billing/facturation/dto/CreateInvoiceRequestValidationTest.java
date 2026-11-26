package com.billing.facturation.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CreateInvoiceRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidClientAndNestedInvoiceLines() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
                0L, null, null, null, List.of(new LineRequest(" ", null, null)));

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(invalidProperties.containsAll(Set.of(
                "clientId",
                "lignes[0].description",
                "lignes[0].quantite",
                "lignes[0].prixUnitaire")));
    }
}
