package com.billing.abonnements.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotNull(message = "L'identifiant du client est obligatoire")
        @Positive(message = "L'identifiant du client doit être positif")
        Long clientId,

        @NotNull(message = "L'identifiant du plan est obligatoire")
        @Positive(message = "L'identifiant du plan doit être positif")
        Long planId,

        LocalDate dateDebut) { }
