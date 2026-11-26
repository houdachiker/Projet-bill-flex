package com.billing.facturation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceRequest(
        @NotNull(message = "L'identifiant du client est obligatoire")
        @Positive(message = "L'identifiant du client doit être positif")
        Long clientId,

        @Positive(message = "L'identifiant de l'abonnement doit être positif")
        Long subscriptionId,

        @Pattern(regexp = "^\\s*$|^\\d{4}-(0[1-9]|1[0-2])$",
                message = "La période doit respecter le format AAAA-MM")
        String periode,

        LocalDate dateEcheance,

        @NotEmpty(message = "La facture doit contenir au moins une ligne")
        @Valid
        List<LineRequest> lignes) { }
