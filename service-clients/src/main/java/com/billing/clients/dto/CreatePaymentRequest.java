package com.billing.clients.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull(message = "Le client est obligatoire")
        @Positive(message = "Identifiant client invalide")
        Long clientId,

        @NotNull(message = "La facture est obligatoire")
        @Positive(message = "Identifiant facture invalide")
        Long invoiceId,

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être supérieur ou égal à 0,01")
        @Digits(integer = 10, fraction = 2, message = "Montant invalide (2 décimales max)")
        BigDecimal montant
) { }